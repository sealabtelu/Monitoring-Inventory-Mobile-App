package com.example.inventorymonitoring.data.model

data class User(
    val email: String = "",
    val id: String = "",
    val password: String = "",
    val resetCode: String = "",
    val terakhirLogin: Long = 0,
    val updatedAt: Long = 0,
    val username: String = "",
)