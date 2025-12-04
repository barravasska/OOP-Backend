import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        // --- Izinkan semua metode yang digunakan frontend ---
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // --- Izinkan header yang digunakan JavaScript fetch() ---
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader("ngrok-skip-browser-warning") // ← Tambahkan ini

        // --- Penting agar browser tidak blokir karena beda origin ---
        allowCredentials = true
        anyHost() // gunakan ini untuk dev (jangan di produksi)
    }
}
