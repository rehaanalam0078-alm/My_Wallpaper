package com.example.mywallpaper.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.theme.*

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is AuthState.Authenticated) {
            onAuthenticated()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(Modifier.height(32.dp))
        Text("Welcome back", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Sign in with your email to continue", color = TextSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(40.dp))

        if (state is AuthState.Error) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = ErrorContainer.copy(alpha = 0.3f)
            ) {
                Text(
                    (state as AuthState.Error).message,
                    color = ErrorColor,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = OutlineVariant,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Primary,
            focusedContainerColor = SurfaceContainerHigh,
            unfocusedContainerColor = SurfaceContainerHigh
        )

        // Email field
        Text("Email", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("your@email.com", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors
        )
        Spacer(Modifier.height(20.dp))

        // Password field
        Text("Password", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your password", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.signInWithEmail(email, password)
            }),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors
        )
        Spacer(Modifier.height(40.dp))

        GradientButton(
            text = "Sign In",
            onClick = {
                focusManager.clearFocus()
                viewModel.signInWithEmail(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state is AuthState.Loading,
            enabled = state !is AuthState.Loading
        )
    }
}
