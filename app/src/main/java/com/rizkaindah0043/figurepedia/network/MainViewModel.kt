package com.rizkaindah0043.figurepedia.network

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizkaindah0043.figurepedia.model.Tokoh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Tokoh>())
        private set

    init {
        retrieveDta()
    }

    private fun retrieveDta() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = TokohApi.service.getTokoh("null")
                data.value = response.people
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
            }
        }
    }
}