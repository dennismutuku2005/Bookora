package com.dennis.bookora.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Login to continue sharing and discovering books.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible.value) "Hide password" else "Show password")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgotPassword() },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Don't have an account? Create Account",
                modifier = Modifier.clickable { onRegister() },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
