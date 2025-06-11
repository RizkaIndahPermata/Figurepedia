package com.rizkaindah0043.figurepedia.network

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizkaindah0043.figurepedia.model.Tokoh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Tokoh>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


     fun retrieveDta(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val response = TokohApi.service.getTokoh(userId)
                Log.d("MainViewModel", "Data dari API: ${response.people}")
                data.value = response.people
                Log.d("MainViewModel", "State data diperbarui")
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(userId: String?, name: String, country: String, field: String, bitmap: Bitmap) {
        if (userId.isNullOrBlank()) {
            errorMessage.value = "Anda harus login terlebih dahulu untuk menambahkan data"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = TokohApi.service.postTokoh(
                    userId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    country.toRequestBody("text/plain".toMediaTypeOrNull()),
                    field.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
                if (result.isSuccessful) {
                    val body = result.body()
                    Log.d("MainViewModel", "Response status: ${body?.status}")
                    if (body?.status == "201") {
                        retrieveDta(userId)
                    } else {
                        Log.d("MainViewModel", "Server responded but with failure status: ${body?.status}")
                    }
                } else {
                    Log.d("MainViewModel", "Server error: ${result.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(userId: String, id: String) {
        viewModelScope.launch(Dispatchers.IO){
            try {
                val response = TokohApi.service.deleteTokoh(id)
                if (response.isSuccessful) {
                    Log.d("MainViewModel", "Data berhasil dihapus.")
                    retrieveDta(userId)
                } else {
                    Log.e("MainViewModel", "Gagal menghapus. Kode: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Exception saat menghapus: ${e.message}")
            }
        }
    }

    fun updateData(userId: String, id: String, name: String, country: String, field: String, bitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val countryPart = country.toRequestBody("text/plain".toMediaTypeOrNull())
                val fieldPart = field.toRequestBody("text/plain".toMediaTypeOrNull())

                val image = bitmap?.let {
                    val file = File.createTempFile("image", ".jpg")
                    val out = FileOutputStream(file)
                    it.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()

                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val response = TokohApi.service.updateTokoh(id, namePart, countryPart, fieldPart, image)
                if (response.isSuccessful) {
                    Log.d("UpdateTokoh", "Berhasil update data tokoh: $id")
                    retrieveDta(userId)
                } else {
                    Log.e("UpdateTokoh", "Gagal update: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("UpdateTokoh", "Exception: ${e.message}")
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody)
    }
    fun clearMessage() { errorMessage.value = null }
}