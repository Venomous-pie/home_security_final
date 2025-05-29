package com.example.smarthomeappfinal.repository

import com.example.smarthomeappfinal.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            NetworkResult.Success(apiCall())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> NetworkResult.Error("Network Error: Please check your internet connection")
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = throwable.message()
                    NetworkResult.Error("Error $code: $errorResponse", code)
                }
                else -> NetworkResult.Error("An unexpected error occurred: ${throwable.message}")
            }
        }
    }

    protected suspend fun <T> safeApiCallWithLoading(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return try {
            NetworkResult.Loading
            val response = withContext(Dispatchers.IO) { apiCall() }
            NetworkResult.Success(response)
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> NetworkResult.Error("Network Error: Please check your internet connection")
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = throwable.message()
                    NetworkResult.Error("Error $code: $errorResponse", code)
                }
                else -> NetworkResult.Error("An unexpected error occurred: ${throwable.message}")
            }
        }
    }
} 