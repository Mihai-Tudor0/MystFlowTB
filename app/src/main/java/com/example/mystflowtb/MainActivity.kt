package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.mystflowtb.ui.screens.*
import com.example.mystflowtb.ui.theme.MystFlowTBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MystFlowTBTheme {
                var currentScreen by remember { mutableStateOf("WELCOME") }
                var authMethod by remember { mutableStateOf("PIN") } // PIN sau Pattern

                // Definim gradientul vertical premium (de la verde smarald mediu la verde foarte închis)
                val premiumGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF004D3B), // Sus: Verde Smarald luminos
                        Color(0xFF00382B), // Mijloc: Emerald Deep standard
                        Color(0xFF002119)  // Jos: Verde abis (aproape negru)
                    )
                )

                // Lăsăm Surface transparent pentru a permite Box-ului cu gradient să se vadă
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    // Box global care aplică fundalul cu gradient pe toate ecranele
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(premiumGradient)
                    ) {
                        when (currentScreen) {
                            "WELCOME" -> {
                                WelcomeScreen(
                                    onNextClicked = {
                                        currentScreen = "LOGIN"
                                    }
                                )
                            }
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
                                    currentScreen = "SETUP"
                                }
                            )
                            "SETUP" -> SetupScreen(
                                method = authMethod,
                                onFinished = { codSalvat ->
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
}