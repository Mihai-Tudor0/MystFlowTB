package com.example.mystflowtb.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
fun SetupScreen(
    authViewModel: AuthViewModel,
    onFinished: () -> Unit
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var isPinSaved by remember { mutableStateOf(false) }

    // Once PIN is saved and state is Success, show Biometric Prompt
    LaunchedEffect(authState, isPinSaved) {
        if (authState is AuthUiState.Success && isPinSaved) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                showBiometricEnrollmentOrPrompt(
                    activity = activity,
                    onSuccess = { onFinished() },
                    onError = { 
                        // Even if biometrics fail/cancel, they set up the PIN, so they can proceed
                        Toast.makeText(context, "Biometric setup skipped/failed. You can use your PIN.", Toast.LENGTH_LONG).show()
                        onFinished() 
                    }
                )
            } else {
                onFinished() // Fallback if context is not FragmentActivity
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Set your 6-digit local PIN",
            color = roseGold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )
        
        Text(
            text = "This PIN is stored securely on your device as a backup to your fingerprint.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // ================= ECRAN PIN =================
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Vizualizare puncte PIN
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(2.dp, roseGold, CircleShape)
                            .background(if (index < pinInput.length) roseGold else Color.Transparent, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Tastatură Numerică
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "delete")
            )

            keypad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { char ->
                        IconButton(
                            onClick = {
                                when (char) {
                                    "delete" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                    "C" -> pinInput = ""
                                    else -> if (pinInput.length < 6) pinInput += char
                                }
                            },
                            modifier = Modifier.size(75.dp)
                        ) {
                            if (char == "delete") {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = roseGold)
                            } else {
                                Text(text = char, color = Color.White, fontSize = 28.sp)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                authViewModel.saveLocalPin(pinInput)
                isPinSaved = true
            },
            enabled = pinInput.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roseGold,
                disabledContainerColor = roseGold.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "SAVE PIN & SETUP FINGERPRINT",
                color = emeraldDeep,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private fun showBiometricEnrollmentOrPrompt(
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
                        // Just let the user try again
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login for MystFlow")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> onError("No biometric features available on this device.")
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> onError("Biometric features are currently unavailable.")
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> onError("The user hasn't associated any biometric credentials with their account.")
        else -> onError("Unknown biometric status")
    }
}