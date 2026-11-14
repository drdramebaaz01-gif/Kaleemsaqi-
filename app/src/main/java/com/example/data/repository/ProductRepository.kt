package com.example.data.repository

import android.content.Context
import com.example.R
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

class ProductRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val productsCollection = db.collection("products")

    fun observeProducts(): Flow<List<Product>> {
        return productsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                handleFirestoreError(e, OperationType.LIST, productsCollection.path)
                throw e
            }
    }

    suspend fun getProduct(productId: String): Result<Product?> {
        val docRef = productsCollection.document(productId)
        return try {
            val snapshot = docRef.get().await()
            val product = snapshot.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(product)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val docRef = productsCollection.document()
            val payload = mutableMapOf<String, Any>(
                "name" to product.name.trim(),
                "code" to product.code.trim().uppercase(),
                "description" to product.description.trim(),
                "price" to product.price,
                "discountPercentage" to product.discountPercentage,
                "category" to product.category,
                "stock" to product.stock,
                "images" to product.images,
                "featured" to product.featured,
                "isFlashSale" to product.isFlashSale,
                "isTrending" to product.isTrending,
                "isBestSeller" to product.isBestSeller,
                "isNewArrival" to product.isNewArrival,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, productsCollection.path)
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        val docRef = productsCollection.document(product.id)
        return try {
            val payload = mutableMapOf<String, Any>(
                "name" to product.name.trim(),
                "code" to product.code.trim().uppercase(),
                "description" to product.description.trim(),
                "price" to product.price,
                "discountPercentage" to product.discountPercentage,
                "category" to product.category,
                "stock" to product.stock,
                "images" to product.images,
                "featured" to product.featured,
                "isFlashSale" to product.isFlashSale,
                "isTrending" to product.isTrending,
                "isBestSeller" to product.isBestSeller,
                "isNewArrival" to product.isNewArrival,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        val docRef = productsCollection.document(productId)
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
