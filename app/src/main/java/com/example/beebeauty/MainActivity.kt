package com.example.beebeauty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.beebeauty.ui.pantallas.*
import com.example.beebeauty.ui.theme.BeeBeautyCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeeBeautyCareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavegacion()
                }
            }
        }
    }
}

@Composable
fun AppNavegacion() {
    var pantalla by remember { mutableStateOf("login") }
    var nombreUsuario by remember { mutableStateOf("") }
    var rolUsuario by remember { mutableStateOf("") }
    var idUsuario by remember { mutableStateOf(0L) }

    when (pantalla) {
        "login" -> {
            LoginPantalla(
                alIniciarSesion = { nombre, rol, id ->
                    nombreUsuario = nombre
                    rolUsuario = rol
                    idUsuario = id
                    pantalla = "citas"
                }
            )
        }

        "citas" -> {
            CitasPantalla(
                nombreUsuario = nombreUsuario,
                rolUsuario = rolUsuario,
                alCerrarSesion = {
                    nombreUsuario = ""
                    rolUsuario = ""
                    idUsuario = 0L
                    pantalla = "login"
                },
                alNavegarNuevaCita = { pantalla = "nueva_cita" },
                alNavegarClientes = { pantalla = "clientes" }
            )
        }

        "nueva_cita" -> {
            NuevaCitaPantalla(
                alVolver = {
                    pantalla = "citas"
                }
            )
        }

        "clientes" -> {
            ClientesPantalla(
                alVolver = { pantalla = "citas" }
            )
        }
    }
}