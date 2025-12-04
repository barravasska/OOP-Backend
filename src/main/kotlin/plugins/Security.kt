package plugins // <-- Pastikan ini nama package-nya

// --- SEMUA IMPORT INI PENTING ---
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

// --- KITA BACA "KUNCI" DARI "BRANKAS" (Environment Variables) ---
// Kita pakai '?:' untuk memberi nilai default saat jalan di laptop
private val secret = System.getenv("JWT_SECRET") ?: "ini-rahasia-lokal-jangan-dipakai-di-prod"
private val issuer = System.getenv("JWT_ISSUER") ?: "http://localhost:8080"
private val audience = System.getenv("JWT_AUDIENCE") ?: "cafe-admin"
private val jwtRealm = "Cafe Admin Area"

/**
 * FUNGSI YANG HILANG: Untuk membuat Token
 */
fun generateToken(username: String): String = JWT.create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withClaim("username", username)
    .withExpiresAt(Date(System.currentTimeMillis() + 60_000 * 60 * 24)) // Token valid 24 jam
    .sign(Algorithm.HMAC256(secret))

/**
 * Fungsi untuk menginstal "Gembok" (Plugin Authentication)
 */
fun Application.configureSecurity() {

    install(Authentication) {
        jwt("auth-jwt") { // Kita beri nama "auth-jwt"
            realm = jwtRealm

            verifier(JWT.require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())

            // Ktor akan validasi token. Jika valid, dia akan menjalankan blok ini
            validate { credential ->
                // Cek apakah ada claim "username" di dalam token
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}