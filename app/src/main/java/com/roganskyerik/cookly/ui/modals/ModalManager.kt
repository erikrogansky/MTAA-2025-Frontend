package com.roganskyerik.cookly.ui.modals

import androidx.compose.runtime.*
import com.roganskyerik.cookly.ui.CustomModal

@Composable
fun ModalManager(modalType: ModalType?, onDismiss: () -> Unit) {
    if (modalType == null) return

    CustomModal(
        showModal = true,
        onDismiss = { onDismiss() }
    ) {
        when (modalType) {
            is ModalType.Custom -> {
                modalType.content(onDismiss)
            }
        }
    }
}
