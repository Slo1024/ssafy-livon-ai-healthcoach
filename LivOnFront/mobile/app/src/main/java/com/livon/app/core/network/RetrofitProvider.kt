package com.livon.app.core.network

import com.livon.app.BuildConfig
import com.livon.app.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    // Use APPLICATION_SERVER_URL (should include /api/v1) as the canonical base URL.
    // Retrofit requires the baseUrl to end with a single '/'. We normalize here to avoid
    // accidental double slashes when endpoints start with '/'.
    private val BASE_URL: String = BuildConfig.APPLICATION_SERVER_URL
        .let { url ->
            if (url.isBlank()) throw IllegalStateException("APPLICATION_SERVER_URL is not configured.")
            // Normalize: remove any trailing slashes, then add exactly one.
            url.trimEnd('/') + "/"
        }

    private val authInterceptor = Interceptor { chain ->
        val reqBuilder = chain.request().newBuilder()
        SessionManager.getTokenSync()?.let { token ->
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(reqBuilder.build())
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Build Moshi with Kotlin adapter so Kotlin data classes are supported
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun <T> createService(service: Class<T>): T = retrofit.create(service)
}
