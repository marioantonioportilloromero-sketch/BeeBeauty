package com.example.beebeauty.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ CAMBIA ESTA URL POR LA QUE TE DA NGROK
    // Ejemplo: "https://abc123-def456.ngrok-free.app/"
    // IMPORTANTE: Debe terminar con / (barra)
    private const val BASE_URL = "https://azariah-unbrittle-gwen.ngrok-free.dev/"

    // Esta se construye automáticamente agregando "api/"
    const val API_URL = "${BASE_URL}api/"

    // Logger para ver las peticiones en Logcat
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor para agregar headers necesarios
    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("ngrok-skip-browser-warning", "true") // Necesario para ngrok
            .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }

    // Cliente HTTP con configuración de timeouts
    private val client = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)  // Primero los headers
        .addInterceptor(logging)             // Luego el logging
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Configuración de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(API_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Instancia de la API
    val api: ApiService = retrofit.create(ApiService::class.java)
}