package models // <-- Pastikan ini nama package Anda

import kotlinx.serialization.Serializable

// Ini data class untuk Produk
@Serializable // <-- Penting agar bisa jadi JSON
data class Product(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val price: Long,
    val imageUrl: String
)

// Ini data class untuk Kategori
@Serializable
data class Category(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class CartItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Long
)

@Serializable
data class CheckoutRequest(
    val items: List<CartItem>,
    val table: Int  // <-- Ubah jadi Int
)

@Serializable
data class CheckoutResponse(
    val snapToken: String? = null,
    val orderId: Int? = null,
    val error: String? = null
)

@Serializable
data class UpdateStatusRequest(
    val orderId: Int,
    val newStatus: String
)

@Serializable
data class ProductRequest(
    val categoryId: Int,
    val name: String,
    val price: Long,
    val imageUrl: String
)

@Serializable
data class AdminOrderItem(
    val productName: String,
    val quantity: Int,
    val price: Long
)

// Data class untuk satu pesanan lengkap yang dikirim ke admin
@Serializable
data class AdminOrderResponse(
    val id: Int,
    val tableNumber: Int?,
    val status: String,
    val totalAmount: Long,
    val items: List<AdminOrderItem>
)