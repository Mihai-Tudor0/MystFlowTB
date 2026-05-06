package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.example.mystflowtb.LoginRequest
import androidx.compose.ui.unit.dp
import com.example.mystflowtb.ui.theme.MystFlowTBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MystFlowTBTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var isRegistering by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        isLoggedIn -> {
                            HomeScreen(onLogout = { isLoggedIn = false })
                        }

                        isRegistering -> {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    isRegistering = false
                                },
                                onBackToLogin = {
                                    isRegistering = false
                                }
                            )
                        }

                        else -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { isLoggedIn = true },
                                onGoToRegister = { isRegistering = true })
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LoginScreen(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit, onGoToRegister: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        var mesajServer by remember { mutableStateOf("") }
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "MystFlow Login", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = ApiClient.apiService.login(
                                LoginRequest(email = email, password = password)
                            )
                            mesajServer = response.message
                            if (response.status == "success") {
                                onLoginSuccess()
                            }
                        } catch (e: Exception) {
                            mesajServer = "Eroare: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Intră în cont")
            }
            TextButton(onClick = onGoToRegister) {
                Text("Nu ai cont? Creează unul nou")
            }
            if (mesajServer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = mesajServer, color = MaterialTheme.colorScheme.primary)
            }

        }
    }

    @Composable
    fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var mesajServer by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Creează un cont nou", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nume utilizator") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = ApiClient.apiService.register(
                                RegisterRequest(username, email, password)
                            )
                            mesajServer = response.message
                            if (response.status == "success") {
                                onRegisterSuccess()
                            }
                        } catch (e: Exception) {
                            mesajServer = "Eroare: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Înregistrează-te")
            }

            TextButton(onClick = onBackToLogin) {
                Text("Ai deja cont? Loghează-te aici")
            }

            if (mesajServer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = mesajServer, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    @Composable
    fun HomeScreen(onLogout: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Ai intrat în cont cu succes!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { onLogout() }) {
                Text("Ieși din cont")
            }
        }
    }
}