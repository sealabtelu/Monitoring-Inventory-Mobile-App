package com.example.inventorymonitoring.ui.screens

import android.content.Context
import android.os.Build
import android.widget.Space
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventorymonitoring.data.ServiceLocator
import com.example.inventorymonitoring.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    context: Context,
    onClickBack: () -> Unit,
    onUpdateCredentials: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userEmail by remember { mutableStateOf<String?>("") }
    var userName by remember { mutableStateOf<String?>("") }
    var password by remember { mutableStateOf("") }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentTime = LocalDateTime.now().format(formatter)
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val authRepository = remember { ServiceLocator.provideAuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            val user = authRepository.getCurrentUser()
            userEmail = user?.email
            userName = user?.username
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "New Username:",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        OutlinedTextField(
            value = userName ?: "Loading username...",
            onValueChange = { userName = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "New Email:",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        OutlinedTextField(
            value = userEmail ?: "Loading email...",
            onValueChange = { userEmail = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "New Password:",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {onClickBack()},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B35E8)
                )
               ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.weight(0.2f))

            Button(onClick = {
                coroutineScope.launch {
                    isLoading = true
                    error = null
                    val result = authRepository.updateUserCredentials(userName, userEmail, password, currentTime)
                    isLoading = false
                    result.fold(
                        onSuccess = { onUpdateCredentials() },
                        onFailure = { error = it.message }
                    )
                }
            },
                enabled = !isLoading && userName?.isNotBlank() == true && userEmail?.isNotBlank() == true && password?.isNotBlank() == true,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B35E8)
                )
            ) {
                Text("Change")
            }
        }
    }


}
