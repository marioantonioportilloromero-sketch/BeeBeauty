package com.example.beebeauty.data.solicitudes

import com.google.gson.annotations.SerializedName

data class CitaSolicitud(
    @SerializedName("id_cliente") val idCliente: Long,
    @SerializedName("id_empleado") val idEmpleado: Long,
    val fecha: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    val servicios: List<ServicioSolicitud>,
    val notas: String? = null,
    @SerializedName("creado_por") val creadoPor: Long = 1
)

data class ServicioSolicitud(
    @SerializedName("id_empserv") val idEmpserv: Long,
    val orden: Int,
    val duracion: Int
)