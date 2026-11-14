package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val discountPercentage: Int = 0,
    val image: String = "",
    val quantity: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val finalUnitPrice: Double
        get() = if (discountPercentage > 0) price * (1.0 - discountPercentage / 100.0) else price

    val subtotal: Double
        get() = finalUnitPrice * quantity
}
