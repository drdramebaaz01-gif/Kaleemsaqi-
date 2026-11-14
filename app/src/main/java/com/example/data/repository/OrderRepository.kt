package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Order
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrderRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val ordersCollection = db.collection("orders")

    fun observeCustomerOrders(userId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }.sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                handleFirestoreError(e, OperationType.LIST, ordersCollection.path)
                throw e
            }
    }

    fun observeAllOrders(): Flow<List<Order>> {
        return ordersCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }.sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                handleFirestoreError(e, OperationType.LIST, ordersCollection.path)
                throw e
            }
    }

    suspend fun placeOrder(
        userId: String,
        customerName: String,
        mobileNumber: String,
        whatsappNumber: String,
        city: String,
        deliveryAddress: String,
        items: List<Map<String, Any>>,
        totalAmount: Double
    ): Result<String> {
        return try {
            val docRef = ordersCollection.document()
            val payload = mapOf(
                "userId" to userId,
                "customerName" to customerName.trim(),
                "mobileNumber" to mobileNumber.trim(),
                "whatsappNumber" to (if (whatsappNumber.isNotBlank()) whatsappNumber.trim() else mobileNumber.trim()),
                "city" to city.trim(),
                "deliveryAddress" to deliveryAddress.trim(),
                "items" to items,
                "totalAmount" to totalAmount,
                "paymentMethod" to "Cash on Delivery",
                "status" to "Pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ordersCollection.path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        val docRef = ordersCollection.document(orderId)
        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
