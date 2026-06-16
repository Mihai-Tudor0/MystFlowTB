package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.mystflowtb.ui.viewmodel.BankingViewModel
import com.example.mystflowtb.ui.viewmodel.BankingViewModelFactory

// Must extend FragmentActivity for AndroidX BiometricPrompt
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MystFlowTBTheme {
                val factory = AuthViewModelFactory(this.applicationContext)
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val aiViewModel: AiViewModel = viewModel()

                // BankingViewModel shares the same repository as AuthViewModel
                val bankingFactory = remember { BankingViewModelFactory(authViewModel.repository) }
                val bankingViewModel: BankingViewModel = viewModel(factory = bankingFactory)

                // Observe current user profile
                val currentUser by authViewModel.currentUser.collectAsState()

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
                            }
                            "HOME" -> {
                                // Refresh profile data every time we enter HomeScreen
                                LaunchedEffect(Unit) {
                                    authViewModel.loadCurrentProfile()
                                    bankingViewModel.refreshProfile()
                                    bankingViewModel.loadTransactions()
                                }

                                // Use the freshest profile from either source
                                val bankingProfile by bankingViewModel.currentProfile.collectAsState()
                                val displayProfile = bankingProfile ?: currentUser

                                HomeScreen(
                                    aiViewModel = aiViewModel,
                                    bankingViewModel = bankingViewModel,
                                    userProfile = displayProfile,
                                    onNavigateToTopUp = { currentScreen = "TOP_UP" },
                                    onNavigateToTransfer = { currentScreen = "TRANSFER" },
                                    onLogout = {
                                        authViewModel.signOut()
                                        currentScreen = "WELCOME"
                                    }
                                )
                            }
                            "TOP_UP" -> {
                                TopUpScreen(
                                    bankingViewModel = bankingViewModel,
                                    onBack = {
                                        // Refresh profile when coming back
                                        authViewModel.loadCurrentProfile()
                                        bankingViewModel.refreshProfile()
                                        currentScreen = "HOME"
                                    }
                                )
                            }
                            "TRANSFER" -> {
                                val bankingProfile by bankingViewModel.currentProfile.collectAsState()
                                val displayProfile = bankingProfile ?: currentUser

                                TransferScreen(
                                    bankingViewModel = bankingViewModel,
                                    userProfile = displayProfile,
                                    onBack = {
                                        // Refresh profile when coming back
                                        authViewModel.loadCurrentProfile()
                                        bankingViewModel.refreshProfile()
                                        currentScreen = "HOME"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}