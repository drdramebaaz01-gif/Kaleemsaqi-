package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Product
import com.example.data.model.WishlistItem
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WishlistRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun wishlistCollection(userId: String) =
        db.collection("users").document(userId).collection("wishlist")

    fun observeWishlist(userId: String): Flow<List<WishlistItem>> {
        val col = wishlistCollection(userId)
        return col.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(WishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }.sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                handleFirestoreError(e, OperationType.LIST, col.path)
                throw e
            }
    }

    suspend fun toggleWishlist(userId: String, product: Product): Result<Boolean> {
        val col = wishlistCollection(userId)
        return try {
            val existing = col.whereEqualTo("productId", product.id).get().await()
            if (!existing.isEmpty) {
                for (doc in existing.documents) {
                    doc.reference.delete().await()
                }
                Result.success(false)
            } else {
                val docRef = col.document()
                val payload = mapOf(
                    "productId" to product.id,
                    "name" to product.name,
                    "price" to product.price,
                    "discountPercentage" to product.discountPercentage,
                    "image" to product.primaryImage,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, col.path)
            Result.failure(e)
        }
    }

    suspend fun removeFromWishlist(userId: String, wishlistItemId: String): Result<Unit> {
        val docRef = wishlistCollection(userId).document(wishlistItemId)
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
}
