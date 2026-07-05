// Bismillah Hir Rahman Nir Raheem



package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.R
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.viewmodel.AuthUiState
import com.albbiz.map.viewmodel.AuthViewModel
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.MeTontWhite
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.AppLanguage



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            //.background(Color(0xFFFFF8F0))
            .background(MaterialTheme.colorScheme.background)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── RED HEADER SECTION ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MeTontRed),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.metont_nobackgroundcolor),
                        contentDescription = "MeTont Logo",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MeTont",
                        color = MeTontWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Albanian Business Directory",
                        color = MeTontWhite.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            // ── WHITE CARD SECTION ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MeTontWhite)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = if (isLoginMode) strings.welcomeBack else strings.signUp,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = if (isLoginMode) strings.signInToContinue else strings.signUpToGetStarted,
                        fontSize = 14.sp,
                        color = MeTontGrey,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(strings.email) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                null,
                                tint = MeTontRed
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(strings.password) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                null,
                                tint = MeTontRed
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null,
                                    tint = MeTontGrey
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                        ),
                        singleLine = true
                    )

                    // Confirm Password field
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(strings.confirmPassword) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    null,
                                    tint = MeTontRed
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeTontRed,
                                focusedLabelColor = MeTontRed,
                                cursorColor = MeTontRed
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign In/Up Button
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
                                    if (isLoginMode) viewModel.login(email, password, isAlbanian = currentLanguage == AppLanguage.SQ)
                                    else viewModel.register(email, password, isAlbanian = currentLanguage == AppLanguage.SQ
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeTontRed,
                            contentColor = MeTontWhite
                        ),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MeTontWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (isLoginMode) strings.signIn else strings.signUp,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onLanguageChange(AppLanguage.EN) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (currentLanguage == AppLanguage.EN)
                                    MeTontRed else MeTontGrey
                            )
                        ) {
                            Text(
                                "🇬🇧 EN",
                                fontWeight = if (currentLanguage == AppLanguage.EN)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text("|", color = MeTontGrey)
                        TextButton(
                            onClick = { onLanguageChange(AppLanguage.SQ) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (currentLanguage == AppLanguage.SQ)
                                    MeTontRed else MeTontGrey
                            )
                        ) {
                            Text(
                                "🇦🇱 SQ",
                                fontWeight = if (currentLanguage == AppLanguage.SQ)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Toggle login/register
                    TextButton(
                        onClick = {
                            isLoginMode = !isLoginMode
                            viewModel.resetState()
                        }
                    ) {
                        Text(
                            if (isLoginMode) strings.noAccount else strings.haveAccount,
                            color = MeTontRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


