package com.example.mywallpaper.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.components.GlassButton
import com.example.mywallpaper.ui.theme.*

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(state) {
        if (state is AuthState.Authenticated) {
            onAuthenticated()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Hero gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientViolet.copy(alpha = 0.3f),
                            Background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top logo section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(colors = listOf(GradientViolet, GradientCyan)),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("W", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "MyWallpaper Studio",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Discover beautiful wallpapers.\nCreate the ones you imagine.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Bottom actions
            Column(modifier = Modifier.fillMaxWidth()) {
                // Error message
                if (state is AuthState.Error) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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

                GradientButton(
                    text = "Sign In with Email",
                    onClick = onNavigateToSignIn,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                GlassButton(
                    text = "Create Account",
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                    Text("  or continue with  ", color = TextSecondary, fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val intent = viewModel.getGoogleSignInClient().signInIntent
                        googleSignInLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ShapeButton,
                    enabled = state !is AuthState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                ) {
                    if (state is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
                    } else {
                        Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Continue with Google", color = Color.White)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
