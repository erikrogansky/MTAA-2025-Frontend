package com.roganskyerik.cookly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import kotlinx.coroutines.launch

@Composable
fun CustomModal(
    showModal: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = if (showModal) ModalBottomSheetValue.Expanded else ModalBottomSheetValue.Hidden
    )

    val colors = LocalCooklyColors.current

    // Monitor the state and dismiss when dragged down
    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
            onDismiss()
        }
    }

    // Control modal opening when needed
    LaunchedEffect(showModal) {
        if (showModal) {
            coroutineScope.launch { bottomSheetState.show() }
        } else {
            coroutineScope.launch { bottomSheetState.hide() }
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        sheetBackgroundColor = colors.ModalBackground,
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 28.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(colors.PureOpositeColor.copy(0.15f), shape = RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Content Slot
                content()

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    ) {}
}