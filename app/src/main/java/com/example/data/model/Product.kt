package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPercentage: Int = 0,
    val category: String = "Electronics",
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    val featured: Boolean = false,
    val isFlashSale: Boolean = false,
    val isTrending: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val discountedPrice: Double
        get() = if (discountPercentage > 0) {
            price * (1.0 - discountPercentage / 100.0)
        } else {
            price
        }

    val primaryImage: String
        get() = images.firstOrNull() ?: ""
}
