package plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds // PENTING: Pakai ini untuk waktu

fun Application.configureWebSocket() {
    install(WebSockets) {
        // Menggunakan format waktu Kotlin yang benar (.seconds)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        // --- ROUTE ADMIN ---
        webSocket("/ws/admin") {
            AdminWebSocketManager.addSession(this)
            try {
                for (frame in incoming) {
                    // Loop ini menjaga koneksi tetap hidup sampai client putus
                }
            } finally {
                AdminWebSocketManager.removeSession(this)
            }
        }

        // --- ROUTE USER (Order Spesifik) ---
        webSocket("/ws/order/{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid order ID"))
                return@webSocket
            }

            UserWebSocketManager.addSession(orderId, this)
            try {
                for (frame in incoming) {
                    // Loop menjaga koneksi
                }
            } finally {
                UserWebSocketManager.removeSession(orderId, this)
            }
        }
    }
}

// --- MANAGER ADMIN ---
object AdminWebSocketManager {
    // Gunakan 'newKeySet' -> Ini adalah Set yang Thread-Safe (Aman tanpa synchronized)
    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()

    fun addSession(session: WebSocketSession) {
        sessions.add(session)
        println("✅ Admin connected. Total: ${sessions.size}")
    }

    fun removeSession(session: WebSocketSession) {
        sessions.remove(session)
        println("❌ Admin disconnected. Total: ${sessions.size}")
    }

    suspend fun notifyNewOrder(orderId: Int, tableNumber: String) {
        val message = """{"type":"new_order","orderId":$orderId,"table":"$tableNumber"}"""

        println("DEBUG: WS Manager menerima request broadcast. Jumlah Admin Aktif: ${sessions.size}")

        sessions.forEach { session ->
            try {
                println("DEBUG: Mengirim ke sesi admin...")
                session.send(Frame.Text(message))
                println("DEBUG: ✅ Terkirim ke satu admin.")
            } catch (e: Exception) {
                println("DEBUG: ❌ Gagal kirim ke sesi tertentu: ${e.message}")
            }
        }
    }
}

// --- MANAGER USER ---
object UserWebSocketManager {
    // Map utama thread-safe
    private val sessions = ConcurrentHashMap<Int, MutableSet<WebSocketSession>>()

    fun addSession(orderId: Int, session: WebSocketSession) {
        // Ambil Set untuk orderId ini, atau buat baru jika belum ada (Thread-Safe)
        sessions.computeIfAbsent(orderId) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    fun removeSession(orderId: Int, session: WebSocketSession) {
        val userSessions = sessions[orderId]
        userSessions?.remove(session)

        // Bersihkan memori jika tidak ada yang connect ke order ini lagi
        if (userSessions != null && userSessions.isEmpty()) {
            sessions.remove(orderId)
        }
    }

    suspend fun notifyStatusUpdate(orderId: Int, newStatus: String) {
        val message = """{"type":"status_update","orderId":$orderId,"status":"$newStatus"}"""

        // Langsung akses map dan loop. Tidak perlu synchronized block lagi.
        sessions[orderId]?.forEach { session ->
            try {
                session.send(Frame.Text(message)) // <-- ERROR HILANG DISINI
            } catch (e: Exception) {
                println("Error sending to user: ${e.message}")
            }
        }
    }
}