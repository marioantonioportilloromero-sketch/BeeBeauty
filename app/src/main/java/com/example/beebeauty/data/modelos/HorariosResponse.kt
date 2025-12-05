package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class HorariosResponse(
    val slots: List<SlotHorario>,
    val fecha: String?,
    @SerializedName("empleado_id") val empleadoId: Long?,
    @SerializedName("duracion_requerida") val duracionRequerida: Int?
)

data class SlotHorario(
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    val disponible: Boolean
)