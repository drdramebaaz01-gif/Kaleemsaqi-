package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.ui.graphics.vector.ImageVector

data class StoreCategory(
    val name: String,
    val icon: ImageVector,
    val tagLine: String
)

object StoreCategories {
    val ALL = listOf(
        StoreCategory("Men's Fashion", Icons.Default.DryCleaning, "Shirts, Jeans, Ethnic"),
        StoreCategory("Women's Fashion", Icons.Default.Checkroom, "Kurtis, Sarees, Western"),
        StoreCategory("Beauty & Skincare", Icons.Default.Spa, "Glow, Makeup, Serums"),
        StoreCategory("Watches", Icons.Default.Watch, "Luxury, Smart, Casual"),
        StoreCategory("Electronics", Icons.Default.DevicesOther, "Gadgets, Audio, Phones"),
        StoreCategory("Shoes", Icons.Default.Face, "Sneakers, Formal, Heels"),
        StoreCategory("Bags", Icons.Default.ShoppingBag, "Backpacks, Totes, Clutches"),
        StoreCategory("Accessories", Icons.Default.Diamond, "Jewelry, Belts, Eyewear"),
        StoreCategory("Home & Lifestyle", Icons.Default.Home, "Decor, Kitchen, Bedding")
    )

    val NAMES = ALL.map { it.name }
}
