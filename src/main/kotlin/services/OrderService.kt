package services

import models.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
// Import WebSocket Managers
import plugins.AdminWebSocketManager
import plugins.UserWebSocketManager

object OrderService {

    /**
     * Creates a new order and notifies Admins via WebSocket.
     */
    suspend fun createOrder(request: CheckoutRequest): Pair<Int, Long> {
        // Variables to hold data for notification outside the transaction block
        var notifOrderId = 0
        var notifTableNumber = ""

        // 1. Database Transaction
        val result = newSuspendedTransaction(Dispatchers.IO) {
            val productIds = request.items.map { it.id }

            val productPrices = Products
                .selectAll()
                .where { Products.id inList productIds }
                .associate { it[Products.id].value to it[Products.price] }

            if (productPrices.size != productIds.distinct().size) {
                val missingIds = productIds.distinct().filter { it !in productPrices.keys }
                throw IllegalArgumentException("Produk ID tidak ditemukan di database: $missingIds")
            }

            var totalAmount = 0L
            request.items.forEach { cartItem ->
                val price = productPrices[cartItem.id] ?: 0L
                totalAmount += price * cartItem.quantity
            }

            val newOrderId = Orders.insertAndGetId {
                it[Orders.totalAmount] = totalAmount
                it[Orders.status] = "paid"
                it[Orders.tableNumber] = request.table
            }

            // Save data for notification
            notifOrderId = newOrderId.value
            notifTableNumber = (request.table ?: "-").toString()

            OrderItems.batchInsert(request.items) { cartItem ->
                this[OrderItems.orderId] = newOrderId
                this[OrderItems.productId] = cartItem.id
                this[OrderItems.quantity] = cartItem.quantity
                this[OrderItems.pricePerItem] = productPrices[cartItem.id] ?: 0L
            }

            Pair(newOrderId.value, totalAmount)
        }

        // 2. WebSocket Notification (Admin)
        // This runs AFTER the DB transaction is successful
        if (notifOrderId != 0) {
            try {
                // Call the suspend function directly without any synchronized block
                AdminWebSocketManager.notifyNewOrder(notifOrderId, notifTableNumber)
                println("DEBUG: ✅ Admin notification sent for Order #$notifOrderId")
            } catch (e: Exception) {
                println("DEBUG: ❌ Failed to send Admin notification: ${e.message}")
                e.printStackTrace()
            }
        }

        return result
    }

    /**
     * Updates order status and notifies User via WebSocket.
     */
    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        // 1. Database Transaction
        val success = newSuspendedTransaction(Dispatchers.IO) {
            val updatedRows = Orders.update({ Orders.id eq orderId }) {
                it[status] = newStatus
            }
            updatedRows > 0
        }

        // 2. WebSocket Notification (User)
        if (success) {
            try {
                // Call the suspend function directly
                UserWebSocketManager.notifyStatusUpdate(orderId, newStatus)
                println("DEBUG: ✅ User notification sent for Order #$orderId -> $newStatus")
            } catch (e: Exception) {
                println("DEBUG: ❌ Failed to send User notification: ${e.message}")
            }
        }

        return success
    }

    // --- Standard Get Methods (Unchanged) ---

    suspend fun getOrderStatus(orderId: Int): String? {
        return newSuspendedTransaction(Dispatchers.IO) {
            Orders.selectAll()
                .where { Orders.id eq orderId }
                .singleOrNull()
                ?.get(Orders.status)
        }
    }

    suspend fun getActiveOrders(): List<AdminOrderResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {
            val activeOrders = Orders
                .selectAll()
                .where { (Orders.status eq "paid") or (Orders.status eq "processing") }
                .orderBy(Orders.createdAt to SortOrder.ASC)
                .toList()

            val orderIds = activeOrders.map { it[Orders.id] }

            if (orderIds.isEmpty()) {
                return@newSuspendedTransaction emptyList()
            }

            data class ItemData(val orderId: Int, val adminItem: AdminOrderItem)

            val allItems = (OrderItems innerJoin Products)
                .selectAll()
                .where { OrderItems.orderId inList orderIds }
                .map { itemRow ->
                    ItemData(
                        orderId = itemRow[OrderItems.orderId].value,
                        adminItem = AdminOrderItem(
                            productName = itemRow[Products.name],
                            quantity = itemRow[OrderItems.quantity],
                            price = itemRow[OrderItems.pricePerItem]
                        )
                    )
                }

            val itemsByOrderId = allItems.groupBy { it.orderId }

            activeOrders.map { row ->
                val orderId = row[Orders.id].value
                val itemsForThisOrder = itemsByOrderId[orderId]?.map { it.adminItem } ?: emptyList()

                AdminOrderResponse(
                    id = orderId,
                    tableNumber = row[Orders.tableNumber],
                    status = row[Orders.status],
                    totalAmount = row[Orders.totalAmount],
                    items = itemsForThisOrder
                )
            }
        }
    }

    suspend fun getOrderHistory(): List<AdminOrderResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {
            val historyOrders = Orders
                .selectAll()
                .where { (Orders.status eq "complete") or (Orders.status eq "cancelled") }
                .orderBy(Orders.createdAt to SortOrder.DESC)
                .limit(50)
                .map { row ->
                    AdminOrderResponse(
                        id = row[Orders.id].value,
                        tableNumber = row[Orders.tableNumber],
                        status = row[Orders.status],
                        totalAmount = row[Orders.totalAmount],
                        items = mutableListOf()
                    )
                }

            historyOrders.forEach { order ->
                val items = (OrderItems innerJoin Products)
                    .selectAll()
                    .where { OrderItems.orderId eq order.id }
                    .map { itemRow ->
                        AdminOrderItem(
                            productName = itemRow[Products.name],
                            quantity = itemRow[OrderItems.quantity],
                            price = itemRow[OrderItems.pricePerItem]
                        )
                    }
                (order.items as MutableList).addAll(items)
            }

            historyOrders
        }
    }
}