package com.rizkaindah0043.figurepedia.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val BASE_URL = "https://notableperson.sendiko.my.id/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface TokohApiService {
    @GET("people")
    @Headers("Accept: application/json")
    suspend fun getTokoh(@Query("userId") userId: String): String
}

object TokohApi {
    val service: TokohApiService by lazy {
        retrofit.create(TokohApiService::class.java)
    }
}