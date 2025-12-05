package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class ServicioResponse(
    @SerializedName("id_servicio") val idServicio: Long,
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double,
    @SerializedName("id_categoria") val idCategoria: Long?,
    val categoria: String?
)
