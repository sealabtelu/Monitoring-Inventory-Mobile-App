package com.example.inventorymonitoring.data.repository

import com.example.inventorymonitoring.data.model.BarangKeluar
import com.example.inventorymonitoring.data.model.BarangMasuk
import com.example.inventorymonitoring.data.model.DataBarang
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore
) {

    fun getBarangStream(): Flow<List<DataBarang>> = callbackFlow {
        val subscription = firestore.collection("data_barang")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(DataBarang::class.java)
                    trySend(items)
                }
            }

        awaitClose { subscription.remove() }
    }

    fun getBarangMasukStream(): Flow<List<BarangMasuk>> = callbackFlow {
        val subscription = firestore.collection("barang_masuk")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(BarangMasuk::class.java)
                    trySend(items)
                }
            }

        awaitClose { subscription.remove() }
    }

    fun getBarangKeluarStream(): Flow<List<BarangKeluar>> = callbackFlow {
        val subscription = firestore.collection("barang_keluar")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(BarangKeluar::class.java)
                    trySend(items)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addBarang(barang: DataBarang) {
        val document = firestore.collection("data_barang").document()
        barang.copy(
            id = document.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).let {
            document.set(it)
        }
    }

    suspend fun addBarangMasuk(barangMasuk: BarangMasuk) {
        val document = firestore.collection("barang_masuk").document()
        barangMasuk.copy(
            id = document.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).let {
            document.set(it)
        }

        // Update stok in data_barang
        firestore.collection("data_barang")
            .document(barangMasuk.barangId)
            .get()
            .await()
            .toObject(DataBarang::class.java)
            ?.let { barang ->
                firestore.collection("data_barang")
                    .document(barangMasuk.barangId)
                    .update("stokSekarang", barang.stokSekarang + barangMasuk.stok)
            }
    }

    suspend fun addBarangKeluar(barangKeluar: BarangKeluar) {
        val document = firestore.collection("barang_keluar").document()
        barangKeluar.copy(
            id = document.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).let {
            document.set(it)
        }

        // Update stok in data_barang
        firestore.collection("data_barang")
            .document(barangKeluar.barangId)
            .get()
            .await()
            .toObject(DataBarang::class.java)
            ?.let { barang ->
                firestore.collection("data_barang")
                    .document(barangKeluar.barangId)
                    .update("stokSekarang", barang.stokSekarang - barangKeluar.stok)
            }
    }
}
