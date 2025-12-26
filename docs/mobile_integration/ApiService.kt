/**
 * API Service Interface untuk BalitaSehat
 * Menggunakan Retrofit untuk consume REST API
 */

package com.example.balitasehat.api

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    
    // ============================================================================
    // ENDPOINT REGISTER & NIK CHECK
    // ============================================================================
    
    /**
     * Check apakah NIK sudah terdaftar
     * GET /api/child/check?nik=3301234567890123
     */
    @GET("api/child/check")
    fun checkNik(
        @Query("nik") nik: String
    ): Call<CheckNikResponse>
    
    /**
     * Register anak baru
     * POST /api/child/register
     */
    @POST("api/child/register")
    fun registerChild(
        @Body request: RegisterChildRequest
    ): Call<RegisterChildResponse>
    
    
    // ============================================================================
    // ENDPOINT MEASUREMENT
    // ============================================================================
    
    /**
     * Input pengukuran baru
     * POST /api/measurement/add
     */
    @POST("api/measurement/add")
    fun addMeasurement(
        @Body request: AddMeasurementRequest
    ): Call<AddMeasurementResponse>
    
    
    // ============================================================================
    // ENDPOINT HISTORY & CHART
    // ============================================================================
    
    /**
     * Get riwayat pengukuran
     * GET /api/child/{child_id}/history
     */
    @GET("api/child/{child_id}/history")
    fun getHistory(
        @Path("child_id") childId: String
    ): Call<HistoryResponse>
    
    /**
     * Get chart URL (tidak perlu Call, langsung build URL)
     * GET /api/child/{child_id}/chart?type=growth
     */
    fun getChartUrl(baseUrl: String, childId: String, type: String): String {
        return "$baseUrl/api/child/$childId/chart?type=$type"
    }
}
