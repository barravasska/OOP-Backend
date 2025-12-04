package services

import models.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

data class User(val id: Int, val username: String, val passwordHash: String)

object UserService {
    suspend fun findByUsername(username: String): User? {
        return newSuspendedTransaction(Dispatchers.IO) {
            println("=== SEARCHING USER ===")
            println("Looking for username: $username")

            // Debug: Tampilkan semua user yang ada
            val allUsers = Users.selectAll().map { it[Users.username] }
            println("Users in database: $allUsers")

            val result = Users.selectAll()
                .where { Users.username eq username }
                .map {
                    User(
                        id = it[Users.id].value,
                        username = it[Users.username],
                        passwordHash = it[Users.passwordHash]
                    )
                }
                .singleOrNull()

            if (result != null) {
                println("✅ User found: ${result.username}")
            } else {
                println("❌ User NOT found")
            }

            result
        }
    }
}