package com.roganskyerik.cookly.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text

@Composable
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(50.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(modifier = modifier) {
        var isFocused by remember { mutableStateOf(false) }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        ) { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, if (isFocused) borderColor else Color.Gray, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp) // Adjust padding here
            ) {
                if (value.isEmpty()) {
                    Text(text = label, color = labelColor)
                }
                innerTextField()
            }
        }
    }
}