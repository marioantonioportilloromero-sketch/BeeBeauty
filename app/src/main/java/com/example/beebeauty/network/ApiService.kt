package com.example.beebeauty.network

import com.example.beebeauty.data.modelos.*
import com.example.beebeauty.data.solicitudes.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {


    @POST("login")
    fun login(@Body solicitud: LoginSolicitud): Call<RespuestaLogin>


    @GET("reservaciones")
    fun obtenerCitas(): Call<List<CitaResponse>>

    @GET("reservaciones/{id}")
    fun obtenerCita(@Path("id") id: Long): Call<RespuestaApi<CitaResponse>>

    @POST("reservaciones")
    fun crearCita(@Body solicitud: CitaSolicitud): Call<RespuestaApi<CitaResponse>>

    @PUT("reservaciones/{id}")
    fun actualizarCita(@Path("id") id: Long, @Body solicitud: Any): Call<RespuestaApi<Any>>

    @PATCH("reservaciones/{id}/estado")
    fun cambiarEstadoCita(@Path("id") id: Long, @Body estado: Map<String, String>): Call<RespuestaApi<Any>>

    @DELETE("reservaciones/{id}")
    fun eliminarCita(@Path("id") id: Long): Call<RespuestaApi<String>>


    @GET("clientes")
    fun obtenerClientes(): Call<RespuestaApi<List<ClienteResponse>>>

    @GET("clientes/{id}")
    fun obtenerCliente(@Path("id") id: Long): Call<RespuestaApi<ClienteResponse>>

    @POST("clientes")
    fun crearCliente(@Body cliente: ClienteRequest): Call<RespuestaApi<ClienteResponse>>

    @PUT("clientes/{id}")
    fun actualizarCliente(@Path("id") id: Long, @Body cliente: ClienteRequest): Call<RespuestaApi<ClienteResponse>>

    @DELETE("clientes/{id}")
    fun eliminarCliente(@Path("id") id: Long): Call<RespuestaApi<String>>


    @GET("servicios")
    fun obtenerServicios(): Call<RespuestaApi<List<ServicioResponse>>>

    @GET("servicios/{id}")
    fun obtenerServicio(@Path("id") id: Long): Call<RespuestaApi<ServicioResponse>>


    @GET("empleados")
    fun obtenerEmpleados(): Call<RespuestaApi<List<EmpleadoResponse>>>

    @POST("empleados/disponibles")
    fun obtenerEmpleadosDisponibles(@Body datos: EmpleadosDisponiblesRequest): Call<RespuestaApi<List<EmpleadoResponse>>>

    @POST("horarios/disponibles")
    fun obtenerHorariosDisponibles(@Body datos: HorariosDisponiblesRequest): Call<RespuestaApi<HorariosResponse>>
}