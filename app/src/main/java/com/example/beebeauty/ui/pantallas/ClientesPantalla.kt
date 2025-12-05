package com.example.beebeauty.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.beebeauty.data.modelos.Cliente
import com.example.beebeauty.data.modelos.*
import com.example.beebeauty.network.RetrofitClient
import com.example.beebeauty.ui.theme.ColorAcento
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesPantalla(
    alVolver: () -> Unit
) {
    val contexto = LocalContext.current

    var clientes by remember { mutableStateOf<List<ClienteResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var clienteEditar by remember { mutableStateOf<ClienteResponse?>(null) }
    var busqueda by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        cargarListaClientes { listaClientes ->
            clientes = listaClientes
            cargando = false
        }
    }


    val clientesFiltrados = remember(clientes, busqueda) {
        if (busqueda.isBlank()) {
            clientes
        } else {
            clientes.filter {
                it.nombre.contains(busqueda, ignoreCase = true) ||
                        it.correo.contains(busqueda, ignoreCase = true) ||
                        it.telefono.contains(busqueda, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoNuevo = true },
                containerColor = ColorAcento
            ) {
                Icon(Icons.Default.Add, "Nuevo Cliente", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (clientesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (busqueda.isBlank()) "No hay clientes registrados"
                            else "No se encontraron clientes",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clientesFiltrados) { cliente ->
                        TarjetaCliente(
                            cliente = cliente,
                            alEditar = {
                                clienteEditar = cliente
                                mostrarDialogoEditar = true
                            },
                            alEliminar = {
                                eliminarCliente(cliente.idCliente) { exito ->
                                    if (exito) {
                                        Toast.makeText(
                                            contexto,
                                            "Cliente eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        cargarListaClientes { clientes = it }
                                    } else {
                                        Toast.makeText(
                                            contexto,
                                            "Error al eliminar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    if (mostrarDialogoNuevo) {
        DialogoClienteFormulario(
            titulo = "Nuevo Cliente",
            cliente = null,
            alGuardar = { clienteRequest ->
                crearCliente(clienteRequest) { exito ->
                    if (exito) {
                        Toast.makeText(
                            contexto,
                            "Cliente creado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        cargarListaClientes { clientes = it }
                        mostrarDialogoNuevo = false
                    } else {
                        Toast.makeText(
                            contexto,
                            "Error al crear cliente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            alCerrar = { mostrarDialogoNuevo = false }
        )
    }


    if (mostrarDialogoEditar && clienteEditar != null) {
        DialogoClienteFormulario(
            titulo = "Editar Cliente",
            cliente = clienteEditar,
            alGuardar = { clienteRequest ->
                actualizarCliente(clienteEditar!!.idCliente, clienteRequest) { exito ->
                    if (exito) {
                        Toast.makeText(
                            contexto,
                            "Cliente actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                        cargarListaClientes { clientes = it }
                        mostrarDialogoEditar = false
                    } else {
                        Toast.makeText(
                            contexto,
                            "Error al actualizar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            alCerrar = { mostrarDialogoEditar = false }
        )
    }
}

@Composable
fun TarjetaCliente(
    cliente: ClienteResponse,
    alEditar: () -> Unit,
    alEliminar: () -> Unit
) {
    var mostrarMenuOpciones by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cliente.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            cliente.correo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            cliente.telefono,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }


                    if (cliente.numeroReservaciones != null && cliente.numeroReservaciones > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Citas: ${cliente.numeroReservaciones}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorAcento,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                Box {
                    IconButton(onClick = { mostrarMenuOpciones = true }) {
                        Icon(Icons.Default.MoreVert, "Opciones")
                    }

                    DropdownMenu(
                        expanded = mostrarMenuOpciones,
                        onDismissRequest = { mostrarMenuOpciones = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                mostrarMenuOpciones = false
                                alEditar()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, "Editar") }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                mostrarMenuOpciones = false
                                alEliminar()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, "Eliminar") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoClienteFormulario(
    titulo: String,
    cliente: ClienteResponse?,
    alGuardar: (ClienteRequest) -> Unit,
    alCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(cliente?.nombre ?: "") }
    var correo by remember { mutableStateOf(cliente?.correo ?: "") }
    var telefono by remember { mutableStateOf(cliente?.telefono ?: "") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun validarFormulario(): Boolean {
        val nuevosErrores = mutableMapOf<String, String>()

        if (nombre.isBlank()) nuevosErrores["nombre"] = "El nombre es obligatorio"
        if (correo.isBlank()) nuevosErrores["correo"] = "El correo es obligatorio"
        else if (!correo.contains("@")) nuevosErrores["correo"] = "Correo inválido"
        if (telefono.isBlank()) nuevosErrores["telefono"] = "El teléfono es obligatorio"
        else if (telefono.length < 10) nuevosErrores["telefono"] = "Teléfono debe tener al menos 10 dígitos"

        errores = nuevosErrores
        return nuevosErrores.isEmpty()
    }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errores.containsKey("nombre"),
                    supportingText = errores["nombre"]?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = errores.containsKey("correo"),
                    supportingText = errores["correo"]?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = errores.containsKey("telefono"),
                    supportingText = errores["telefono"]?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validarFormulario()) {
                        val clienteRequest = ClienteRequest(
                            nombre = nombre.trim(),
                            correo = correo.trim(),
                            telefono = telefono.trim()
                        )
                        alGuardar(clienteRequest)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text("Cancelar")
            }
        }
    )
}


fun cargarListaClientes(alCompletar: (List<ClienteResponse>) -> Unit) {
    android.util.Log.d("CLIENTES_DEBUG", "==== CARGANDO CLIENTES ====")

    RetrofitClient.api.obtenerClientes().enqueue(object : Callback<RespuestaApi<List<ClienteResponse>>> {
        override fun onResponse(
            call: Call<RespuestaApi<List<ClienteResponse>>>,
            response: Response<RespuestaApi<List<ClienteResponse>>>
        ) {
            android.util.Log.d("CLIENTES_DEBUG", "Código: ${response.code()}")

            if (response.isSuccessful && response.body()?.success == true) {
                val clientes = response.body()?.data ?: emptyList()
                android.util.Log.d("CLIENTES_DEBUG", "Clientes recibidos: ${clientes.size}")
                alCompletar(clientes)
            } else {
                android.util.Log.e("CLIENTES_DEBUG", "Error: ${response.errorBody()?.string()}")
                alCompletar(emptyList())
            }
        }

        override fun onFailure(call: Call<RespuestaApi<List<ClienteResponse>>>, t: Throwable) {
            android.util.Log.e("CLIENTES_DEBUG", "Fallo: ${t.message}", t)
            alCompletar(emptyList())
        }
    })
}

fun crearCliente(clienteRequest: ClienteRequest, alCompletar: (Boolean) -> Unit) {
    RetrofitClient.api.crearCliente(clienteRequest).enqueue(object : Callback<RespuestaApi<ClienteResponse>> {
        override fun onResponse(
            call: Call<RespuestaApi<ClienteResponse>>,
            response: Response<RespuestaApi<ClienteResponse>>
        ) {
            alCompletar(response.isSuccessful && response.body()?.success == true)
        }

        override fun onFailure(call: Call<RespuestaApi<ClienteResponse>>, t: Throwable) {
            alCompletar(false)
        }
    })
}

fun actualizarCliente(id: Long, clienteRequest: ClienteRequest, alCompletar: (Boolean) -> Unit) {
    RetrofitClient.api.actualizarCliente(id, clienteRequest).enqueue(object : Callback<RespuestaApi<ClienteResponse>> {
        override fun onResponse(
            call: Call<RespuestaApi<ClienteResponse>>,
            response: Response<RespuestaApi<ClienteResponse>>
        ) {
            alCompletar(response.isSuccessful && response.body()?.success == true)
        }

        override fun onFailure(call: Call<RespuestaApi<ClienteResponse>>, t: Throwable) {
            alCompletar(false)
        }
    })
}

fun eliminarCliente(id: Long, alCompletar: (Boolean) -> Unit) {
    RetrofitClient.api.eliminarCliente(id).enqueue(object : Callback<RespuestaApi<String>> {
        override fun onResponse(
            call: Call<RespuestaApi<String>>,
            response: Response<RespuestaApi<String>>
        ) {
            alCompletar(response.isSuccessful && response.body()?.success == true)
        }

        override fun onFailure(call: Call<RespuestaApi<String>>, t: Throwable) {
            alCompletar(false)
        }
    })
}