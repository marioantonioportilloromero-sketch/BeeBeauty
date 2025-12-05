package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class Servicio(
    @SerializedName("id_servicio") val idServicio: Long,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val duracion: Int,
    val disponible: Boolean = true
)