package services

import models.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.dao.id.EntityID

object OrderService {

    suspend fun createOrder(request: CheckoutRequest): Pair<Int, Long> {
        return newSuspendedTransaction(Dispatchers.IO) {
            val productIds = request.items.map { it.id }

            println("=== DEBUG CHECKOUT ===")
            println("Product IDs from cart: $productIds")

            val productPrices = mutableMapOf<Int, Long>()

            Products.selectAll().forEach { row ->
                val productId = row[Products.id].value
                println("Found product in DB: ID=$productId, Name=${row[Products.name]}, Price=${row[Products.price]}")
                if (productId in productIds) {
                    productPrices[productId] = row[Products.price]
                }
            }

            println("Product prices found: $productPrices")

            val missingIds = productIds.filter { it !in productPrices.keys }
            if (missingIds.isNotEmpty()) {
                throw IllegalArgumentException("Product IDs not found in database: $missingIds")
            }

            var totalAmount = 0L
            request.items.forEach { cartItem ->
                val price = productPrices[cartItem.id] ?: 0L
                totalAmount += price * cartItem.quantity
            }

            val newOrderId = Orders.insertAndGetId {
                it[Orders.totalAmount] = totalAmount
                it[Orders.status] = "pending"
                it[Orders.tableNumber] = request.table
            }

            println("Order created with ID: ${newOrderId.value}")

            request.items.forEach { cartItem ->
                println("Inserting OrderItem: orderId=${newOrderId.value}, productId=${cartItem.id}")
                OrderItems.insertAndGetId {
                    it[OrderItems.orderId] = newOrderId
                    it[OrderItems.productId] = EntityID(cartItem.id, Products)
                    it[OrderItems.quantity] = cartItem.quantity
                    it[OrderItems.pricePerItem] = productPrices[cartItem.id] ?: 0L
                }
            }

            println("=== CHECKOUT SUCCESS ===")
            Pair(newOrderId.value, totalAmount)
        }
    }

    suspend fun getOrderStatus(orderId: Int): String? {
        return newSuspendedTransaction(Dispatchers.IO) {
            // Gunakan selectAll + firstOrNull
            Orders.selectAll()
                .where { Orders.id eq orderId }
                .singleOrNull()
                ?.get(Orders.status)
        }
    }

    suspend fun getActiveOrders(): List<AdminOrderResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {

            // 1. Ambil semua pesanan yang statusnya "paid" atau "processing"
            val activeOrders = Orders
                .selectAll()
                .where { (Orders.status eq "paid") or (Orders.status eq "processing") }
                .orderBy(Orders.createdAt to SortOrder.ASC) // Urutkan dari yang paling lama
                .map { row ->
                    val orderId = row[Orders.id].value

                    // 2. Langsung ambil items untuk order ini
                    val items = (OrderItems innerJoin Products)
                        .selectAll()
                        .where { OrderItems.orderId eq orderId }
                        .map { itemRow ->
                            AdminOrderItem(
                                productName = itemRow[Products.name],
                                quantity = itemRow[OrderItems.quantity],
                                price = itemRow[OrderItems.pricePerItem]
                            )
                        }

                    // 3. Return order dengan items-nya
                    AdminOrderResponse(
                        id = orderId,
                        tableNumber = row[Orders.tableNumber],
                        status = row[Orders.status],
                        totalAmount = row[Orders.totalAmount],
                        items = items
                    )
                }

            activeOrders
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            val updatedRows = Orders.update(
                where = { Orders.id eq orderId }
            ) {
                it[Orders.status] = newStatus
            }
            updatedRows > 0
        }
    }
}