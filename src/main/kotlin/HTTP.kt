import io.ktor.http.* // <-- PENTING
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.* // <-- PENTING

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // <-- PENTING
    }
}