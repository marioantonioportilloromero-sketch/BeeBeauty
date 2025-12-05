package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class Cita(
    @SerializedName("id_reservacion") val idReservacion: Long,
    @SerializedName("id_cliente") val idCliente: Long,
    @SerializedName("id_empleado") val idEmpleado: Long,
    val fecha: String,
    val hora: String,
    val estado: String,
    val total: Double,
    val duracion: Int,
    val notas: String?,

    // Relaciones
    val cliente: Cliente?,
    val empleado: Empleado?,
    val servicios: List<ServicioCita>?
)

data class ServicioCita(
    @SerializedName("id_servicio") val idServicio: Long,
    val nombre: String,
    val precio: Double,
    val duracion: Int
)