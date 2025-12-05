package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Long,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val rol: String,
    @SerializedName("foto_perfil") val fotoPerfil: String?
)

data class RespuestaLogin(
    val success: Boolean,
    val message: String,
    val usuario: Usuario?
)