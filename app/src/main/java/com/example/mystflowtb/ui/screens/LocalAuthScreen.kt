package com.example.mystflowtb.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.mystflowtb.ui.viewmodel.AuthUiState
import com.example.mystflowtb.ui.viewmodel.AuthViewModel

@Composable
fun LocalAuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)
    val context = LocalContext.current

    val authState by authViewModel.authState.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    
    // Auto-prompt biometrics when screen loads
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = { onAuthSuccess() },
                onError = {
                    // Show PIN fallback UI if biometrics fail or cancel
                    Toast.makeText(context, "Biometric failed/cancelled. Use PIN.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // React to PIN verification success
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            authViewModel.resetState()
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. TITLU ---
        Text(
            text = "Welcome back",
            color = roseGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )

        // ================= ECRAN PIN FALLBACK =================
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Error message
            if (authState is AuthUiState.Error) {
                Text(
                    text = (authState as AuthUiState.Error).message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Indicatori 6 puncte
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(2.dp, roseGold, CircleShape)
                            .background(if (index < pinInput.length) roseGold else Color.Transparent, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Tastatura numerică
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "delete")
            )

            keypad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { char ->
                        IconButton(
                            onClick = {
                                when (char) {
                                    "delete" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                    "C" -> pinInput = ""
                                    else -> if (pinInput.length < 6) {
                                        pinInput += char
                                        if (pinInput.length == 6) {
                                            // Trigger PIN verify via ViewModel
                                            authViewModel.verifyLocalPin(pinInput)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(70.dp)
                        ) {
                            if (char == "delete") Icon(Icons.Default.Delete, null, tint = roseGold)
                            else if (char == "C") Text("C", color = roseGold, fontSize = 24.sp)
                            else Text(text = char, color = Color.White, fontSize = 28.sp)
                        }
                    }
                }
            }
        }

        // --- 3. JOS: RETRY AMPRENTĂ ȘI LOGOUT ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { 
                val activity = context as? FragmentActivity
                if (activity != null) {
                    showBiometricPrompt(activity, { onAuthSuccess() }, {})
                }
            }) {
                Icon(Icons.Default.Fingerprint, null, tint = roseGold, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onLogout) {
                Text("Log out completely", color = roseGold.copy(0.8f))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Authentication failed")
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MystFlow")
                .setSubtitle("Use your biometric credential to unlock the app")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> onError("Biometric not available")
    }
}
