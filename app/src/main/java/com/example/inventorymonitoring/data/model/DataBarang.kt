package com.example.inventorymonitoring.data.model

data class DataBarang(
    val created_at: String = "",
    val id: String = "",
    val jenis: String = "",
    val kode_barang: String = "",
    val nama_barang: String = "",
    val nomor_rak: String = "",
    val status: String = "",
    val stok_awal: Int = 0,
    val stok_sekarang: Int = 0,
    val terkunci: Int = 0,
    val updated_at: String = "",
    val umur: Int = 0,
)
