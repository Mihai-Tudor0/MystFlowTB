package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mystflowtb.ui.screens.*
import com.example.mystflowtb.ui.theme.MystFlowTBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MystFlowTBTheme {
                var currentScreen by remember { mutableStateOf("LOGIN") }
                var authMethod by remember { mutableStateOf("PIN") } // PIN sau Pattern

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF00382B)) {
                    when (currentScreen) {
                        "LOGIN" -> {
                            LoginScreen(
                                authMethod = authMethod,
                                onNavigateToSignUp = { currentScreen = "SIGNUP" },
                                onLoginSuccess = { currentScreen = "HOME" }
                            )
                        }
                        "SIGNUP" -> SignUpScreen(
                            onMethodSelected = { metoda ->
                                authMethod = metoda
                                currentScreen = "SETUP" // Pasul următor: Setarea efectivă
                            }
                        )
                        "SETUP" -> SetupScreen(
                            method = authMethod, // Aceasta este variabila care zice "PIN" sau "Pattern"
                            onFinished = { codSalvat ->
                                // Aici poți salva 'codSalvat' dacă vrei, apoi mergi la Login
                                currentScreen = "LOGIN"
                            }
                        )
                        "HOME" -> HomeScreen()
                    }
                }
            }
        }
    }
}