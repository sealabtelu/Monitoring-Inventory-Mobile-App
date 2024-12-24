package com.example.inventorymonitoring.data.repository

import com.example.inventorymonitoring.data.model.BarangMasuk
import com.example.inventorymonitoring.data.model.DataBarang
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore
) {

    fun getBarangStream(): Flow<List<DataBarang>> = flow {
        val snapshot = firestore.collection("barang").get().await()
        val items = snapshot.documents.mapNotNull { it.toObject(DataBarang::class.java) }
        emit(items)
    }

    fun getBarangById(itemId: String): Flow<DataBarang?> = flow {
        val document = firestore.collection("barang").document(itemId).get().await()
        val item = document.toObject(DataBarang::class.java)
        emit(item)
    }

    fun getBarangMasukStream(): Flow<List<BarangMasuk>> = flow {
        val snapshot = firestore.collection("barangMasuk").get().await()
        val items = snapshot.documents.mapNotNull { it.toObject(BarangMasuk::class.java) }
        emit(items)
    }
}
