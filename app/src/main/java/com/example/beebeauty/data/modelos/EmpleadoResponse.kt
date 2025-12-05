package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class EmpleadoResponse(
    @SerializedName("id_empleado") val idEmpleado: Long,
    @SerializedName("id_usuario") val idUsuario: Long?,
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val cargo: String?,
    @SerializedName("slots_disponibles") val slotsDisponibles: Int? = 1
)