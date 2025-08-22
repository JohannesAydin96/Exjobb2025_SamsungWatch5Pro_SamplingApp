package com.example.sampleapp.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetroClient {

    private const val BASE_URL = "https://bluepixeltech.com/heartrate/api/"

    private val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.MINUTES)  // Connection timeout
            .readTimeout(5, TimeUnit.MINUTES)     // Read timeout
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
