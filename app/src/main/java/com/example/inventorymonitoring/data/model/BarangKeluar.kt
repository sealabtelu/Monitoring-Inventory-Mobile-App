package com.example.inventorymonitoring.data.model

data class BarangKeluar(
    val barangId: String = "",
    val createdAt: Long = 0,
    val dilakukanOleh: String = "",
    val id: String = "",
    val jenis: String = "",
    val kodeBarang: String = "",
    val stok: Int = 0,
    val tanggalKeluar: Long = 0,
    val updatedAt: Long = 0
)
