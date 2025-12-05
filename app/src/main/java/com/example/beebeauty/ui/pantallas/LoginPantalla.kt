package com.example.beebeauty.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.beebeauty.data.solicitudes.LoginSolicitud
import com.example.beebeauty.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPantalla(
    alIniciarSesion: (String, String, Long) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Bee Beauty Care",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Sistema de Gestión",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))


            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario o Correo") },
                leadingIcon = { Icon(Icons.Default.Person, "Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !cargando
            )

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, "Contraseña") },
                trailingIcon = {
                    IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                        Icon(
                            if (mostrarContrasena) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarContrasena)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !cargando
            )

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    if (usuario.isBlank() || contrasena.isBlank()) {
                        Toast.makeText(
                            contexto,
                            "Por favor completa todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    cargando = true
                    val contrasenaCifrada = cifrarSHA256(contrasena)

                    android.util.Log.d("LOGIN_DEBUG", "====== DATOS A ENVIAR ======")
                    android.util.Log.d("LOGIN_DEBUG", "URL: ${RetrofitClient.API_URL}login")
                    android.util.Log.d("LOGIN_DEBUG", "Usuario: $usuario")
                    android.util.Log.d("LOGIN_DEBUG", "Contraseña original: $contrasena")
                    android.util.Log.d("LOGIN_DEBUG", "Contraseña cifrada: $contrasenaCifrada")
                    android.util.Log.d("LOGIN_DEBUG", "Longitud cifrada: ${contrasenaCifrada.length}")

                    val solicitud = LoginSolicitud(
                        username = usuario,
                        password = contrasenaCifrada
                    )

                    RetrofitClient.api.login(solicitud).enqueue(object : Callback<com.example.beebeauty.data.modelos.RespuestaLogin> {
                        override fun onResponse(
                            call: Call<com.example.beebeauty.data.modelos.RespuestaLogin>,
                            response: Response<com.example.beebeauty.data.modelos.RespuestaLogin>
                        ) {
                            cargando = false
                            if (response.isSuccessful && response.body()?.success == true) {
                                val respuesta = response.body()!!
                                val usuarioData = respuesta.usuario!!


                                if (usuarioData.rol == "administrador" || usuarioData.rol == "recepcionista") {
                                    Toast.makeText(
                                        contexto,
                                        "Bienvenido ${usuarioData.nombre}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    alIniciarSesion(
                                        usuarioData.nombre,
                                        usuarioData.rol,
                                        usuarioData.idUsuario
                                    )
                                } else {
                                    Toast.makeText(
                                        contexto,
                                        "Acceso denegado. Solo administradores y recepcionistas",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    contexto,
                                    "Credenciales incorrectas",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<com.example.beebeauty.data.modelos.RespuestaLogin>, t: Throwable) {
                            cargando = false
                            Toast.makeText(
                                contexto,
                                "Error de conexión: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Usuarios de prueba:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Admin: karim.benzema@beebeauty.com / Admin123!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Recep: laura.martinez@beebeauty.com / Recep123!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}


fun cifrarSHA256(texto: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
