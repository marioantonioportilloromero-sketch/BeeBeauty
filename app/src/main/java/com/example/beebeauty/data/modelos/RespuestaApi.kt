package com.example.beebeauty.data.modelos

data class RespuestaApi<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null,
    val total: Int? = null
)