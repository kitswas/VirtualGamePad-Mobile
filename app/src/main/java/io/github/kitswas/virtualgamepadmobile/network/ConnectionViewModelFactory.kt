package io.github.kitswas.virtualgamepadmobile.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConnectionViewModelFactory(private val onConnectionSuccess: suspend (String, Int) -> Unit) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ConnectionViewModel(onConnectionSuccess) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
