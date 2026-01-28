package com.app.sino.data.remote

import android.content.Context
import com.app.sino.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://192.168.0.150:8080/"
    private var authInterceptor: AuthInterceptor? = null

    fun initialize(context: Context) {
        authInterceptor = AuthInterceptor(TokenManager(context))
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val interceptor = authInterceptor
            if (interceptor != null) {
                interceptor.intercept(chain)
            } else {
                chain.proceed(chain.request())
            }
        }
        .connectTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
    val studyPlanApi: StudyPlanApi = retrofit.create(StudyPlanApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
}
