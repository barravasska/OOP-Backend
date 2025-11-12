// TAMBAHKAN SEMUA IMPORT INI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

// Ini adalah fungsi main/utama yang akan dijalankan
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    // 2. Jalankan server di port yang sudah ditentukan
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// Ini adalah "modul" aplikasi kita
// Ktor akan memanggil fungsi-fungsi konfigurasi dari file lain
fun Application.module() {
    // 1. Panggil configureDatabase() dari Databases.kt
    configureDatabase()

    // 2. Panggil configureRouting() dari Routing.kt
    configureRouting()

    // 3. Panggil configureSerialization() dari Serialization.kt
    configureSerialization()

    // 4. Panggil configureHTTP() dari HTTP.kt (untuk CORS)
    configureHTTP()
}