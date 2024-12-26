package com.example.inventorymonitoring.data.repository

import android.util.Log
import com.example.inventorymonitoring.data.model.BarangHilang
import com.example.inventorymonitoring.data.model.BarangKeluar
import com.example.inventorymonitoring.data.model.BarangMasuk
import com.example.inventorymonitoring.data.model.DataBarang
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
    fun updateUmurAndTimestamp(): Flow<List<DataBarang>> = flow {
        val snapshot = firestore.collection("data_barang").get().await()
        val items = snapshot.documents.mapNotNull { document ->
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.let {
                if (it.status == "Masuk") {
                    // Calculate umur
                    val currentTime = System.currentTimeMillis()
                    val createdAt = it.created_at // This is a String
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Adjust the format as needed
                    val createdDate: Date? = createdAt?.let { dateFormat.parse(it) }

                    val umur = createdDate?.let { (currentTime - it.time) / (1000 * 60 * 60 * 24) }?.toInt() ?: 0 // Convert milliseconds to days

                    // Update the umur and updated_at fields
                    firestore.collection("data_barang").document(it.id)
                        .update("umur", umur).await()

                    it.copy(umur = umur) // Return the updated item
                } else {
                    it // Return the item without changes if status is not "Masuk"
                }
            }
        }
        emit(items)
    }
    fun updateHilangItems(): Flow<List<DataBarang>> = flow {
        Log.d("HilangCheck", "updateHilangItems called") // Log statement
        val snapshot = firestore.collection("data_barang").get().await()
        val currentTime = System.currentTimeMillis()
        val hilangItems = mutableListOf<DataBarang>()

        snapshot.documents.forEach { document ->
            val dataBarang = document.toObject(DataBarang::class.java)
            dataBarang?.let {
                // Log the item being processed
                Log.d("HilangCheck", "Processing item: ${it.nama_barang}, status: ${it.status}, updated_at: ${it.updated_at}")

                // Check if the item is "Keluar" or already "Hilang"
                if (it.status == "Keluar") {
                    Log.d("HilangCheck", "Item: ${it.nama_barang} has status 'Keluar'")

                    val updatedAt = it.updated_at // This is a nullable String
                    Log.d("HilangCheck", "Item: ${it.nama_barang}, Raw Updated At: $updatedAt") // Log raw updated_at

                    // Replace 'T' with a space for correct parsing
                    val formattedUpdatedAt = updatedAt?.replace("T", " ")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val updatedDate: Date? = formattedUpdatedAt?.let { dateFormat.parse(it) }

                    // Log the updated date for debugging
                    Log.d("HilangCheck", "Item: ${it.nama_barang}, Parsed Updated Date: $updatedDate")

                    // Calculate daysDifference only if updatedDate is not null
                    val daysDifference = updatedDate?.let { (currentTime - it.time) / (1000 * 60 * 60 * 24) }?.toInt() ?: 0

                    // Log the days difference for debugging
                    Log.d("HilangCheck", "Item: ${it.nama_barang}, Days Since Update: $daysDifference")

                    if (daysDifference >= 7) {
                        // Update the status to "Hilang"
                        firestore.collection("data_barang").document(it.id)
                            .update("status", "Hilang").await()

                        // Create BarangHilang object
                        val barangHilang = BarangHilang(
                            barang_id = it.id,
                            created_at = dateFormat.format(Date()),
                            id = UUID.randomUUID().toString(), // Generate a unique ID for barang_hilang
                            jenis = it.jenis, // Assuming jenis is the name of the item
                            kode_barang = it.kode_barang,
                            stok = it.stok_sekarang,
                            updated_at = updatedAt ?: "" // Provide a default value if updatedAt is null
                        )

                        // Add to barang_hilang collection
                        firestore.collection("barang_hilang").document(barangHilang.id).set(barangHilang).await()

                        // Add the updated item to the list
                        hilangItems.add(it.copy(status = "Hilang")) // Update the item status in the list

                        // Log the successful update
                        Log.d("HilangCheck", "Item: ${it.nama_barang} status updated to 'Hilang'")
                    }
                } else if (it.status == "Hilang") {
                    // If the item is already marked as "Hilang", add it to the list
                    hilangItems.add(it)
                    Log.d("HilangCheck", "Item: ${it.nama_barang} is already marked as 'Hilang' and added to hilangItems")
                }
            }
        }
        emit(hilangItems) // Emit the list of DataBarang items that are marked as "Hilang"
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
