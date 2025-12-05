package com.example.beebeauty.data.solicitudes

import com.google.gson.annotations.SerializedName

data class HorariosDisponiblesRequest(
    @SerializedName("id_empleado") val idEmpleado: Long,
    val fecha: String,
    @SerializedName("duracion_total") val duracionTotal: Int
)