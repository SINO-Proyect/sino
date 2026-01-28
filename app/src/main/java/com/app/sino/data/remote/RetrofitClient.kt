package com.app.sino.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.145.143.212:8080/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    //region Date and Time Serializers/Deserializers
    private val localDateAdapter = object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: com.google.gson.JsonSerializationContext?): com.google.gson.JsonElement {
            return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalDate? {
            return json?.asString?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        }
    }

    private val localTimeAdapter = object : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: com.google.gson.JsonSerializationContext?): com.google.gson.JsonElement {
            return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_TIME))
        }

        override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalTime? {
            return json?.asString?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
        }
    }
    //endregion

    private val customGson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, localDateAdapter)
        .registerTypeAdapter(LocalTime::class.java, localTimeAdapter)
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(customGson))
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
    val studyPlanApi: StudyPlanApi = retrofit.create(StudyPlanApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val eventApi: EventApi = retrofit.create(EventApi::class.java)
    val courseApi: CourseApi = retrofit.create(CourseApi::class.java)
}
