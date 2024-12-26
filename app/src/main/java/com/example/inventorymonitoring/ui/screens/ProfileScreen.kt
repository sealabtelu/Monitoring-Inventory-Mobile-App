package com.example.inventorymonitoring.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventorymonitoring.R
import com.example.inventorymonitoring.data.ServiceLocator
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onEditButtonClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { ServiceLocator.provideAuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var userName by remember { mutableStateOf<String?>(null) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            val user = authRepository.getCurrentUser()
            userName = user?.username
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B35E8),
                            Color(0xFF8B5CF6)
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
                        text = "Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            onNotificationClick()
                        },
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
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Text(
                            text = userName ?: "Loading username...",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 0.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = { onEditButtonClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B35E8)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text("Edit Profile")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isDarkModeEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF3A0F8A),
                                    checkedTrackColor = Color(0xFFBAA8E7),
                                    uncheckedThumbColor = Color(0xFF6B35E8),
                                    uncheckedTrackColor = Color(0xFFBAA8E7)
                                ),
                                onCheckedChange = { isChecked ->
                                    isDarkModeEnabled = isChecked
                                    // Add logic to apply the theme, e.g., update a ViewModel or a settings repository
                                }
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                authRepository.signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEE2626)
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Logout")
        }
    }
}

