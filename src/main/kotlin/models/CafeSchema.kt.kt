package models
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.dao.id.IntIdTable
import java.time.LocalDateTime

object Categories : IntIdTable() {
    val name = varchar("name", 100)
    val slug = varchar("slug", 100).uniqueIndex()
}

object Products : IntIdTable() {
    val categoryId = reference("category_id", Categories)
    val name = varchar("name", 255)
    val price = long("price")
    val imageUrl = varchar("image_url", 500)
}

object Orders : IntIdTable() {
    val totalAmount = long("total_amount")
    val status = varchar("status", 20).default("pending")
    val tableNumber = integer("table_number") // <-- TAMBAHKAN INI
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

object OrderItems : IntIdTable("orderitems") {
    val orderId = reference("order_id", Orders)
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    val pricePerItem = long("price_per_item")
}