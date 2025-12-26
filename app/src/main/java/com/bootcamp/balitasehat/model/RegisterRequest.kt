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
    @SerializedName("id") val childId: String?,           // Backend uses "id" not "child_id"
    @SerializedName("nik_anak") val nikAnak: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("created_at") val createdAt: String
)

data class ChildResult(
    @SerializedName("child_id") val childId: String?,  // ✅ Backend uses "child_id" in addMeasurement
    @SerializedName("name") val name: String,
    @SerializedName("age_months") val ageMonths: Int,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("nik_anak") val nikAnak: String? = null
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
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("measurement_date") val measurementDate: String
)

data class AddMeasurementResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: MeasurementResult?
)

data class MeasurementResult(
    @SerializedName("measurement_id") val measurementId: Int,
    @SerializedName("child") val child: ChildResult,
    @SerializedName("measurement") val measurement: MeasurementResultData,
    @SerializedName("classification") val classification: ClassificationResult,
    @SerializedName("chart_urls") val chartUrls: ChartUrls
)

data class MeasurementResultData(
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("measurement_date") val measurementDate: String
)

data class ClassificationResult(
    @SerializedName("height_zscore") val heightZscore: Double,
    @SerializedName("weight_zscore") val weightZscore: Double,
    @SerializedName("classification_height") val classificationHeight: String,
    @SerializedName("classification_weight") val classificationWeight: String,
    @SerializedName("risk_level") val riskLevel: String
)

data class ChartUrls(
    @SerializedName("growth") val growth: String,
    @SerializedName("zscore") val zscore: String
)

// ============================================================================
// HISTORY
// ============================================================================

data class HistoryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("child_id") val childId: String,
    @SerializedName("total_measurements") val totalMeasurements: Int,
    @SerializedName("data") val data: List<HistoryItem>
)

data class HistoryItem(
    @SerializedName("measurement_date") val measurementDate: String,
    @SerializedName("age_months") val ageMonths: Int,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("weight_kg") val weightKg: Double,

    @SerializedName("zscore_height") val zscoreHeight: Double,
    @SerializedName("zscore_weight") val zscoreWeight: Double,

    @SerializedName("classification_height") val classificationHeight: String? = null,
    @SerializedName("classification_weight") val classificationWeight: String? = null,
    @SerializedName("risk_level") val riskLevel: String? = null,

    // ⚠️ Deprecated fields (for backward compatibility)
    @SerializedName("stunting_status") val stuntingStatus: String? = null,
    @SerializedName("wasting_status") val wastingStatus: String? = null
)