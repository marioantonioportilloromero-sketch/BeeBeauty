package com.example.beebeauty.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.beebeauty.data.modelos.*
import com.example.beebeauty.data.solicitudes.CitaSolicitud
import com.example.beebeauty.data.solicitudes.EmpleadosDisponiblesRequest
import com.example.beebeauty.data.solicitudes.HorariosDisponiblesRequest
import com.example.beebeauty.data.solicitudes.ServicioSolicitud
import com.example.beebeauty.network.RetrofitClient
import com.example.beebeauty.ui.theme.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaCitaPantalla(
    alVolver: () -> Unit
) {
    val contexto = LocalContext.current


    var clientes by remember { mutableStateOf<List<ClienteResponse>>(emptyList()) }
    var servicios by remember { mutableStateOf<List<ServicioResponse>>(emptyList()) }
    var empleados by remember { mutableStateOf<List<EmpleadoResponse>>(emptyList()) }
    var horarios by remember { mutableStateOf<List<SlotHorario>>(emptyList()) }

    var clienteSeleccionado by remember { mutableStateOf<ClienteResponse?>(null) }
    var serviciosSeleccionados by remember { mutableStateOf<List<ServicioResponse>>(emptyList()) }
    var empleadoSeleccionado by remember { mutableStateOf<EmpleadoResponse?>(null) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horarioSeleccionado by remember { mutableStateOf<SlotHorario?>(null) }
    var notas by remember { mutableStateOf("") }

    var mostrarSelectorCliente by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var cargandoEmpleados by remember { mutableStateOf(false) }
    var cargandoHorarios by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cargarClientesNuevaCita { clientes = it }
        cargarServiciosNuevaCita { servicios = it }
    }


    LaunchedEffect(serviciosSeleccionados, fechaSeleccionada) {
        try {
            if (serviciosSeleccionados.isNotEmpty() && fechaSeleccionada.isNotEmpty()) {
                cargandoEmpleados = true
                empleadoSeleccionado = null
                horarioSeleccionado = null
                horarios = emptyList()

                android.util.Log.d("NUEVA_CITA", "Cargando empleados para servicios: ${serviciosSeleccionados.map { it.idServicio }}")

                val idsServicios = serviciosSeleccionados.map { it.idServicio }
                val datos = mapOf(
                    "servicios" to idsServicios,
                    "fecha" to fechaSeleccionada
                )

                cargarEmpleadosDisponiblesNuevaCita(idsServicios, fechaSeleccionada) { empleadosDisponibles ->
                    android.util.Log.d("NUEVA_CITA", "Empleados recibidos: ${empleadosDisponibles.size}")
                    empleados = empleadosDisponibles
                    cargandoEmpleados = false
                }
            } else {
                empleados = emptyList()
                empleadoSeleccionado = null
                horarioSeleccionado = null
                horarios = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("NUEVA_CITA", "Error en LaunchedEffect de empleados: ${e.message}", e)
            cargandoEmpleados = false
            empleados = emptyList()
        }
    }

    LaunchedEffect(empleadoSeleccionado, fechaSeleccionada, serviciosSeleccionados) {
        try {
            if (empleadoSeleccionado != null && fechaSeleccionada.isNotEmpty() && serviciosSeleccionados.isNotEmpty()) {
                cargandoHorarios = true
                horarioSeleccionado = null
                val duracionTotal = serviciosSeleccionados.sumOf { it.duracion }

                cargarHorariosDisponiblesNuevaCita(
                    empleadoSeleccionado!!.idEmpleado,
                    fechaSeleccionada,
                    duracionTotal
                ) { horariosDisponibles ->
                    android.util.Log.d("NUEVA_CITA", "Horarios recibidos: ${horariosDisponibles.size}")
                    horarios = horariosDisponibles
                    cargandoHorarios = false
                }
            } else {
                horarios = emptyList()
                horarioSeleccionado = null
            }
        } catch (e: Exception) {
            android.util.Log.e("NUEVA_CITA", "Error en LaunchedEffect de horarios: ${e.message}", e)
            cargandoHorarios = false
            horarios = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Cita") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ResumenCitaNueva(
                    cliente = clienteSeleccionado,
                    servicios = serviciosSeleccionados,
                    empleado = empleadoSeleccionado,
                    fecha = fechaSeleccionada,
                    horario = horarioSeleccionado
                )
            }

            item {
                SeccionFormularioNuevaCita(
                    titulo = "1. Cliente",
                    icono = Icons.Default.Person,
                    completado = clienteSeleccionado != null
                ) {
                    if (clienteSeleccionado != null) {
                        TarjetaClienteSeleccionadoNuevaCita(
                            cliente = clienteSeleccionado!!,
                            alCambiar = { mostrarSelectorCliente = true }
                        )
                    } else {
                        Button(
                            onClick = { mostrarSelectorCliente = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Cliente")
                        }
                    }
                }
            }
            item {
                SeccionFormularioNuevaCita(
                    titulo = "2. Fecha",
                    icono = Icons.Default.CalendarToday,
                    completado = fechaSeleccionada.isNotEmpty()
                ) {
                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (fechaSeleccionada.isEmpty()) "Seleccionar Fecha"
                            else fechaSeleccionada
                        )
                    }
                }
            }


            item {
                SeccionFormularioNuevaCita(
                    titulo = "3. Servicios",
                    icono = Icons.Default.Spa,
                    completado = serviciosSeleccionados.isNotEmpty()
                ) {
                    if (servicios.isEmpty()) {
                        Text("Cargando servicios...")
                    } else {
                        Column {
                            servicios.forEach { servicio ->
                                TarjetaServicioNuevaCita(
                                    servicio = servicio,
                                    seleccionado = serviciosSeleccionados.contains(servicio),
                                    alClick = {
                                        try {
                                            android.util.Log.d("NUEVA_CITA", "Click en servicio: ${servicio.nombre}")

                                            serviciosSeleccionados = if (serviciosSeleccionados.contains(servicio)) {
                                                android.util.Log.d("NUEVA_CITA", "Deseleccionando servicio")
                                                serviciosSeleccionados - servicio
                                            } else {
                                                android.util.Log.d("NUEVA_CITA", "Seleccionando servicio")
                                                serviciosSeleccionados + servicio
                                            }

                                            android.util.Log.d("NUEVA_CITA", "Servicios seleccionados: ${serviciosSeleccionados.size}")
                                        } catch (e: Exception) {
                                            android.util.Log.e("NUEVA_CITA", "Error al seleccionar servicio: ${e.message}", e)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }


            if (serviciosSeleccionados.isNotEmpty() && fechaSeleccionada.isNotEmpty()) {
                item {
                    SeccionFormularioNuevaCita(
                        titulo = "4. Empleado",
                        icono = Icons.Default.Person,
                        completado = empleadoSeleccionado != null
                    ) {
                        when {
                            cargandoEmpleados -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            empleados.isEmpty() -> {
                                Text(
                                    "No hay empleados disponibles para estos servicios en esta fecha",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                Column {
                                    empleados.forEach { empleado ->
                                        TarjetaEmpleadoNuevaCita(
                                            empleado = empleado,
                                            seleccionado = empleadoSeleccionado == empleado,
                                            alClick = { empleadoSeleccionado = empleado }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if (empleadoSeleccionado != null) {
                item {
                    SeccionFormularioNuevaCita(
                        titulo = "5. Horario",
                        icono = Icons.Default.Schedule,
                        completado = horarioSeleccionado != null
                    ) {
                        when {
                            cargandoHorarios -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            horarios.isEmpty() -> {
                                Text(
                                    "No hay horarios disponibles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                Column {
                                    horarios.chunked(3).forEach { fila ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            fila.forEach { horario ->
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TarjetaHorarioNuevaCita(
                                                        horario = horario,
                                                        seleccionado = horarioSeleccionado == horario,
                                                        alClick = {
                                                            if (horario.disponible) {
                                                                horarioSeleccionado = horario
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            repeat(3 - fila.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }


            item {
                SeccionFormularioNuevaCita(
                    titulo = "6. Notas (Opcional)",
                    icono = Icons.Default.Notes,
                    completado = false
                ) {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Agregar notas o comentarios...") },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }


            item {
                Button(
                    onClick = {
                        if (clienteSeleccionado == null || serviciosSeleccionados.isEmpty() ||
                            empleadoSeleccionado == null || fechaSeleccionada.isEmpty() ||
                            horarioSeleccionado == null) {
                            Toast.makeText(
                                contexto,
                                "Por favor completa todos los campos obligatorios",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        cargando = true


                        val serviciosSolicitud = serviciosSeleccionados.mapIndexed { index, servicio ->
                            ServicioSolicitud(
                                idEmpserv = 1,
                                orden = index + 1,
                                duracion = servicio.duracion
                            )
                        }

                        val solicitud = CitaSolicitud(
                            idCliente = clienteSeleccionado!!.idCliente,
                            idEmpleado = empleadoSeleccionado!!.idEmpleado,
                            fecha = fechaSeleccionada,
                            horaInicio = horarioSeleccionado!!.horaInicio,
                            horaFin = horarioSeleccionado!!.horaFin,
                            servicios = serviciosSolicitud,
                            notas = notas.ifBlank { null }
                        )

                        android.util.Log.d("NUEVA_CITA", "Enviando solicitud: $solicitud")

                        RetrofitClient.api.crearCita(solicitud).enqueue(object : Callback<RespuestaApi<CitaResponse>> {
                            override fun onResponse(
                                call: Call<RespuestaApi<CitaResponse>>,
                                response: Response<RespuestaApi<CitaResponse>>
                            ) {
                                cargando = false
                                android.util.Log.d("NUEVA_CITA", "Respuesta: ${response.code()}")
                                android.util.Log.d("NUEVA_CITA", "Body: ${response.body()}")

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(
                                        contexto,
                                        "Cita agendada exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    alVolver()
                                } else {
                                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Error desconocido"
                                    Toast.makeText(
                                        contexto,
                                        "Error al agendar cita: $errorMsg",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<RespuestaApi<CitaResponse>>, t: Throwable) {
                                cargando = false
                                android.util.Log.e("NUEVA_CITA", "Error: ${t.message}", t)
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
                    enabled = !cargando &&
                            clienteSeleccionado != null &&
                            serviciosSeleccionados.isNotEmpty() &&
                            empleadoSeleccionado != null &&
                            fechaSeleccionada.isNotEmpty() &&
                            horarioSeleccionado != null
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agendar Cita")
                    }
                }
            }
        }
    }

    if (mostrarSelectorCliente) {
        DialogoSelectorClienteNuevaCita(
            clientes = clientes,
            alSeleccionar = { cliente ->
                clienteSeleccionado = cliente
                mostrarSelectorCliente = false
            },
            alCerrar = { mostrarSelectorCliente = false }
        )
    }

    if (mostrarDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        fechaSeleccionada = formato.format(date)
                    }
                    mostrarDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SeccionFormularioNuevaCita(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    completado: Boolean,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = if (completado) ColorAcento else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (completado) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completado",
                        tint = ColorAcento,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
fun ResumenCitaNueva(
    cliente: ClienteResponse?,
    servicios: List<ServicioResponse>,
    empleado: EmpleadoResponse?,
    fecha: String,
    horario: SlotHorario?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ColorSecundario
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = ColorAcento
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Resumen de la Cita",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ItemResumenNuevaCita("Cliente", cliente?.nombre ?: "-")
            ItemResumenNuevaCita("Fecha", fecha.ifEmpty { "-" })
            ItemResumenNuevaCita("Horario", horario?.let { "${it.horaInicio.substring(0,5)} - ${it.horaFin.substring(0,5)}" } ?: "-")
            ItemResumenNuevaCita("Empleado", empleado?.nombre ?: "-")
            ItemResumenNuevaCita(
                "Servicios",
                if (servicios.isEmpty()) "-" else servicios.joinToString(", ") { it.nombre }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Duración Total:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${servicios.sumOf { it.duracion }} min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$${servicios.sumOf { it.precio }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorAcento
                )
            }
        }
    }
}

@Composable
fun ItemResumenNuevaCita(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$etiqueta:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TarjetaClienteSeleccionadoNuevaCita(
    cliente: ClienteResponse,
    alCambiar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    cliente.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    cliente.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    cliente.telefono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = alCambiar) {
                Icon(Icons.Default.Edit, "Cambiar")
            }
        }
    }
}

@Composable
fun TarjetaServicioNuevaCita(
    servicio: ServicioResponse,
    seleccionado: Boolean,
    alClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alClick),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) ColorAcento.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(2.dp, ColorAcento) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    servicio.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${servicio.duracion} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$${servicio.precio}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ColorAcento
            )
        }
    }
}

@Composable
fun TarjetaEmpleadoNuevaCita(
    empleado: EmpleadoResponse,
    seleccionado: Boolean,
    alClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alClick),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) ColorAcento.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(2.dp, ColorAcento) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(ColorAcento),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    obtenerInicialesEmpleado(empleado.nombre),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    empleado.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                empleado.cargo?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaHorarioNuevaCita(
    horario: SlotHorario,
    seleccionado: Boolean,
    alClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = horario.disponible, onClick = alClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                seleccionado -> ColorAcento
                horario.disponible -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(2.dp, ColorAcento) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                horario.horaInicio.substring(0, 5),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    seleccionado -> Color.White
                    horario.disponible -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun DialogoSelectorClienteNuevaCita(
    clientes: List<ClienteResponse>,
    alSeleccionar: (ClienteResponse) -> Unit,
    alCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Seleccionar Cliente") },
        text = {
            LazyColumn {
                items(clientes) { cliente ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { alSeleccionar(cliente) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                cliente.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                cliente.correo,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                cliente.telefono,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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

fun obtenerInicialesEmpleado(nombre: String): String {
    return nombre.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
}

fun cargarClientesNuevaCita(alCompletar: (List<ClienteResponse>) -> Unit) {
    RetrofitClient.api.obtenerClientes().enqueue(object : Callback<RespuestaApi<List<ClienteResponse>>> {
        override fun onResponse(
            call: Call<RespuestaApi<List<ClienteResponse>>>,
            response: Response<RespuestaApi<List<ClienteResponse>>>
        ) {
            if (response.isSuccessful && response.body()?.success == true) {
                alCompletar(response.body()?.data ?: emptyList())
            } else {
                alCompletar(emptyList())
            }
        }
        override fun onFailure(call: Call<RespuestaApi<List<ClienteResponse>>>, t: Throwable) {
            alCompletar(emptyList())
        }
    })
}

fun cargarServiciosNuevaCita(alCompletar: (List<ServicioResponse>) -> Unit) {
    RetrofitClient.api.obtenerServicios().enqueue(object : Callback<RespuestaApi<List<ServicioResponse>>> {
        override fun onResponse(
            call: Call<RespuestaApi<List<ServicioResponse>>>,
            response: Response<RespuestaApi<List<ServicioResponse>>>
        ) {
            if (response.isSuccessful && response.body()?.success == true) {
                alCompletar(response.body()?.data ?: emptyList())
            } else {
                alCompletar(emptyList())
            }
        }
        override fun onFailure(call: Call<RespuestaApi<List<ServicioResponse>>>, t: Throwable) {
            alCompletar(emptyList())
        }
    })
}

fun cargarEmpleadosDisponiblesNuevaCita(
    servicios: List<Long>,
    fecha: String,
    alCompletar: (List<EmpleadoResponse>) -> Unit
) {
    try {
        android.util.Log.d("EMPLEADOS_DEBUG", "==== BUSCANDO EMPLEADOS ====")
        android.util.Log.d("EMPLEADOS_DEBUG", "Servicios: $servicios")
        android.util.Log.d("EMPLEADOS_DEBUG", "Fecha: $fecha")

        val solicitud = EmpleadosDisponiblesRequest(
            servicios = servicios,
            fecha = fecha
        )

        RetrofitClient.api.obtenerEmpleadosDisponibles(solicitud).enqueue(
            object : Callback<RespuestaApi<List<EmpleadoResponse>>> {
                override fun onResponse(
                    call: Call<RespuestaApi<List<EmpleadoResponse>>>,
                    response: Response<RespuestaApi<List<EmpleadoResponse>>>
                ) {
                    try {
                        android.util.Log.d("EMPLEADOS_DEBUG", "Código respuesta: ${response.code()}")
                        android.util.Log.d("EMPLEADOS_DEBUG", "Exitoso: ${response.isSuccessful}")

                        if (response.isSuccessful) {
                            val body = response.body()
                            android.util.Log.d("EMPLEADOS_DEBUG", "Body success: ${body?.success}")
                            android.util.Log.d("EMPLEADOS_DEBUG", "Body data: ${body?.data}")

                            if (body?.success == true) {
                                val empleados = body.data ?: emptyList()
                                android.util.Log.d("EMPLEADOS_DEBUG", "Empleados encontrados: ${empleados.size}")
                                empleados.forEach { emp ->
                                    android.util.Log.d("EMPLEADOS_DEBUG", "  - ${emp.nombre} (ID: ${emp.idEmpleado})")
                                }
                                alCompletar(empleados)
                            } else {
                                android.util.Log.w("EMPLEADOS_DEBUG", "Success = false: ${body?.message}")
                                alCompletar(emptyList())
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("EMPLEADOS_DEBUG", "Error HTTP: $errorBody")
                            alCompletar(emptyList())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("EMPLEADOS_DEBUG", "Error procesando respuesta: ${e.message}", e)
                        alCompletar(emptyList())
                    }
                }

                override fun onFailure(
                    call: Call<RespuestaApi<List<EmpleadoResponse>>>,
                    t: Throwable
                ) {
                    android.util.Log.e("EMPLEADOS_DEBUG", "Fallo de conexión: ${t.message}", t)
                    alCompletar(emptyList())
                }
            }
        )
    } catch (e: Exception) {
        android.util.Log.e("EMPLEADOS_DEBUG", "Error al crear llamada: ${e.message}", e)
        alCompletar(emptyList())
    }
}

fun cargarHorariosDisponiblesNuevaCita(
    idEmpleado: Long,
    fecha: String,
    duracionTotal: Int,
    alCompletar: (List<SlotHorario>) -> Unit
) {
    try {
        android.util.Log.d("HORARIOS_DEBUG", "==== BUSCANDO HORARIOS ====")
        android.util.Log.d("HORARIOS_DEBUG", "Empleado: $idEmpleado")
        android.util.Log.d("HORARIOS_DEBUG", "Fecha: $fecha")
        android.util.Log.d("HORARIOS_DEBUG", "Duración: $duracionTotal")

        val solicitud = HorariosDisponiblesRequest(
            idEmpleado = idEmpleado,
            fecha = fecha,
            duracionTotal = duracionTotal
        )

        RetrofitClient.api.obtenerHorariosDisponibles(solicitud).enqueue(
            object : Callback<RespuestaApi<HorariosResponse>> {
                override fun onResponse(
                    call: Call<RespuestaApi<HorariosResponse>>,
                    response: Response<RespuestaApi<HorariosResponse>>
                ) {
                    try {
                        android.util.Log.d("HORARIOS_DEBUG", "Código respuesta: ${response.code()}")
                        android.util.Log.d("HORARIOS_DEBUG", "Exitoso: ${response.isSuccessful}")

                        if (response.isSuccessful && response.body()?.success == true) {
                            val horariosResponse = response.body()?.data
                            val horarios = horariosResponse?.slots ?: emptyList()
                            android.util.Log.d("HORARIOS_DEBUG", "Horarios encontrados: ${horarios.size}")
                            alCompletar(horarios)
                        } else {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("HORARIOS_DEBUG", " Error: $errorBody")
                            alCompletar(emptyList())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HORARIOS_DEBUG", "Error procesando: ${e.message}", e)
                        alCompletar(emptyList())
                    }
                }

                override fun onFailure(
                    call: Call<RespuestaApi<HorariosResponse>>,
                    t: Throwable
                ) {
                    android.util.Log.e("HORARIOS_DEBUG", "Fallo: ${t.message}", t)
                    alCompletar(emptyList())
                }
            }
        )
    } catch (e: Exception) {
        android.util.Log.e("HORARIOS_DEBUG", "Error: ${e.message}", e)
        alCompletar(emptyList())
    }
}