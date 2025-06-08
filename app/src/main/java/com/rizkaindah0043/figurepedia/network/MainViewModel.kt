package com.rizkaindah0043.figurepedia.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    init {
        retrieveDta()
    }

    private fun retrieveDta() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TokohApi.service.getTokoh("null")
                Log.d("MainViewModel", "Success: $result")
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
            }
        }
    }
}