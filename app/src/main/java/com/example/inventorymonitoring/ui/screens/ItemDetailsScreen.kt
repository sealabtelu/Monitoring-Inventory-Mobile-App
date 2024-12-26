package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.data.model.DataBarang
import com.example.inventorymonitoring.data.repository.RecentActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var item by remember { mutableStateOf<DataBarang?>(null) }
    var logs by remember { mutableStateOf<List<RecentActivity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val firestoreRepository = remember { ServiceLocator.firestoreRepository }

    LaunchedEffect(Unit) {
        // Update umur and timestamp for all items
        firestoreRepository.updateUmurAndTimestamp()
            .catch { e ->
                error = e.message
                isLoading = false
            }
            .collectLatest { updatedItems ->
                // Optionally handle the updated items if needed
            }
    }

    LaunchedEffect(itemId) {
        firestoreRepository.getBarangById(itemId)
            .catch { e ->
                error = e.message
                isLoading = false
            }
            .collectLatest { fetchedItem ->
                item = fetchedItem
                isLoading = false
            }

        firestoreRepository.getRecentActivityForItem(itemId)
            .catch { e ->
                error = e.message
                isLoading = false
            }
            .collectLatest { fetchedLogs ->
                logs = fetchedLogs
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B35E8),
                            Color(0xFF8B5CF6)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Item Details",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    color = Color.White
                )
            } else if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                item?.let { currentItem ->
                    // Item Details
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 24.dp)) {
                            Text(
                                text = currentItem.nama_barang,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentItem.id,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }

                        DetailRow("Kode Barang", currentItem.kode_barang)
                        DetailRow("Stok", currentItem.stok_sekarang.toString())
                        DetailRow("Umur", "${currentItem.umur} hari")                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Item Logs Section
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "ITEM LOGS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(24.dp)
                            )

                            LazyColumn {
                                items(logs) { log ->
                                    LogItem(log)
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        color = Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun LogItem(log: RecentActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = log.timestamp,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Box(
            modifier = Modifier
                .background(
                    color = if (log.status == "Barang Masuk") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = log.status,
                color = if (log.status == "Barang Masuk") Color(0xFF4CAF50) else Color(0xFFE53935),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
