package com.example.inventorymonitoring.data.repository

import com.example.inventorymonitoring.data.model.BarangKeluar
import com.example.inventorymonitoring.data.model.BarangMasuk
import com.example.inventorymonitoring.data.model.DataBarang
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    fun getRecentActivityStream(): Flow<List<RecentActivity>> = flow {
        val barangMasukSnapshot = firestore.collection("barang_masuk").get().await()
        val barangKeluarSnapshot = firestore.collection("barang_keluar").get().await()
        val dataBarangSnapshot = firestore.collection("data_barang").get().await()

        // Convert data_barang documents to a map for quick lookup
        val dataBarangMap = dataBarangSnapshot.documents.associate { document ->
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.id to dataBarang // Ensure you are using the correct field for the key
        }

        val recentActivities = mutableListOf<RecentActivity>()

        // Process barang_masuk
        for (document in barangMasukSnapshot.documents) {
            val barangMasuk = document.toObject(BarangMasuk::class.java)
            val dataBarang = dataBarangMap[barangMasuk?.barang_id]
            if (dataBarang != null) {
                if (barangMasuk != null) {
                    recentActivities.add(
                        RecentActivity(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Masuk",
                            timestamp = barangMasuk.created_at // Use the appropriate timestamp
                        )
                    )
                }
            }
        }

        // Process barang_keluar
        for (document in barangKeluarSnapshot.documents) {
            val barangKeluar = document.toObject(BarangKeluar::class.java)
            val dataBarang = dataBarangMap[barangKeluar?.barang_id]
            if (dataBarang != null) {
                if (barangKeluar != null) {
                    recentActivities.add(
                        RecentActivity(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Keluar",
                            timestamp = barangKeluar.created_at // Use the appropriate timestamp
                        )
                    )
                }
            }
        }

        // Sort activities by timestamp in descending order
        recentActivities.sortByDescending { it.timestamp }

        // Limit to the latest 5 activities
        emit(recentActivities.take(5))
    }

    fun getRecentNotifStream() : Flow<List<RecentNotif>> = flow {
        val barangMasukSnapshot = firestore.collection("barang_masuk").get().await()
        val barangKeluarSnapshot = firestore.collection("barang_keluar").get().await()
        val dataBarangSnapshot = firestore.collection("data_barang").get().await()

        // Convert data_barang documents to a map for quick lookup
        val dataBarangMap = dataBarangSnapshot.documents.associate { document ->
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.id to dataBarang // Ensure you are using the correct field for the key
        }

        val recentNotif = mutableListOf<RecentNotif>()

        // Process barang_masuk
        for (document in barangMasukSnapshot.documents) {
            val barangMasuk = document.toObject(BarangMasuk::class.java)
            val dataBarang = dataBarangMap[barangMasuk?.barang_id]
            if (dataBarang != null) {
                if (barangMasuk != null) {
                    recentNotif.add(
                        RecentNotif(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Masuk",
                            timestamp = barangMasuk.updated_at // Use the appropriate timestamp
                        )
                    )
                }
            }
        }

        // Process barang_keluar
        for (document in barangKeluarSnapshot.documents) {
            val barangKeluar = document.toObject(BarangKeluar::class.java)
            val dataBarang = dataBarangMap[barangKeluar?.barang_id]
            if (dataBarang != null) {
                if (barangKeluar != null) {
                    recentNotif.add(
                        RecentNotif(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Keluar",
                            timestamp = barangKeluar.updated_at
                        )
                    )
                }
            }
        }

        // Sort activities by timestamp in descending order
        recentNotif.sortByDescending { it.timestamp }

        emit(recentNotif.take(30))
    }


    // Fetch a single item by ID
    fun getBarangById(itemId: String): Flow<DataBarang?> = flow {
        val document = firestore.collection("data_barang").document(itemId).get().await()
        val item = document.toObject(DataBarang::class.java)
        emit(item)
    }

    fun getUniqueNomorRak(): Flow<List<String>> = flow {
        val dataBarangSnapshot = firestore.collection("data_barang").get().await()
        val nomorRakSet = mutableSetOf<String>()

        for (document in dataBarangSnapshot.documents) {
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.nomor_rak?.let { nomorRakSet.add(it) }
        }

        emit(nomorRakSet.toList()) // Convert the set to a list and emit it
    }
    fun getAllDataBarang(): Flow<List<DataBarang>> = flow {
        val snapshot = firestore.collection("data_barang").get().await()
        val items = snapshot.documents.mapNotNull { it.toObject(DataBarang::class.java) }
        emit(items)
    }
    fun getRecentActivityForItem(itemId: String): Flow<List<RecentActivity>> = flow {
        val barangMasukSnapshot = firestore.collection("barang_masuk").get().await()
        val barangKeluarSnapshot = firestore.collection("barang_keluar").get().await()
        val dataBarangSnapshot = firestore.collection("data_barang").get().await()

        // Convert data_barang documents to a map for quick lookup
        val dataBarangMap = dataBarangSnapshot.documents.associate { document ->
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.id to dataBarang
        }

        val recentActivities = mutableListOf<RecentActivity>()

        // Process barang_masuk
        for (document in barangMasukSnapshot.documents) {
            val barangMasuk = document.toObject(BarangMasuk::class.java)
            if (barangMasuk?.barang_id == itemId) {
                val dataBarang = dataBarangMap[barangMasuk.barang_id]
                if (dataBarang != null) {
                    recentActivities.add(
                        RecentActivity(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Masuk",
                            timestamp = barangMasuk.created_at
                        )
                    )
                }
            }
        }

        // Process barang_keluar
        for (document in barangKeluarSnapshot.documents) {
            val barangKeluar = document.toObject(BarangKeluar::class.java)
            if (barangKeluar?.barang_id == itemId) {
                val dataBarang = dataBarangMap[barangKeluar.barang_id]
                if (dataBarang != null) {
                    recentActivities.add(
                        RecentActivity(
                            namaBarang = dataBarang.nama_barang,
                            nomorRak = "Ruangan " + dataBarang.nomor_rak,
                            status = "Barang Keluar",
                            timestamp = barangKeluar.created_at
                        )
                    )
                }
            }
        }

        // Sort activities by timestamp in descending order
        recentActivities.sortByDescending { it.timestamp }

        // Emit the activities
        emit(recentActivities)
    }
    fun updateItemLockStatus(itemId: String, status: Int) {
        firestore.collection("data_barang").document(itemId)
            .update("terkunci", status)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

}

// Data class to represent recent activity
data class RecentActivity(
    val namaBarang: String,
    val nomorRak: String,
    val status: String,
    val timestamp: String
)


data class RecentNotif(
    val namaBarang: String,
    val nomorRak: String,
    val status: String,
    val timestamp: String
)
