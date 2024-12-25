package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.data.repository.RecentActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (String) -> Unit,
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreRepository = remember { ServiceLocator.firestoreRepository }
    var recentActivity by remember { mutableStateOf<List<RecentActivity>>(emptyList()) }
    var uniqueNomorRak by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firestoreRepository) {
        firestoreRepository.getRecentActivityStream()
            .catch { e ->
                error = e.message
                isLoading = false
            }
            .collectLatest { activities ->
                recentActivity = activities.take(5)
                isLoading = false
            }
    }

    LaunchedEffect(firestoreRepository) {
        firestoreRepository.getUniqueNomorRak()
            .catch { e ->
                error = e.message
                isLoading = false
            }
            .collectLatest { nomorRakList ->
                uniqueNomorRak = nomorRakList
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background for header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B35E8), // Dark purple
                            Color(0xFF8B5CF6)  // Light purple
                        )
                    )
                )
        )

        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { /* Handle notifications */ },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
            }

            // White background content
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // Rooms Section
                        Text(
                            text = "Rooms",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )

                        if (uniqueNomorRak.isEmpty()) {
                            Text(
                                text = "No rooms available",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                uniqueNomorRak.forEach { nomorRak ->
                                    RoomCard(
                                        roomName = "Ruangan $nomorRak",
                                        description = "Description for Ruangan $nomorRak",
                                        onViewClick = { /* Handle room view */ },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Recent Activity Section
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            recentActivity.forEach { activity ->
                                ActivityCard(
                                    itemName = activity.namaBarang,
                                    location = activity.nomorRak,
                                    action = activity.status
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    itemName: String,
    location: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (action == "Barang Masuk") Color(0xFFE2F5E9) else Color(0xFFFFE9E9)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = action,
                    color = if (action == "Barang Masuk") Color(0xFF4CAF50) else Color(0xFFE53935),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RoomCard(
    roomName: String,
    description: String,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = roomName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onViewClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B35E8)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View", fontWeight = FontWeight.Bold)
            }
        }
    }
}
