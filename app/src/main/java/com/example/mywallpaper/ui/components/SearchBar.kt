package com.example.mywallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.theme.Primary
import com.example.mywallpaper.ui.theme.SurfaceContainerHigh
import com.example.mywallpaper.ui.theme.TextSecondary

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search wallpapers...",
    onSearch: () -> Unit = {},
    glassmorphic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val barShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                if (glassmorphic) {
                    Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = barShape,
                            spotColor = Primary.copy(alpha = 0.45f),
                            ambientColor = Color.Black.copy(alpha = 0.6f)
                        )
                        .clip(barShape)
                        .background(Color(0xD9181E2C))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.1f),
                                    Primary.copy(alpha = 0.35f)
                                )
                            ),
                            shape = barShape
                        )
                } else {
                    Modifier
                        .clip(barShape)
                        .background(SurfaceContainerHigh)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = if (glassmorphic) Primary else TextSecondary,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp).size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Normal),
            cursorBrush = SolidColor(if (glassmorphic) Primary else Color.White),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(placeholder, color = TextSecondary, fontSize = 15.sp)
                }
                innerTextField()
            },
            modifier = Modifier.weight(1f)
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = {
                onQueryChange("")
                keyboardController?.hide()
                focusManager.clearFocus()
            }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
