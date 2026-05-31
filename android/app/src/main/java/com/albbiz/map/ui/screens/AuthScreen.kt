// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.viewmodel.AuthUiState
import com.albbiz.map.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(
                    context,
                    if (isLoginMode) strings.welcomeBack else strings.signUp,
                    Toast.LENGTH_SHORT
                ).show()
                onAuthSuccess()
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as AuthUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalAppStrings.current.appName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isLoginMode) LocalAppStrings.current.welcomeBack else LocalAppStrings.current.signUp,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isLoginMode) LocalAppStrings.current.signInToContinue else LocalAppStrings.current.signUpToGetStarted,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(LocalAppStrings.current.email) },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(LocalAppStrings.current.password) },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                ),
                singleLine = true
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(LocalAppStrings.current.confirmPassword) },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> {
                            Toast.makeText(context, strings.emailRequired, Toast.LENGTH_SHORT).show()
                        }
                        password.isBlank() -> {
                            Toast.makeText(context, strings.passwordRequired, Toast.LENGTH_SHORT).show()
                        }
                        !isLoginMode && password != confirmPassword -> {
                            Toast.makeText(context, strings.passwordsDoNotMatch, Toast.LENGTH_SHORT).show()
                        }
                        !isLoginMode && password.length < 6 -> {
                            Toast.makeText(context, strings.passwordTooShort, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.register(email, password)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoginMode) LocalAppStrings.current.signIn else LocalAppStrings.current.signUp)
            }

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    viewModel.resetState()
                }
            ) {
                Text(
                    if (isLoginMode) LocalAppStrings.current.noAccount
                    else LocalAppStrings.current.haveAccount)
            }
        }
    }
}