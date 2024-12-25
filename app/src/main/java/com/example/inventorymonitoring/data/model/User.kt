package com.example.inventorymonitoring.data.model

data class User(
    val email: String = "",
    val id: String = "",
    val password: String = "",
    val reset_code: Int = 0,
    val terakhir_login: String = "",
    val updated_at: String = "",
    val username: String = "",
)