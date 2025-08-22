package com.example.sampleapp.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {
    @Multipart
    @POST("sendexercisedata")
    fun sendData(
        @PartMap params: HashMap<String, RequestBody>,
        @Part file: MultipartBody.Part
    ): Call<BaseRes>
}
