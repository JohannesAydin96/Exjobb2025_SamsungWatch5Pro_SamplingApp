package com.example.sampleapp.presentation.history

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.network.BaseRes
import com.example.sampleapp.network.RetroClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

@HiltViewModel
class HistoryViewModel @Inject constructor(
) : ViewModel() {

    // State to hold the response status
    var responseMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)


    fun getAllFilesFromFolder(context: Context): List<File>? {
        // Locate the folder inside the cache directory
        val folder = File(context.cacheDir, "ExerciseHistory")

        // Check if the folder exists and is a directory
        return if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.toList()  // Convert the array to a list
        } else {
            null
        }
    }



    fun deleteFileFromFolder(context: Context, fileName: String): Boolean {
        val folder = File(context.cacheDir, "ExerciseHistory")
        val fileToDelete = File(folder, fileName)
        return fileToDelete.exists() && fileToDelete.delete()
    }


    fun callSendDataApi(path: String, email: String) {

        Log.e("HeartRateViewModel", "callSendDataApi : path $path  EMAIL $email")
        isLoading.value = true

        val apiService = RetroClient.getApiService()
        val hasMap = HashMap<String, RequestBody>()
        val file = File(path)
        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val multipartBody =
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        hasMap["email"] = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
        hasMap["action"] = RequestBody.create("text/plain".toMediaTypeOrNull(), "sendexercisedata")

        viewModelScope.launch {
            apiService.sendData(hasMap, multipartBody)
                .enqueue(object : retrofit2.Callback<BaseRes> {
                    override fun onResponse(call: Call<BaseRes>, response: Response<BaseRes>) {
                        isLoading.value = false
                        if (response.body() != null && "Success" == response.body()?.status) {
                            responseMessage.value = response.body()?.msg
                        } else {
                            Log.e("HeartRateViewModel", "Response : ${response.code()}")
                            responseMessage.value = "Response Null"
                        }
                    }

                    override fun onFailure(call: Call<BaseRes>, t: Throwable) {
                        isLoading.value = false
                        Log.e("HeartRateViewModel", "onFailure : $t")
                        responseMessage.value = t.message
                    }
                })
        }
    }
}



