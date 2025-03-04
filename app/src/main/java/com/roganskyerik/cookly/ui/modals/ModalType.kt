package com.roganskyerik.cookly.ui.modals

import androidx.compose.runtime.Composable

sealed class ModalType {
    data class Custom(val content: @Composable (onDismiss: () -> Unit) -> Unit) : ModalType()
}