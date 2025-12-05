package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("id_cliente") val idCliente: Long? = null,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String? = null
)