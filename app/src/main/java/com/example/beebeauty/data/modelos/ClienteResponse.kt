package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class ClienteResponse(
    @SerializedName("id_cliente") val idCliente: Long,
    @SerializedName("id_usuario") val idUsuario: Long,
    val nombre: String,
    val correo: String,
    val telefono: String,
    @SerializedName("numero_reservaciones") val numeroReservaciones: Int? = 0,
    @SerializedName("ultima_visita") val ultimaVisita: String? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null
)

data class ClienteRequest(
    val nombre: String,
    val correo: String,
    val telefono: String
)