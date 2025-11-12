package services

// --- PASTIKAN SEMUA IMPORT INI ADA ---
import models.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ProductService {

    // --- READ (GET) - INI SUDAH ADA ---
    suspend fun getAllProducts(): List<Product> {
        return newSuspendedTransaction(Dispatchers.IO) {
            Products.selectAll()
                .map { row ->
                    Product(
                        id = row[Products.id].value,
                        categoryId = row[Products.categoryId].value,
                        name = row[Products.name],
                        price = row[Products.price],
                        imageUrl = row[Products.imageUrl]
                    )
                }
        }
    }

    // --- CREATE (POST) - FUNGSI BARU ---
    suspend fun createProduct(request: ProductRequest): Product {
        return newSuspendedTransaction(Dispatchers.IO) {
            // Masukkan produk baru ke tabel
            val newId = Products.insert {
                it[categoryId] = request.categoryId
                it[name] = request.name
                it[price] = request.price
                it[imageUrl] = request.imageUrl
            } get Products.id // Ambil ID yang baru dibuat

            // Kembalikan data produk lengkap
            Product(
                id = newId.value,
                categoryId = request.categoryId,
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl
            )
        }
    }

    // --- UPDATE (PUT) - FUNGSI BARU ---
    suspend fun updateProduct(id: Int, request: ProductRequest): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            // Update baris di mana ID-nya cocok
            val updatedRows = Products.update({ Products.id eq id }) {
                it[categoryId] = request.categoryId
                it[name] = request.name
                it[price] = request.price
                it[imageUrl] = request.imageUrl
            }
            updatedRows > 0 // Kembalikan true jika ada baris yang di-update
        }
    }

    // --- DELETE (DELETE) - FUNGSI BARU ---
    suspend fun deleteProduct(id: Int): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            // Hapus baris di mana ID-nya cocok
            val deletedRows = Products.deleteWhere { Products.id eq id }
            deletedRows > 0 // Kembalikan true jika ada baris yang dihapus
        }
    }
}