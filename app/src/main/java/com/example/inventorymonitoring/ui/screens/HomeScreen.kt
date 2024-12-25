package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                recentActivity = activities.take(5) // Limit to the latest 5 activities
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { /* Handle notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Recent Activity Section
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                } else if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(recentActivity) { activity ->
                            ActivityCard(
                                itemName = activity.namaBarang,
                                location = activity.nomorRak,
                                action = activity.status // Use status for action
                            )
                        }
                    }
                }
            }

            // Rooms Section
            item {
                Text(
                    text = "Rooms",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                } else if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Display room cards based on unique nomor_rak
                    uniqueNomorRak.forEach { nomorRak ->
                        RoomCard(
                            roomName = "Room $nomorRak",
                            description = "Description for Room $nomorRak",
                            onViewClick = { /* Handle room view */ }
                        )
                    }
                }
            }

            // Add New Item Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add new item",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Track Item Locations and count",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = onAddNewItem,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Item")
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
    action: String
) {
    Card(
        modifier = Modifier
            .padding(end = 8.dp)
            .width(200.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { /* Handle action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (action == "Move In")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text(action)
            }
        }
    }
}

@Composable
fun RoomCard(
    roomName: String,
    description: String,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    text = roomName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onViewClick) {
                Text("View")
            }
        }
    }
}
