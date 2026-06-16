package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystflowtb.ui.screens.*
import com.example.mystflowtb.ui.theme.MystFlowTBTheme
import com.example.mystflowtb.ui.viewmodel.AuthViewModel
import com.example.mystflowtb.ui.viewmodel.AuthViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystflowtb.screens.SetupScreen
import com.example.mystflowtb.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MystFlowTBTheme {
                val factory = AuthViewModelFactory(this.applicationContext)
                val authViewModel: AuthViewModel = viewModel(factory = factory)

                // Navigation State
                var currentScreen by remember {
                    mutableStateOf(
                        if (authViewModel.hasActiveSessionAndLocalPin()) "LOCAL_AUTH" else "WELCOME"
                    )
                }

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
                                    authViewModel = authViewModel,
                                    onNavigateToSignUp = { currentScreen = "SIGNUP" },
                                    onLoginSuccess = { currentScreen = "HOME" },
                                    onSetupNeeded = { currentScreen = "SETUP" }
                                )
                            }
                            "SIGNUP" -> {
                                SignUpScreen(
                                    authViewModel = authViewModel,
                                    onRegistrationComplete = { currentScreen = "SETUP" }
                                )
                            }
                            "SETUP" -> {
                                SetupScreen(
                                    authViewModel = authViewModel,
                                    onFinished = {
                                        currentScreen = "HOME"
                                    }
                                )
                            }
                            "LOCAL_AUTH" -> {
                                LocalAuthScreen(
                                    authViewModel = authViewModel,
                                    onAuthSuccess = { currentScreen = "HOME" },
                                    onLogout = {
                                        authViewModel.signOut()
                                        currentScreen = "WELCOME"
                                    }
                                )

                            "HOME" -> HomeScreen(viewModel = aiViewModel)
                        }
                    }
                }
            }
        }
    }
}
    @Composable
    fun AiSecurityCard(viewModel: AiViewModel = viewModel(), userId: Int) {
        LaunchedEffect(userId) {
            viewModel.fetchInsight(userId)
        }


        Text(text = viewModel.insightMessage.value, color = Color.White)
    }