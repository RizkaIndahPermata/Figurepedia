package com.rizkaindah0043.figurepedia.network

import com.rizkaindah0043.figurepedia.model.ApiResponse
import com.rizkaindah0043.figurepedia.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://notableperson.sendiko.my.id/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TokohApiService {
    @GET("people")
    @Headers("Accept: application/json")
    suspend fun getTokoh(@Query("userId") userId: String): ApiResponse

    @Multipart
    @POST("people")
    suspend fun postTokoh(
        @Part("userId") userId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("country") country: RequestBody,
        @Part("field") field: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<OpStatus>

    @DELETE("people/{id}")
    suspend fun deleteTokoh(
        @Path("id") id: String
    ): Response<Unit>

    @Multipart
    @PUT("people/{id}")
    suspend fun updateTokoh(
        @Path("id") id: String,
        @Part("name") name: RequestBody,
        @Part("country") country: RequestBody,
        @Part("field") field: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<Unit>
}

object TokohApi {
    val service: TokohApiService by lazy {
        retrofit.create(TokohApiService::class.java)
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }