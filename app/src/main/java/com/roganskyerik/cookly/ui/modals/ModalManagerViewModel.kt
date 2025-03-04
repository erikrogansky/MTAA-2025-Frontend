package com.roganskyerik.cookly.ui.modals

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ModalManagerViewModel : ViewModel() {
    private val _modalType = MutableStateFlow<ModalType?>(null)
    val modalType: StateFlow<ModalType?> get() = _modalType

    fun showModal(modal: ModalType) {
        _modalType.value = modal
    }

    fun dismissModal() {
        _modalType.value = null
    }
}