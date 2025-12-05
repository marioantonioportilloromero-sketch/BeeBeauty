package com.example.beebeauty.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.beebeauty.data.modelos.Cita
import com.example.beebeauty.data.modelos.*
import com.example.beebeauty.network.RetrofitClient
import com.example.beebeauty.ui.theme.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasPantalla(
    nombreUsuario: String,
    rolUsuario: String,
    alCerrarSesion: () -> Unit,
    alNavegarNuevaCita: () -> Unit,
    alNavegarClientes: () -> Unit
) {
    var citas by remember { mutableStateOf<List<CitaResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarMenu by remember { mutableStateOf(false) }
    var citaSeleccionada by remember { mutableStateOf<CitaResponse?>(null) }
    var mostrarDialogoEstado by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    // Cargar citas
    LaunchedEffect(Unit) {
        cargarCitas { listaCitas ->
            citas = listaCitas
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citas") },
                navigationIcon = {
                    IconButton(onClick = { mostrarMenu = !mostrarMenu }) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ColorAcento),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = obtenerIniciales(nombreUsuario),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = alNavegarNuevaCita,
                containerColor = ColorAcento
            ) {
                Icon(Icons.Default.Add, "Nueva Cita", tint = Color.White)
            }
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize()) {
            // Menú lateral
            if (mostrarMenu) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(250.dp),
                    color = ColorPrincipal,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = nombreUsuario,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (rolUsuario == "administrador") "Administrador" else "Recepcionista",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OpcionMenu(
                            icono = Icons.Default.CalendarToday,
                            texto = "Citas",
                            seleccionado = true,
                            alClick = { mostrarMenu = false }
                        )

                        OpcionMenu(
                            icono = Icons.Default.Add,
                            texto = "Nueva Cita",
                            alClick = {
                                mostrarMenu = false
                                alNavegarNuevaCita()
                            }
                        )

                        OpcionMenu(
                            icono = Icons.Default.People,
                            texto = "Clientes",
                            alClick = {
                                mostrarMenu = false
                                alNavegarClientes()
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        OpcionMenu(
                            icono = Icons.Default.ExitToApp,
                            texto = "Cerrar Sesión",
                            alClick = alCerrarSesion
                        )
                    }
                }
            }

            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (citas.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay citas registradas",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(citas) { cita ->
                            TarjetaCita(
                                cita = cita,
                                alClick = {
                                    citaSeleccionada = cita
                                    mostrarDialogoEstado = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para cambiar estado
    if (mostrarDialogoEstado && citaSeleccionada != null) {
        DialogoCambiarEstado(
            cita = citaSeleccionada!!,
            alCerrar = { mostrarDialogoEstado = false },
            alCambiar = { nuevoEstado ->
                cambiarEstadoCita(citaSeleccionada!!.idReservacion, nuevoEstado) { exito ->
                    if (exito) {
                        Toast.makeText(contexto, "Estado actualizado", Toast.LENGTH_SHORT).show()
                        cargarCitas { listaCitas ->
                            citas = listaCitas
                        }
                    } else {
                        Toast.makeText(contexto, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
                mostrarDialogoEstado = false
            }
        )
    }
}

@Composable
fun OpcionMenu(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    seleccionado: Boolean = false,
    alClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (seleccionado) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = alClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icono,
            contentDescription = texto,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            texto,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TarjetaCita(
    cita: CitaResponse,
    alClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = cita.cliente,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${cita.fecha} - ${cita.hora ?: cita.horaInicio.substring(0, 5)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = obtenerColorEstado(cita.estado)
                ) {
                    Text(
                        text = traducirEstado(cita.estado),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cita.empleado,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "$${cita.totalPrecio ?: 0.0}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorAcento,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mostrar servicios si existen
            if (cita.serviciosNombres.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Servicios: ${cita.serviciosNombres}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DialogoCambiarEstado(
    cita: CitaResponse,
    alCerrar: () -> Unit,
    alCambiar: (String) -> Unit
) {
    val estados = listOf(
        "pendiente" to "Pendiente",
        "confirmada" to "Confirmada",
        "en_proceso" to "En Proceso",
        "completada" to "Completada",
        "cancelada" to "Cancelada",
        "no_asistio" to "No Asistió"
    )

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Cambiar Estado") },
        text = {
            Column {
                Text("Cita de: ${cita.cliente}")
                Text("Estado actual: ${traducirEstado(cita.estado)}")
                Spacer(modifier = Modifier.height(16.dp))
                estados.forEach { (valor, etiqueta) ->
                    Button(
                        onClick = { alCambiar(valor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = obtenerColorEstado(valor)
                        ),
                        enabled = cita.estado != valor
                    ) {
                        Text(etiqueta)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) {
                Text("Cancelar")
            }
        }
    )
}

fun obtenerColorEstado(estado: String): Color {
    return when (estado.lowercase()) {
        "pendiente" -> EstadoPendiente
        "confirmada" -> EstadoConfirmada
        "en_proceso" -> Color(0xFFFFE082)
        "completada" -> EstadoCompletada
        "cancelada" -> EstadoCancelada
        "no_asistio" -> Color(0xFFFFCDD2)
        else -> Color.Gray
    }
}

fun traducirEstado(estado: String): String {
    return when (estado.lowercase()) {
        "pendiente" -> "PENDIENTE"
        "confirmada" -> "CONFIRMADA"
        "en_proceso" -> "EN PROCESO"
        "completada" -> "COMPLETADA"
        "cancelada" -> "CANCELADA"
        "no_asistio" -> "NO ASISTIÓ"
        else -> estado.uppercase()
    }
}

fun obtenerIniciales(nombre: String): String {
    return nombre.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
}

fun cargarCitas(alCompletar: (List<CitaResponse>) -> Unit) {
    android.util.Log.d("CITAS_DEBUG", "==== CARGANDO CITAS ====")

    RetrofitClient.api.obtenerCitas().enqueue(object : Callback<List<CitaResponse>> {
        override fun onResponse(call: Call<List<CitaResponse>>, response: Response<List<CitaResponse>>) {
            android.util.Log.d("CITAS_DEBUG", "Código respuesta: ${response.code()}")
            android.util.Log.d("CITAS_DEBUG", "¿Exitoso?: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val citas = response.body() ?: emptyList()
                android.util.Log.d("CITAS_DEBUG", "Total citas recibidas: ${citas.size}")

                if (citas.isNotEmpty()) {
                    android.util.Log.d("CITAS_DEBUG", "Primera cita: ${citas[0]}")
                }

                alCompletar(citas)
            } else {
                android.util.Log.e("CITAS_DEBUG", "Error: ${response.errorBody()?.string()}")
                alCompletar(emptyList())
            }
        }

        override fun onFailure(call: Call<List<CitaResponse>>, t: Throwable) {
            android.util.Log.e("CITAS_DEBUG", "Fallo: ${t.message}", t)
            alCompletar(emptyList())
        }
    })
}

fun cambiarEstadoCita(idCita: Long, nuevoEstado: String, alCompletar: (Boolean) -> Unit) {
    val datos = mapOf("estado" to nuevoEstado)
    RetrofitClient.api.cambiarEstadoCita(idCita, datos).enqueue(object : Callback<RespuestaApi<Any>> {
        override fun onResponse(call: Call<RespuestaApi<Any>>, response: Response<RespuestaApi<Any>>) {
            alCompletar(response.isSuccessful && response.body()?.success == true)
        }

        override fun onFailure(call: Call<RespuestaApi<Any>>, t: Throwable) {
            alCompletar(false)
        }
    })
}