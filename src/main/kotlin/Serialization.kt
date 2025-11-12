import io.ktor.serialization.kotlinx.json.* // <-- PENTING
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.* // <-- PENTING
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json() // <-- PENTING
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}