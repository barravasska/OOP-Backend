package services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.CartItem
import java.util.* // Untuk Base64

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

// Data class untuk "cetakan" JSON yang DIKIRIM ke Midtrans
@Serializable
private data class MidtransTransactionRequest(
    @SerialName("transaction_details") val transactionDetails: TransactionDetails,
    @SerialName("item_details") val itemDetails: List<ItemDetails>,
    // Kita bisa tambahkan customer_details jika perlu
) {
    @Serializable
    data class TransactionDetails(
        @SerialName("order_id") val orderId: String,
        @SerialName("gross_amount") val grossAmount: Long
    )
    @Serializable
    data class ItemDetails(
        val id: String,
        val price: Long,
        val quantity: Int,
        val name: String
    )
}

// Data class untuk "cetakan" JSON yang DITERIMA dari Midtrans
@Serializable
private data class MidtransTransactionResponse(
    val token: String,
    @SerialName("redirect_url") val redirectUrl: String
)

object MidtransService {

    private val MIDTRANS_SERVER_KEY = System.getenv("MIDTRANS_SERVER_KEY")
    private const val MIDTRANS_API_URL = "https://app.sandbox.midtrans.com/snap/v1/transactions"

    // Fungsi utama: Buat Snap Token
    suspend fun createSnapToken(orderId: Int, totalAmount: Long, items: List<CartItem>): String? {

        // 1. Siapkan "Authorization" Header
        // Midtrans pakai Basic Auth (Server Key + ":") di-encode Base64
        val authHeader = "Basic " + Base64.getEncoder().encodeToString(
            "$MIDTRANS_SERVER_KEY:".toByteArray()
        )

        // 2. Siapkan Body JSON untuk dikirim ke Midtrans
        val requestBody = MidtransTransactionRequest(
            transactionDetails = MidtransTransactionRequest.TransactionDetails(
                orderId = "KAFE-ORDER-$orderId-${System.currentTimeMillis()}", // Buat ID unik
                grossAmount = totalAmount
            ),
            itemDetails = items.map {
                MidtransTransactionRequest.ItemDetails(
                    id = it.id.toString(),
                    price = it.price,
                    quantity = it.quantity,
                    name = it.name
                )
            }
        )

        // 3. Panggil API Midtrans pakai Ktor Client
        try {
            val response: HttpResponse = client.post(MIDTRANS_API_URL) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, authHeader)
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()

            if (response.status == HttpStatusCode.Created) {
                // Jika sukses, ambil token-nya
                val midtransResponse = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<MidtransTransactionResponse>(responseBody)

                return midtransResponse.token
            } else {
                // Jika gagal
                println("Midtrans Error: $responseBody")
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}