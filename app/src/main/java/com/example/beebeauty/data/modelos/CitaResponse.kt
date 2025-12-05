package com.example.beebeauty.data.modelos

import com.google.gson.annotations.SerializedName

data class CitaResponse(
    @SerializedName("id_reservacion") val idReservacion: Long,
    @SerializedName("id_cliente") val idCliente: Long,
    val cliente: String,
    val telefono: String?,
    val empleado: String,
    val fecha: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    val hora: String?,
    val estado: String,
    val notas: String?,
    // Cambiar de String? a List<ServicioDetalle>?
    @SerializedName("servicios_detalle") val serviciosDetalle: List<ServicioDetalle>?,
    @SerializedName("total_precio") val totalPrecio: Double?,
    @SerializedName("motivo_cancelacion") val motivoCancelacion: String? = null
) {
    // Propiedad calculada para obtener nombres de servicios
    val serviciosNombres: String
        get() = serviciosDetalle?.joinToString(", ") { it.nombre } ?: ""
}

data class ServicioDetalle(
    @SerializedName("id_servicio") val idServicio: Long,
    val nombre: String,
    @SerializedName("duracion_real") val duracionReal: Int,
    @SerializedName("precio_aplicado") val precioAplicado: Double
)