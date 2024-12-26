package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorymonitoring.R
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.data.model.DataBarang
import com.example.inventorymonitoring.data.repository.RecentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    var showDialog by remember { mutableStateOf(false) }
    var isLocking by remember { mutableStateOf(false) } // New state to track locking action
    var isProcessing by remember { mutableStateOf(false) } // New state to track processing

    val firestoreRepository = remember { ServiceLocator.firestoreRepository }

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
                    // Lock/Unlock Icon
                    item?.let { currentItem ->
                        IconButton(onClick = {
                            if (!isProcessing) { // Prevent multiple clicks
                                if (currentItem.terkunci == 1) {
                                    // Show dialog to confirm unlocking
                                    showDialog = true
                                } else {
                                    // Show dialog to confirm locking
                                    isLocking = true
                                    showDialog = true
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = if (currentItem.terkunci == 1) R.drawable.lock else R.drawable.unlock),
                                contentDescription = "Lock",
                                tint = Color.White
                            )
                        }
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
                        DetailRow("Umur Barang", currentItem.umur.toString()) // Convert to String
                    }

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

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isLocking) "Lock Item" else "Unlock Item") },
            text = { Text("Are you sure you want to ${if (isLocking) "lock" else "unlock"} this item?") },
            confirmButton = {
                TextButton(onClick = {
                    isProcessing = true // Set processing state
                    if (isLocking) {
                        // Lock the item
                        lockItem(item?.id ?: "") {
                            // Update the item state after locking
                            item = item?.copy(terkunci = 1)
                            isProcessing = false // Reset processing state
                        }
                    } else {
                        // Unlock the item
                        unlockItem(item?.id ?: "") {
                            // Update the item state after unlocking
                            item = item?.copy(terkunci = 0)
                            isProcessing = false // Reset processing state
                        }
                    }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

private fun unlockItem(itemId: String, onUnlock: () -> Unit) {
    val firestoreRepository = ServiceLocator.firestoreRepository

    // Launch a coroutine to fetch the current item and update the lock status
    CoroutineScope(Dispatchers.IO).launch {
        firestoreRepository.getBarangById(itemId).collectLatest { currentItem ->
            currentItem?.let {
                // Toggle the terkunci value
                val newStatus = if (it.terkunci == 1) 0 else 1
                firestoreRepository.updateItemLockStatus(itemId, newStatus)
                onUnlock() // Call the onUnlock callback to update the UI
            }
        }
    }
}

private fun lockItem(itemId: String, onLock: () -> Unit) {
    val firestoreRepository = ServiceLocator.firestoreRepository

    // Launch a coroutine to lock the item
    CoroutineScope(Dispatchers.IO).launch {
        firestoreRepository.updateItemLockStatus(itemId, 1) // Set terkunci to 1
        onLock() // Call the onLock callback to update the UI
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
