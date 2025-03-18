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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.annotations.concurrent.Background
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito

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
    borderColor: Color = LocalCooklyColors.current.FontColor,
    focusedBorderColor: Color = LocalCooklyColors.current.DarkOrange,
    labelColor: Color = LocalCooklyColors.current.FontColor,
    isError: Boolean = false,
    errorColor: Color = LocalCooklyColors.current.Error
) {
    var isFocused by remember { mutableStateOf(false) }

    val currentBorderColor = when {
        isError -> errorColor
        isFocused -> focusedBorderColor
        else -> borderColor
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            singleLine = singleLine,
            textStyle = textStyle.copy(color = LocalCooklyColors.current.FontColor),
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        ) { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, currentBorderColor, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (value.isEmpty()) {
                    Text(text = label, color = labelColor.copy(alpha = 0.5f),style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Nunito
                    ))
                }
                innerTextField()
            }
        }
    }
}



@Composable
fun CustomMultilineTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(8.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    borderColor: Color = LocalCooklyColors.current.FontColor,
    focusedBorderColor: Color = LocalCooklyColors.current.DarkOrange,
    labelColor: Color = LocalCooklyColors.current.FontColor,
    isError: Boolean = false,
    errorColor: Color = LocalCooklyColors.current.Error,
    height: Int = 120
) {
    var isFocused by remember { mutableStateOf(false) }

    val currentBorderColor = when {
        isError -> errorColor
        isFocused -> focusedBorderColor
        else -> borderColor
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            singleLine = false,
            textStyle = textStyle.copy(color = LocalCooklyColors.current.FontColor),
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .onFocusChanged { isFocused = it.isFocused }
        ) { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, currentBorderColor, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxSize()
            ) {
                if (value.isEmpty()) {
                    Text(text = label, color = labelColor.copy(alpha = 0.5f),style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Nunito
                    ))
                }
                innerTextField()
            }
        }
    }
}


@Composable
fun CustomDropdownField(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    label: String = "Select an option",
    shape: Shape = RoundedCornerShape(50.dp),
    backgroundColor: Color = LocalCooklyColors.current.ModalBackground,
    borderColor: Color = LocalCooklyColors.current.FontColor,
    focusedBorderColor: Color = LocalCooklyColors.current.DarkOrange,
    labelColor: Color = LocalCooklyColors.current.FontColor,
    isError: Boolean = false,
    errorColor: Color = LocalCooklyColors.current.Error
) {
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    val currentBorderColor = when {
        isError -> errorColor
        isFocused -> focusedBorderColor
        else -> borderColor
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, currentBorderColor, shape)
                .background(backgroundColor, shape)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedOption == "") label else selectedOption ?: label,
                    color = if (selectedOption == "") labelColor.copy(alpha = 0.5f) else labelColor,
                    style = if (selectedOption == "") TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Nunito
                    ) else TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )
                )

                Icon(
                    painter = painterResource(id = R.drawable.arrow_bottom_icon),
                    contentDescription = "Dropdown arrow",
                    tint = labelColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),

            containerColor = LocalCooklyColors.current.ModalBackground
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = LocalCooklyColors.current.FontColor,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Nunito
                            )
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
