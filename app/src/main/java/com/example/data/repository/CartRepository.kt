package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CartRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun cartCollection(userId: String) =
        db.collection("users").document(userId).collection("cart")

    fun observeCart(userId: String): Flow<List<CartItem>> {
        val col = cartCollection(userId)
        return col.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }.sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                handleFirestoreError(e, OperationType.LIST, col.path)
                throw e
            }
    }

    suspend fun addToCart(userId: String, product: Product, quantity: Int = 1): Result<Unit> {
        val col = cartCollection(userId)
        return try {
            // Check if item already exists in cart for this product
            val existing = col.whereEqualTo("productId", product.id).get().await()
            if (!existing.isEmpty) {
                val doc = existing.documents.first()
                val currentQty = (doc.getLong("quantity") ?: 1L).toInt()
                doc.reference.update(
                    mapOf(
                        "quantity" to (currentQty + quantity),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val docRef = col.document()
                val payload = mapOf(
                    "productId" to product.id,
                    "name" to product.name,
                    "price" to product.price,
                    "discountPercentage" to product.discountPercentage,
                    "image" to product.primaryImage,
                    "quantity" to quantity,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, col.path)
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(userId: String, cartItemId: String, newQuantity: Int): Result<Unit> {
        val docRef = cartCollection(userId).document(cartItemId)
        return try {
            if (newQuantity <= 0) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "quantity" to newQuantity,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(userId: String, cartItemId: String): Result<Unit> {
        val docRef = cartCollection(userId).document(cartItemId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> {
        val col = cartCollection(userId)
        return try {
            val snapshot = col.get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, col.path)
            Result.failure(e)
        }
    }
}
