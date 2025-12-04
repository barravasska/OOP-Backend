// --- GANTI SELURUH FILE DATABASES.KT ANDA DENGAN INI ---

import models.Categories
import models.Products
import models.Orders
import models.OrderItems
import models.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime // <-- Import penting
import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.LocalDateTime // <-- Import penting

fun Application.configureDatabase() {
    val jdbcURL: String = System.getenv("SUPABASE_DB_URL") ?: "jdbc:h2:file:./cafe-db"
    val driver: String = System.getenv("DB_DRIVER") ?: "org.h2.Driver"
    Database.connect(jdbcURL, driver)
    // ------------------------------------

    transaction {
        SchemaUtils.create(Categories, Products, Orders, OrderItems, Users)

        val isDbEmpty = Products.selectAll().empty()

        if (isDbEmpty) {

            println("📦 DATABASE SUPABASE KOSONG, MENJALANKAN SEEDING DATA AWAL...")

            val kopiId = Categories.insertAndGetId {
                it[name] = "Kopi"
                it[slug] = "menu-kopi"
            }
            val makananId = Categories.insertAndGetId {
                it[name] = "Makanan"
                it[slug] = "menu-makanan"
            }
            val cemilanId = Categories.insertAndGetId {
                it[name] = "Cemilan"
                it[slug] = "menu-cemilan"
            }
            val nonKopiId = Categories.insertAndGetId {
                it[name] = "Non-Kopi"
                it[slug] = "menu-non-kopi"
            }

            // --- ADMIN USER ---
            Users.insert {
                it[username] = "admin"
                it[passwordHash] = BCrypt.withDefaults()
                    .hashToString(12, "admin123".toCharArray())
            }
            println("✅ ADMIN USER CREATED (username: admin, password: admin123)")


            Products.insert {
                it[categoryId] = kopiId
                it[name] = "Brown Sugar Coffee"
                it[price] = 25000L
                it[imageUrl] = "assets/kopi_susu_aren.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId
                it[name] = "Cappuccino"
                it[price] = 20000L
                it[imageUrl] = "assets/cappuccino.png"
            }
            Products.insert {
                it[categoryId] = kopiId
                it[name] = "Hot Americano"
                it[price] = 22000L
                it[imageUrl] = "assets/Americano-Hot.jpeg"
            }
            Products.insert {
                it[categoryId] = kopiId
                it[name] = "Iced Americano"
                it[price] = 22000L
                it[imageUrl] = "assets/Iced-Americano.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId
                it[name] = "Latte (hot)"
                it[price] = 25000L
                it[imageUrl] = "assets/hot-latte.jpg"
            }

            // --- KATEGORI NON-KOPI (ID DIPERBAIKI) ---
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Iced Chocolate"
                it[price] = 22000L
                it[imageUrl] = "assets/coklat.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Matcha Latte"
                it[price] = 38000L
                it[imageUrl] = "assets/matcha-latte.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Avocado Smoothie"
                it[price] = 40000L
                it[imageUrl] = "assets/avocado.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Caramel Frappe"
                it[price] = 30000L
                it[imageUrl] = "assets/caramel-frappe.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Brewed Tea (hot)"
                it[price] = 15000L
                it[imageUrl] = "assets/brewed.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Lemon Tea"
                it[price] = 18000L
                it[imageUrl] = "assets/lemon-tea.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Hot Chocolate"
                it[price] = 20000L
                it[imageUrl] = "assets/hot-coklat.jpg"
            }
            Products.insert {
                it[categoryId] = kopiId // <-- DIPERBAIKI
                it[name] = "Matcha Milk"
                it[price] = 25000L
                it[imageUrl] = "assets/matcha-milk.jpg"
            }

            // --- KATEGORI MAKANAN ---
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Nasi Goreng Spesial"
                it[price] = 25000L
                it[imageUrl] = "assets/nasi-goreng-spesial.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Spaghetti Bolognese"
                it[price] = 35000L
                it[imageUrl] = "assets/spaghetti.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Chicken Steak dengan BBQ Sauce + French Fries"
                it[price] = 46000L
                it[imageUrl] = "assets/bbq.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Fish & Chips dengan Coleslaw"
                it[price] = 46000L
                it[imageUrl] = "assets/fish.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Beef Black Pepper"
                it[price] = 55000L
                it[imageUrl] = "assets/beef.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Ayam Geprek (varian pedas)"
                it[price] = 28000L
                it[imageUrl] = "assets/geprek.jpg"
            }
            Products.insert {
                it[categoryId] = makananId
                it[name] = "Sirloin Steak dengan Saus Lada Hitam"
                it[price] = 48000L
                it[imageUrl] = "assets/sirloin.jpg"
            }

            // --- KATEGORI CEMILAN ---
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Roti Bakar Coklat Keju"
                it[price] = 18000L
                it[imageUrl] = "assets/roti-bakar-coklat-keju.jpg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Pisang Goreng"
                it[price] = 15000L
                it[imageUrl] = "assets/pisang-goreng.jpg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Kentang Goreng Original"
                it[price] = 20000L
                it[imageUrl] = "assets/kentang-goreng.jpeg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Cireng Rujak"
                it[price] = 16000L
                it[imageUrl] = "assets/cireng-rujak.jpg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Waffle Coklat Ice Cream"
                it[price] = 28000L
                it[imageUrl] = "assets/waffle-ice-cream.jpg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Risoles Ragout (3 pcs)"
                it[price] = 19000L
                it[imageUrl] = "assets/risoles-ragout.jpg"
            }
            Products.insert {
                it[categoryId] = cemilanId
                it[name] = "Singkong Goreng Keju"
                it[price] = 17000L
                it[imageUrl] = "assets/singkong-keju.jpg"
            }

            println("✅ DATABASE SEEDED SUCCESSFULLY!")

            // --- INI 'ELSE' YANG BENAR UNTUK 'IF (ISDBEMPTY)' ---
        } else {
            println("ℹ️  Database Supabase already has data, skipping seeding.")
        }
    } // <-- Akhir dari blok 'transaction'
}