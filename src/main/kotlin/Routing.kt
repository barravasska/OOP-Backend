// --- PASTIKAN SEMUA IMPORT INI ADA ---
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.* // <-- Penting untuk status code
import models.* // <-- Impor semua data class
import services.CategoryService
import services.MidtransService
import services.OrderService
import services.ProductService

// (Data class UpdateStatusRequest Anda tetap di sini)
data class UpdateStatusRequest(val orderId: Int, val newStatus: String)

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Halo! Ini Backend Aksen Coffee.")
        }

        // --- SEMUA RUTE API KITA ---
        route("/api") {

            // --- RUTE PELANGGAN (LAMA) ---
            get("/products")   { /* ... (Kode lama Anda, tidak berubah) ... */ }
            get("/categories") { /* ... (Kode lama Anda, tidak berubah) ... */ }
            post("/checkout")  { /* ... (Kode lama Anda, tidak berubah) ... */ }
            get("/order/status/{id}") { /* ... (Kode lama Anda, tidak berubah) ... */ }
            post("/order/update-status") { /* ... (Kode lama Anda, tidak berubah) ... */ }

            // --- ============================ ---
            // --- RUTE ADMIN CRUD (BARU) ---
            // --- ============================ ---

            // --- CREATE (POST) ---
            post("/products") {
                try {
                    // 1. Terima JSON produk baru dari Admin
                    val request = call.receive<ProductRequest>()
                    // 2. Panggil service untuk menyimpannya
                    val newProduct = ProductService.createProduct(request)
                    // 3. Kirim balik produk yang baru dibuat
                    call.respond(HttpStatusCode.Created, newProduct)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // --- UPDATE (PUT) ---
            put("/products/{id}") {
                // 1. Ambil ID dari URL (misal: /api/products/12)
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID produk tidak valid"))
                    return@put
                }
                try {
                    // 2. Terima JSON data produk yang sudah diedit
                    val request = call.receive<ProductRequest>()
                    // 3. Panggil service untuk update
                    val success = ProductService.updateProduct(id, request)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Produk #${id} berhasil di-update"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Produk tidak ditemukan"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // --- DELETE (DELETE) ---
            delete("/products/{id}") {
                // 1. Ambil ID dari URL
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID produk tidak valid"))
                    return@delete
                }

                // 2. Panggil service untuk hapus
                val success = ProductService.deleteProduct(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Produk #${id} berhasil dihapus"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Produk tidak ditemukan"))
                }
            }

            get("/orders") {
                try {
                    val orders = OrderService.getActiveOrders()
                    call.respond(orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // --- INI KODE LAMA ANDA, PASTIKAN TETAP ADA ---
            get("/products") {
                val products = ProductService.getAllProducts()
                call.respond(products)
            }
            get("/categories") {
                val categories = CategoryService.getAllCategories()
                call.respond(categories)
            }
            post("/checkout") {
                try {
                    val request = call.receive<CheckoutRequest>()
                    val (newOrderId, totalAmount) = OrderService.createOrder(request)
                    val snapToken = MidtransService.createSnapToken(
                        orderId = newOrderId,
                        totalAmount = totalAmount,
                        items = request.items
                    )
                    if (snapToken != null) {
                        call.respond(CheckoutResponse(
                            snapToken = snapToken,
                            orderId = newOrderId
                        ))
                    } else {
                        throw Exception("Gagal membuat token pembayaran Midtrans.")
                    }
                } catch (e: Exception) {
                    call.respond(CheckoutResponse(
                        error = e.message ?: "Terjadi kesalahan tidak diketahui"
                    ))
                }
            }
            get("/order/status/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(mapOf("error" to "ID Pesanan tidak valid"))
                    return@get
                }
                val status = OrderService.getOrderStatus(id)
                if (status != null) {
                    call.respond(mapOf("orderId" to id, "status" to status))
                } else {
                    call.respond(mapOf("error" to "Pesanan tidak ditemukan"))
                }
            }
            post("/order/update-status") {
                try {
                    val request = call.receive<UpdateStatusRequest>()
                    val success = OrderService.updateOrderStatus(request.orderId, request.newStatus)
                    if (success) {
                        call.respond(mapOf("status" to "success", "message" to "Status pesanan #${request.orderId} diubah ke ${request.newStatus}"))
                    } else {
                        throw Exception("Gagal update status atau pesanan tidak ditemukan")
                    }
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to e.message))
                }
            }

        } // akhir /api
    } // akhir routing
}