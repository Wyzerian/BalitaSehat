/**
 * Retrofit Client Setup
 * Singleton pattern untuk HTTP client
 */

package com.example.balitasehat.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Ganti dengan IP VPS atau IP lokal kamu
    // DEV: IP lokal (testing dengan emulator/HP di jaringan sama)
    // PROD: IP VPS atau domain
    private const val BASE_URL_DEV = "http://10.10.150.20:5000/"
    private const val BASE_URL_PROD = "http://your-vps-ip:5000/"
    
    // Toggle untuk environment
    private const val IS_PRODUCTION = false
    val BASE_URL = if (IS_PRODUCTION) BASE_URL_PROD else BASE_URL_DEV
    
    // HTTP Client dengan logging (untuk debug)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (IS_PRODUCTION) {
            HttpLoggingInterceptor.Level.NONE // Production: no logs
        } else {
            HttpLoggingInterceptor.Level.BODY // Development: full logs
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit instance (singleton)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API Service instance (singleton)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
