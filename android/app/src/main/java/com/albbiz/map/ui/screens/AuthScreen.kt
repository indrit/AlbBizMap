// Bismillah Hir Rahman Nir Raheem



package com.albbiz.map.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.R
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.viewmodel.AuthUiState
import com.albbiz.map.viewmodel.AuthViewModel
import com.albbiz.map.viewmodel.PasswordResetUiState
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.MeTontWhite
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.AppLanguage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

// Web client ID from google-services.json's oauth_client (client_type 3) entry,
// generated once Google was enabled as a sign-in provider in Firebase Console.
// NOTE: this still won't work on a device until a SHA-1 fingerprint (debug and,
// later, release) is registered under Project Settings → your Android app —
// Google Play Services checks the calling app's signature against that before
// letting the native account picker complete.
private const val GOOGLE_WEB_CLIENT_ID =
    "626415932806-jfk7i16odsfg6pj4u2fd9ou568jpbqdb.apps.googleusercontent.com"

// Launches Android's Credential Manager (the system's native Google account
// picker) and returns a Google ID token on success, or null if the user
// cancelled or something went wrong. The token itself doesn't authenticate
// anything by itself — AuthViewModel.signInWithGoogle() hands it to Firebase,
// which verifies it and creates/logs into the matching Firebase Auth user.
private suspend fun requestGoogleIdToken(context: Context): String? {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            null
        }
    } catch (e: GetCredentialException) {
        // Includes user cancellation (tapped outside the picker), no Google
        // account on the device, or the request being misconfigured (e.g. the
        // TODO above not being filled in yet) — all surfaced as null here and
        // handled as a normal auth error by the caller.
        null
    }
}



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
    val passwordResetState by viewModel.passwordResetState.collectAsState()
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

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

    LaunchedEffect(passwordResetState) {
        when (passwordResetState) {
            is PasswordResetUiState.Success -> {
                Toast.makeText(context, strings.resetEmailSent, Toast.LENGTH_LONG).show()
                showForgotPasswordDialog = false
                viewModel.resetPasswordResetState()
            }
            is PasswordResetUiState.Error -> {
                Toast.makeText(
                    context,
                    (passwordResetState as PasswordResetUiState.Error).message,
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
                        // Tagged for the Baseline Profile benchmark test (UiAutomator) to
                        // find this field regardless of the active display language.
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("authEmailField"),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("authPasswordField"),
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

                    // Forgot password — sign-in mode only, no reason to show this
                    // while someone's mid-signup and hasn't set a password yet.
                    if (isLoginMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    resetEmail = email
                                    showForgotPasswordDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    strings.forgotPassword,
                                    color = MeTontRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

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
                                !isLoginMode && password.length < 8 -> {
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
                            .height(52.dp)
                            .testTag("authSubmitButton"),
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

                    // Sign in with Google — real functionality. Requests a Google ID
                    // token via Credential Manager, then hands it to AuthViewModel to
                    // exchange for a Firebase session (see requestGoogleIdToken above).
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val idToken = requestGoogleIdToken(context)
                                if (idToken != null) {
                                    viewModel.signInWithGoogle(
                                        idToken,
                                        isAlbanian = currentLanguage == AppLanguage.SQ
                                    )
                                }
                                // If idToken is null, the user cancelled or something
                                // went wrong picking an account — nothing to show, same
                                // as tapping outside any other system picker.
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("authGoogleButton"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MeTontWhite,
                            contentColor = Color(0xFF3C4043)
                        ),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            strings.continueWithGoogle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sign in with Apple — placeholder only, per product decision.
                    // No auth logic behind this yet; a collaborator is wiring up the
                    // real implementation separately.
                    Button(
                        onClick = { /* placeholder — no-op until Sign in with Apple is implemented */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("authAppleButton"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = MeTontWhite
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_apple_logo),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            strings.continueWithApple,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

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

    // ── FORGOT PASSWORD DIALOG ─────────────────────────────────────────
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                viewModel.resetPasswordResetState()
            },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(strings.resetPasswordTitle, fontWeight = FontWeight.Bold, color = Color.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        strings.resetPasswordDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(strings.email) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = MeTontRed) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        enabled = passwordResetState !is PasswordResetUiState.Loading
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordResetEmail(
                            resetEmail,
                            isAlbanian = currentLanguage == AppLanguage.SQ
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeTontRed),
                    enabled = passwordResetState !is PasswordResetUiState.Loading
                ) {
                    if (passwordResetState is PasswordResetUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(strings.sendResetLink, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        viewModel.resetPasswordResetState()
                    }
                ) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
    }
}


