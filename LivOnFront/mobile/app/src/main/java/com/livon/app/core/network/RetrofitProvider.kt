package com.livon.app.core.network

import android.util.Log
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
        val token = SessionManager.getTokenSync()
        val request = chain.request()
        
        // Authorization 헤더 추가
        token?.let { 
            reqBuilder.addHeader("Authorization", "Bearer $it")
        } ?: run {
            Log.w("RetrofitProvider", "토큰이 없어서 Authorization 헤더를 추가할 수 없습니다.")
        }
        
        // POST 요청이고 본문이 비어있으면 Content-Type 헤더 추가 (nginx 호환성)
        if (request.method == "POST" && request.body == null) {
            reqBuilder.addHeader("Content-Type", "application/json")
        }
        
        chain.proceed(reqBuilder.build())
    }

    private val logging = HttpLoggingInterceptor().apply {
        // Level.BODY can trigger reading of empty/chunked bodies and cause EOF exceptions in some setups.
        // Switch to BASIC to keep useful request/response logging without forcing body reads.
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Expose Moshi publicly so other repositories can reuse the exact same configured instance
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun <T> createService(service: Class<T>): T = retrofit.create(service)
}
