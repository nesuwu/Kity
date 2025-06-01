package com.nesu.kity

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CatboxApiService {
    @Multipart
    @POST("user/api.php")
    suspend fun uploadFile(
        @Part("reqtype") reqtype: RequestBody,
        @Part("userhash") userhash: RequestBody,
        @Part fileToUpload: MultipartBody.Part
    ): Response<String>

    @Multipart
    @POST("user/api.php")
    suspend fun uploadFileAnonymous(
        @Part("reqtype") reqtype: RequestBody,
        @Part fileToUpload: MultipartBody.Part
    ): Response<String>
}