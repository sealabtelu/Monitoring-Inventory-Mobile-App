package com.example.inventorymonitoring.data.repository

import com.example.inventorymonitoring.data.datasource.FirestoreDataSource
import com.example.inventorymonitoring.data.datasource.RecentActivity
import com.example.inventorymonitoring.data.datasource.RecentNotif
import com.example.inventorymonitoring.data.model.DataBarang
import kotlinx.coroutines.flow.Flow

class FirestoreRepository(
    private val firestoreDataSource: FirestoreDataSource
) {
    // Method to update an existing item
    suspend fun updateBarang(dataBarang: DataBarang) {
        firestoreDataSource.updateBarang(dataBarang)
    }

    // Method to add a new item
    suspend fun addBarang(dataBarang: DataBarang) {
        firestoreDataSource.addBarang(dataBarang)
    }

    fun getRecentActivityStream(): Flow<List<RecentActivity>> {
        return firestoreDataSource.getRecentActivityStream()
    }

    fun getRecentNotifStream(): Flow<List<RecentNotif>> {
        return firestoreDataSource.getRecentNotifStream()
    }

    // Fetch a single item by ID
    fun getBarangById(itemId: String): Flow<DataBarang?> {
        return firestoreDataSource.getBarangById(itemId)
    }

    fun getUniqueNomorRak(): Flow<List<String>> {
        return firestoreDataSource.getUniqueNomorRak()
    }

    fun getAllDataBarang(): Flow<List<DataBarang>> {
        return firestoreDataSource.getAllDataBarang()
    }

    fun getRecentActivityForItem(itemId: String): Flow<List<RecentActivity>> {
        return firestoreDataSource.getRecentActivityForItem(itemId)
    }

    suspend fun getItemsWithEmptyNamaBarang(): List<DataBarang> {
        return firestoreDataSource.getItemsWithEmptyNamaBarang()
    }

    fun updateUmurAndTimestamp(): Flow<List<DataBarang>> {
        return firestoreDataSource.updateUmurAndTimestamp()
    }

    fun updateHilangItems(): Flow<List<DataBarang>> {
        return firestoreDataSource.updateHilangItems()
    }
}
