package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WishlistItem(
    @DocumentId val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val discountPercentage: Int = 0,
    val image: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
