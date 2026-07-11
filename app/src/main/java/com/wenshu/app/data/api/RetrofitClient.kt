package com.wenshu.app.data.api

import com.google.gson.Gson
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.model.ApiError
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiException(
    message: String,
    val code: String? = null,
    val suggestions: List<String>? = null
) : Exception(message)

object RetrofitClient {

    private const val BASE_URL = "https://wenshu-server.onrender.com/api/"
    private const val TIMEOUT_SECONDS = 30L

    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = SharedPreferencesManager.getToken()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun parseError(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrBlank()) return "请求失败，请检查网络连接"
            val error = gson.fromJson(errorBody, ApiError::class.java)
            error.error ?: "请求失败"
        } catch (e: Exception) {
            "请求失败，请稍后重试"
        }
    }

    fun parseErrorResponse(errorBody: String?): ApiError? {
        return try {
            if (errorBody.isNullOrBlank()) return null
            gson.fromJson(errorBody, ApiError::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val errorResponse = RetrofitClient.parseErrorResponse(errorBody)
        val errorMsg = errorResponse?.error ?: RetrofitClient.parseError(errorBody)
        Result.failure(ApiException(errorMsg, errorResponse?.code, errorResponse?.suggestions))
    } catch (e: java.net.SocketTimeoutException) {
        Result.failure(ApiException("连接超时，请检查网络"))
    } catch (e: java.net.UnknownHostException) {
        Result.failure(ApiException("网络连接失败，请检查网络设置"))
    } catch (e: Exception) {
        Result.failure(ApiException(e.message ?: "未知错误"))
    }
}
