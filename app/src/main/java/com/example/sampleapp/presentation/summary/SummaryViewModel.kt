package com.example.sampleapp.presentation.summary

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.app.Screen
import com.example.sampleapp.network.BaseRes
import com.example.sampleapp.network.RetroClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // State to hold the response status
    var responseMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    val uiState = MutableStateFlow(
        SummaryScreenState(
            averageHeartRate = savedStateHandle.get<Float>(Screen.Summary.averageHeartRateArg)!!
                .toDouble(),
            elapsedTime = Duration.parse(savedStateHandle.get(Screen.Summary.elapsedTimeArg)!!),
            avgX = savedStateHandle.get<Float>(Screen.Summary.avgX),
            avgY = savedStateHandle.get<Float>(Screen.Summary.avgY),
            avgZ = savedStateHandle.get<Float>(Screen.Summary.avgZ),
            fileName = savedStateHandle.get<String>(Screen.Summary.fileName),
        )
    )


    fun callSendDataApi(path: String, email: String) {
        Log.e("HeartRateViewModel", "callSendDataApi : path $path  EMAIL $email")
        isLoading.value = true

        val apiService = RetroClient.getApiService()
        val hasMap = HashMap<String, RequestBody>()
        val file = File(path)

        if (!file.exists()) {
            Log.e("HeartRateViewModel", "File does not exist: ${file.absolutePath}")
            responseMessage.value = "File not found!"
            isLoading.value = false
            return
        }

        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
        hasMap["email"] = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
        hasMap["action"] = RequestBody.create("text/plain".toMediaTypeOrNull(), "sendexercisedata")

        viewModelScope.launch {
            apiService.sendData(hasMap, multipartBody)
                .enqueue(object : retrofit2.Callback<BaseRes> {
                    override fun onResponse(call: Call<BaseRes>, response: Response<BaseRes>) {
                        isLoading.value = false
                        if (response.isSuccessful && response.body()?.status == "Success") {
                            Log.e("HeartRateViewModel", "Upload success: ${response.body()?.msg}")
                            responseMessage.value = response.body()?.msg
                        } else {
                            Log.e(
                                "HeartRateViewModel",
                                "Upload failed: ${response.code()} ${
                                    response.errorBody()?.string()
                                }"
                            )
                            responseMessage.value = "Upload failed: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<BaseRes>, t: Throwable) {
                        isLoading.value = false
                        Log.e("HeartRateViewModel", "Upload error: ${t.localizedMessage}", t)
                        responseMessage.value = "Error: ${t.localizedMessage}"
                    }
                })
        }
    }
}