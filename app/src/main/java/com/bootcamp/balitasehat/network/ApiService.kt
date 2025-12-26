package com.bootcamp.balitasehat.network

import com.bootcamp.balitasehat.model.AddMeasurementRequest
import com.bootcamp.balitasehat.model.AddMeasurementResponse
import com.bootcamp.balitasehat.model.CheckNikResponse
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.model.RegisterChildRequest
import com.bootcamp.balitasehat.model.RegisterChildResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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
    @GET("api/child/{child_id}/history")
    fun getChildHistory(
        @Path("child_id") childId: String
    ): Call<HistoryResponse>

    @POST("api/measurement/add")
    fun addMeasurement(
        @Body request: AddMeasurementRequest
    ): Call<AddMeasurementResponse>

    @GET("api/child/{child_id}/chart")
    fun generateChart(
        @Path("child_id") childId: String,
        @Query("type") type: String
    ): Call<Void>
}

