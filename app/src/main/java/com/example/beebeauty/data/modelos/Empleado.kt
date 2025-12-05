package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class Empleado(
    @SerializedName("id_empleado") val idEmpleado: Long,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val especialidad: String?,
    @SerializedName("foto_perfil") val fotoPerfil: String?,
    val disponible: Boolean = true
)