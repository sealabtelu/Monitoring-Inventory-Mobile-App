package com.example.inventorymonitoring.data.model

data class DataBarang(
    val createdAt: Long = 0,
    val id: String = "",
    val jenis: String = "",
    val kodeBarang: String = "",
    val namaBarang: String = "",
    val nomorRak: String = "",
    val stokAwal: Int = 0,
    val stokSekarang: Int = 0,
    val updatedAt: Long = 0
)
