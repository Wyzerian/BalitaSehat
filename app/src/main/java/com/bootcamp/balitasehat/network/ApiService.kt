package com.bootcamp.balitasehat.network

import com.bootcamp.balitasehat.model.AddMeasurementResponse
import com.bootcamp.balitasehat.model.CheckNikResponse
import com.bootcamp.balitasehat.model.RegisterChildRequest
import com.bootcamp.balitasehat.model.RegisterChildResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("api/child/check")
    fun checkNik(
        @Query("nik") nik: String
    ): Call<CheckNikResponse>

    @POST("api/child/register")
    fun registerChild(
        @Body request: RegisterChildRequest
    ): Call<RegisterChildResponse>

    // ✅ TAMBAHKAN INI
    @GET("api/measurement/latest")
    fun getLatestMeasurement(
        @Query("child_id") childId: String
    ): Call<AddMeasurementResponse>
}

