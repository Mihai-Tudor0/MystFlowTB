package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystflowtb.ui.viewmodel.AuthUiState
import com.example.mystflowtb.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onRegistrationComplete: () -> Unit // Navigates to SetupScreen for local PIN/Biometrics
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    val authState by authViewModel.authState.collectAsState()

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    // Navigate when OTP is verified and profile is created
    LaunchedEffect(authState) {
        if (authState is AuthUiState.OtpVerified) {
            authViewModel.resetState()
            onRegistrationComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (authState !is AuthUiState.OtpSent && authState !is AuthUiState.Loading && authState !is AuthUiState.OtpVerified) {
            // ================= STEP 1: Personal Details =================
            Text(
                text = "Create your account",
                color = roseGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors(roseGold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Surname
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors(roseGold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (e.g. +407...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors(roseGold)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Error message
            if (authState is AuthUiState.Error) {
                Text(
                    text = (authState as AuthUiState.Error).message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { authViewModel.sendOtp(phoneNumber) },
                enabled = name.isNotBlank() && surname.isNotBlank() && phoneNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = roseGold,
                    disabledContainerColor = roseGold.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("SEND SMS CODE", color = emeraldDeep, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            // ================= STEP 2: OTP Verification =================
            Text(
                text = "Verify your number",
                color = roseGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter the 6-digit code sent to $phoneNumber",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("SMS Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors(roseGold)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Error message
            if (authState is AuthUiState.Error) {
                Text(
                    text = (authState as AuthUiState.Error).message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    authViewModel.verifyOtp(
                        phoneNumber = phoneNumber,
                        otpCode = otpCode,
                        isSignUp = true,
                        name = name,
                        surname = surname
                    )
                },
                enabled = otpCode.length == 6 && authState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = roseGold,
                    disabledContainerColor = roseGold.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = emeraldDeep, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("VERIFY", color = emeraldDeep, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { authViewModel.resetState() }) {
                Text("Change phone number", color = roseGold)
            }
        }
    }
}

@Composable
fun defaultTextFieldColors(roseGold: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = roseGold,
    unfocusedBorderColor = roseGold.copy(alpha = 0.5f),
    focusedLabelColor = roseGold,
    unfocusedLabelColor = roseGold.copy(alpha = 0.6f),
    cursorColor = roseGold,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)