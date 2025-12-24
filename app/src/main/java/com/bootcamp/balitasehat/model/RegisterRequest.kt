package com.bootcamp.balitasehat.model

import com.google.gson.annotations.SerializedName

// ============================================================================
// CHECK NIK
// ============================================================================

data class CheckNikResponse(
    @SerializedName("status") val status: String, // "found" atau "not_found"
    @SerializedName("data") val data: ChildData?,
    @SerializedName("message") val message: String?
)

data class ChildData(
    @SerializedName("child_id") val childId: String,
    @SerializedName("nik_anak") val nikAnak: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("created_at") val createdAt: String
)

// ============================================================================
// REGISTER CHILD
// ============================================================================

data class RegisterChildRequest(
    @SerializedName("nik_anak") val nikAnak: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String, // "laki-laki" atau "perempuan"
    @SerializedName("birth_date") val birthDate: String // format: "YYYY-MM-DD"
)

data class RegisterChildResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: RegisteredChild?
)

data class RegisteredChild(
    @SerializedName("child_id") val childId: String,
    @SerializedName("nik_anak") val nikAnak: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birth_date") val birthDate: String
)

// ============================================================================
// ADD MEASUREMENT
// ============================================================================

data class AddMeasurementRequest(
    @SerializedName("nik_anak") val nikAnak: String,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double
)

data class AddMeasurementResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: MeasurementData?
)

data class MeasurementData(
    @SerializedName("measurement_id") val measurementId: Int,
    @SerializedName("child_id") val childId: String,
    @SerializedName("age_months") val ageMonths: Int,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("classification") val classification: Classification
)

data class Classification(
    @SerializedName("height_status") val heightStatus: String,
    @SerializedName("weight_status") val weightStatus: String,
    @SerializedName("height_zscore") val heightZscore: Double,
    @SerializedName("weight_zscore") val weightZscore: Double
)

// ============================================================================
// HISTORY
// ============================================================================

data class HistoryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("child_id") val childId: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("measurements") val measurements: List<Measurement>
)

data class Measurement(
    @SerializedName("measurement_id") val measurementId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("age_months") val ageMonths: Int,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("height_category") val heightCategory: String,
    @SerializedName("weight_category") val weightCategory: String,
    @SerializedName("height_zscore") val heightZscore: Double?,
    @SerializedName("weight_zscore") val weightZscore: Double?
)