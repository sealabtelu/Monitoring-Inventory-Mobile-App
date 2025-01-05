package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.data.model.DataBarang
import com.example.inventorymonitoring.ui.theme.Purple40
import com.example.inventorymonitoring.ui.theme.Purple80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    itemId: String?,
    navController: NavController,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreRepository = remember { ServiceLocator.firestoreRepository }
    var item by remember { mutableStateOf<DataBarang?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Form state
    var namaBarang by remember { mutableStateOf("") }
    var kodeBarang by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf("") }
    var nomorRak by remember { mutableStateOf("") }

    // Load existing item data if editing
    LaunchedEffect(itemId) {
        if (itemId != null) {
            try {
                firestoreRepository.getBarangById(itemId)
                    .collect { fetchedItem ->
                        fetchedItem?.let {
                            item = it
                            namaBarang = it.nama_barang
                            kodeBarang = it.kode_barang
                            jenis = it.jenis
                            nomorRak = it.nomor_rak
                        }
                        isLoading = false
                    }
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6B35E8),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Barang Baru Terdeteksi",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(24.dp)
                ) {
                    // Nama Barang
                    Text(
                        text = "Nama Barang",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = namaBarang,
                        onValueChange = { namaBarang = it },
                        placeholder = { Text("nama_barang") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF9747FF)
                        )
                    )

                    // Kode Barang
                    Text(
                        text = "Kode Barang",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = kodeBarang,
                        onValueChange = { kodeBarang = it },
                        placeholder = { Text("kode_barang") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF9747FF)
                        )
                    )

                    // Jenis and Ruangan in a row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Jenis
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jenis",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = jenis,
                                onValueChange = { jenis = it },
                                placeholder = { Text("jenis") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF9747FF)
                                )
                            )
                        }

                        // Ruangan
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ruangan",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = nomorRak,
                                onValueChange = { nomorRak = it },
                                placeholder = { Text("nomor_rak") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF9747FF)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Save Button
                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            if (namaBarang.isBlank() || kodeBarang.isBlank() ||
                                jenis.isBlank() || nomorRak.isBlank()
                            ) {
                                error = "Please fill all fields"
                                return@Button
                            }

                            val newItem = DataBarang(
                                id = itemId ?: "",
                                nama_barang = namaBarang,
                                kode_barang = kodeBarang,
                                jenis = jenis,
                                nomor_rak = nomorRak,
                                stok_awal = 0,
                                stok_sekarang = 1,
                                created_at = (item?.created_at ?: System.currentTimeMillis()).toString(),
                                updated_at = System.currentTimeMillis().toString()
                            )

                            coroutineScope.launch {
                                try {
                                    if (itemId != null) {
                                        firestoreRepository.updateBarang(newItem)
                                    } else {
                                        firestoreRepository.addBarang(newItem)
                                    }
                                    onSaveSuccess()
                                } catch (e: Exception) {
                                    error = e.message
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9747FF)
                        )
                    ) {
                        Text(
                            text = "Simpan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}