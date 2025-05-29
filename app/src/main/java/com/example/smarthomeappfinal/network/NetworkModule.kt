package com.example.smarthomeappfinal.network

import com.example.smarthomeappfinal.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private var retrofit: Retrofit? = null
    private var apiService: SmartHomeApiService? = null

    fun provideApiService(): SmartHomeApiService {
        return apiService ?: synchronized(this) {
            apiService ?: createApiService().also { apiService = it }
        }
    }

    private fun createApiService(): SmartHomeApiService {
        return getRetrofit().create(SmartHomeApiService::class.java)
    }

    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: createRetrofit().also { retrofit = it }
        }
    }

    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(SmartHomeApiService.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.Network.TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(Constants.Network.TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(Constants.Network.TIMEOUT_WRITE, TimeUnit.SECONDS)
            .build()
    }

    fun reset() {
        retrofit = null
        apiService = null
    }
} 