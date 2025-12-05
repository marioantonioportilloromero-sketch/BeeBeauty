package com.example.beebeauty.data.solicitudes

data class EmpleadosDisponiblesRequest(
    val servicios: List<Long>,
    val fecha: String
)