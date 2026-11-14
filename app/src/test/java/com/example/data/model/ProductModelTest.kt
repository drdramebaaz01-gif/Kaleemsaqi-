package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductModelTest {

    @Test
    fun testProductDiscountCalculation() {
        val product = Product(
            id = "test_1",
            name = "Smart Watch",
            price = 2000.0,
            discountPercentage = 25,
            stock = 10
        )

        assertEquals(1500.0, product.discountedPrice, 0.01)
    }

    @Test
    fun testCartItemSubtotal() {
        val cartItem = CartItem(
            id = "cart_1",
            productId = "prod_1",
            name = "Leather Bag",
            price = 3000.0,
            discountPercentage = 10,
            quantity = 3
        )

        // 3000 * 0.9 = 2700 * 3 = 8100
        assertEquals(2700.0, cartItem.finalUnitPrice, 0.01)
        assertEquals(8100.0, cartItem.subtotal, 0.01)
    }

    @Test
    fun testStoreCategoriesCompleteness() {
        assertEquals(9, StoreCategories.ALL.size)
        assertTrue(StoreCategories.NAMES.contains("Men's Fashion"))
        assertTrue(StoreCategories.NAMES.contains("Women's Fashion"))
        assertTrue(StoreCategories.NAMES.contains("Beauty & Skincare"))
        assertTrue(StoreCategories.NAMES.contains("Watches"))
        assertTrue(StoreCategories.NAMES.contains("Electronics"))
        assertTrue(StoreCategories.NAMES.contains("Shoes"))
        assertTrue(StoreCategories.NAMES.contains("Bags"))
        assertTrue(StoreCategories.NAMES.contains("Accessories"))
        assertTrue(StoreCategories.NAMES.contains("Home & Lifestyle"))
    }
}
