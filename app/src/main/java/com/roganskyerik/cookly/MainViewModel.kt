package com.roganskyerik.cookly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.repository.ApiRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ApiRepository) : ViewModel() {

    fun login(email: String, password: String, onResult: (LoginResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun register(name: String, email: String, password: String, onResult: (RegisterResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(name, email, password)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun logout(refreshToken: String, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.logout(refreshToken)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }
}
