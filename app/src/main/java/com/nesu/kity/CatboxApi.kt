package com.nesu.kity

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit // Import TimeUnit

object CatboxApi {
    private const val BASE_URL = "https://catbox.moe/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Define timeout duration
    private const val UPLOAD_TIMEOUT_SECONDS = 120L // 120 seconds = 2 minutes

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)    // Socket read timeout
        .writeTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)   // Socket write timeout
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Use the OkHttp client with adjusted timeouts
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val retrofitService: CatboxApiService by lazy {
        retrofit.create(CatboxApiService::class.java)
    }
}