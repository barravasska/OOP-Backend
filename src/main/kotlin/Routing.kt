package plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import at.favre.lib.crypto.bcrypt.BCrypt
import models.*
import services.*

@Serializable
data class UpdateStatusRequest(val orderId: Int, val newStatus: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

fun Application.configureRouting() {

    routing {
        // Tes route dasar
        get("/") {
            call.respondText("Test Aksen Coffee BY BARRA GAMING NIH.")
        }

        // RUTE UTAMA /api
        route("/api") {

            // RUTE PUBLIK

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
                        call.respond(
                            CheckoutResponse(
                                snapToken = snapToken,
                                orderId = newOrderId
                            )
                        )
                    } else {
                        throw Exception("Gagal membuat token pembayaran Midtrans.")
                    }
                } catch (e: Exception) {
                    call.respond(
                        CheckoutResponse(
                            error = e.message ?: "Terjadi kesalahan tidak diketahui"
                        )
                    )
                }
            }

            get("/order/status/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID Pesanan tidak valid"))
                    return@get
                }

                val status = OrderService.getOrderStatus(id)
                if (status != null) {
                    call.respond(mapOf("orderId" to id, "status" to status))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Pesanan tidak ditemukan"))
                }
            }

            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    val user = UserService.findByUsername(request.username)

                    if (user == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Username atau password salah"))
                        return@post
                    }

                    val passwordValid = BCrypt.verifyer()
                        .verify(request.password.toCharArray(), user.passwordHash)
                        .verified

                    if (passwordValid) {
                        val token = generateToken(user.username)
                        call.respond(mapOf("token" to token))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Username atau password salah"))
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }


            // RUTE ADMIN
            authenticate("auth-jwt") {

                // 🔹 Update status pesanan
                post("/order/update-status") {
                    try {
                        val request = call.receive<UpdateStatusRequest>()
                        val success = OrderService.updateOrderStatus(request.orderId, request.newStatus)

                        if (success) {
                            call.respond(
                                mapOf(
                                    "status" to "success",
                                    "message" to "Status pesanan #${request.orderId} diubah ke ${request.newStatus}"
                                )
                            )
                        } else {
                            throw Exception("Gagal update status")
                        }
                    } catch (e: Exception) {
                        call.respond(mapOf("status" to "error", "message" to e.message))
                    }
                }

                // 🔹 Lihat pesanan aktif
                get("/orders") {
                    try {
                        val orders = OrderService.getActiveOrders()
                        call.respond(orders)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }

                // 🔹 Lihat riwayat pesanan
                get("/orders/history") {
                    try {
                        val orders = OrderService.getOrderHistory()
                        call.respond(orders)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }

                // 🔹 Tambah produk
                post("/products") {
                    try {
                        val request = call.receive<ProductRequest>()
                        val newProduct = ProductService.createProduct(request)
                        call.respond(HttpStatusCode.Created, newProduct)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    }
                }

                // 🔹 Update produk
                put("/products/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID produk tidak valid"))
                        return@put
                    }

                    try {
                        val request = call.receive<ProductRequest>()
                        val success = ProductService.updateProduct(id, request)
                        if (success) {
                            call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Produk tidak ditemukan"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    }
                }

                // 🔹 Hapus produk
                delete("/products/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID produk tidak valid"))
                        return@delete
                    }

                    val success = ProductService.deleteProduct(id)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Produk tidak ditemukan"))
                    }
                }
            }
        }
    }
}
