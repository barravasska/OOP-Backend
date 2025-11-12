import models.Categories
import models.Products
import models.Orders
import models.OrderItems
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime // <-- IMPORT YANG HILANG
import java.time.LocalDateTime // <-- IMPORT YANG HILANG
fun Application.configureDatabase() {

    val dbHost = System.getenv("DB_HOST") // Ambil host dari "brankas"
    val dbPort = System.getenv("DB_PORT") // Ambil port
    val dbName = System.getenv("DB_NAME") // Ambil nama DB
    val dbUser = System.getenv("DB_USER") // Ambil user
    val dbPassword = System.getenv("DB_PASSWORD") // Ambil password

    val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/$dbName?user=$dbUser&password=$dbPassword"
    val driver = "org.postgresql.Driver"
    Database.connect(jdbcURL, driver)
    transaction {
        // Buat ulang tabel dari nol
        SchemaUtils.create(Categories, Products, Orders, OrderItems)

        val isDbEmpty = Products.selectAll().empty()
        if (isDbEmpty) {

            println("DATABASE KOSONG, MENJALANKAN SEEDING DATA AWAL...")
        val kopiId = Categories.insert {
            it[name] = "Kopi"
            it[slug] = "menu-kopi"
        } get Categories.id

        val makananId = Categories.insert {
            it[name] = "Makanan"
            it[slug] = "menu-makanan"
        } get Categories.id

        val cemilanId = Categories.insert {
            it[name] = "Cemilan"
            it[slug] = "menu-cemilan"
        } get Categories.id

        // 4. Isi Produk
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
            it[name] = "Iced Chocolate"
            it[price] = 22000L
            it[imageUrl] = "assets/coklat.jpg"
        }

        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Hot Americano"
            it[price] = 22000L
            it[imageUrl] = "assets/Americano-Hot.jpeg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Iced Americano"
            it[price] = 22000L
            it[imageUrl] = "assets/Iced-Americano.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Latte (hot)"
            it[price] = 25000L
            it[imageUrl] = "assets/hot-latte.jpg" // (Buat nama gambar baru)
        }

// --- KATEGORI NON-KOPI ---
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Matcha Latte"
            it[price] = 38000L
            it[imageUrl] = "assets/matcha-latte.jpg"
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Avocado Smoothie"
            it[price] = 40000L
            it[imageUrl] = "assets/avocado.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Caramel Frappe"
            it[price] = 30000L
            it[imageUrl] = "assets/caramel-frappe.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Brewed Tea (hot)"
            it[price] = 15000L
            it[imageUrl] = "assets/brewed.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Lemon Tea"
            it[price] = 18000L
            it[imageUrl] = "assets/lemon-tea.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Hot Chocolate"
            it[price] = 20000L
            it[imageUrl] = "assets/hot-coklat.jpg" // (Buat nama gambar baru)
        }
        Products.insert {
            it[categoryId] = kopiId
            it[name] = "Matcha Milk"
            it[price] = 25000L
            it[imageUrl] = "assets/matcha-milk.jpg" // (Buat nama gambar baru)
        }

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

        // LOG untuk debugging
        println("=== DATABASE SEEDED ===")
        Products.selectAll().forEach { row ->
            println("Product: ID=${row[Products.id].value}, Name=${row[Products.name]}, Price=${row[Products.price]}")
        }
}   }
}