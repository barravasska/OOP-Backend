// GANTI SELURUH ISI FILE Application.kt DENGAN INI

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import plugins.configureSecurity // <-- IMPORT PENTING
import plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Database & Dasar-dasar
    configureDatabase()
    configureSerialization()
    configureHTTP()

    // --- 2. PERUBAHAN KRUSIAL DI SINI ---
    // SECURITY HARUS DIPASANG DULU
    configureSecurity()
    configureWebSocket() // ← Tambahkan ini


    // BARU KEMUDIAN ROUTING
    configureRouting()
}