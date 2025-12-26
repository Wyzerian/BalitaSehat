package com.bootcamp.balitasehat.utils

import android.content.Context
import com.bootcamp.balitasehat.R

object StatusUtils {

    /**
     * Mengembalikan status gabungan berdasarkan classification height dan weight
     * Format: "Height: [classification_height] | Weight: [classification_weight]"
     */
    fun getCombinedStatus(
        classificationHeight: String?,
        classificationWeight: String?
    ): String {
        val height = classificationHeight ?: "Unknown"
        val weight = classificationWeight ?: "Unknown"
        return "TB: $height | BB: $weight"
    }

    /**
     * Mengembalikan warna untuk classification height (Tinggi Badan)
     *
     * Kategori & Warna:
     * 🔴 RED (Bahaya/Sangat Berisiko):
     *   - Severely Stunted: Z-score < -3
     *
     * 🟠 ORANGE (Risiko Tinggi):
     *   - Stunted: -3 <= Z-score < -2
     *
     * 🟡 YELLOW (Peringatan):
     *   - At Risk: -2 <= Z-score < -1
     *
     * 🟢 GREEN (Normal/Baik):
     *   - Normal: -1 <= Z-score <= 2
     *   - Tall: Z-score > 2
     */
    fun getHeightStatusColor(context: Context, classification: String?): Int {
        return when (classification?.trim()) {
            "Severely Stunted" -> context.getColor(R.color.red_status)
            "Stunted" -> context.getColor(R.color.orange_status)
            "At Risk", "At Risk (Early Warning)" -> context.getColor(R.color.yellow_status)
            "Normal" -> context.getColor(R.color.green_status)
            "Tall" -> context.getColor(R.color.green_status)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }

    /**
     * Mengembalikan warna untuk classification weight (Berat Badan)
     *
     * Kategori & Warna:
     * 🔴 RED (Bahaya/Sangat Berisiko):
     *   - Severely Underweight: Z-score < -3
     *
     * 🟠 ORANGE (Risiko Tinggi):
     *   - Underweight: -3 <= Z-score < -2
     *   - Overweight: Z-score > 2
     *
     * 🟡 YELLOW (Peringatan):
     *   - At Risk: -2 <= Z-score < -1
     *   - Risk of Overweight: 1 < Z-score <= 2
     *
     * 🟢 GREEN (Normal/Baik):
     *   - Normal weight: -1 <= Z-score <= 1
     *   - Normal: -1 <= Z-score <= 1
     */
    fun getWeightStatusColor(context: Context, classification: String?): Int {
        return when (classification?.trim()) {
            "Severely Underweight" -> context.getColor(R.color.red_status)
            "Underweight" -> context.getColor(R.color.orange_status)
            "At Risk", "At Risk (Early Warning)" -> context.getColor(R.color.yellow_status)
            "Normal weight", "Normal" -> context.getColor(R.color.green_status)
            "Risk of Overweight" -> context.getColor(R.color.yellow_status)
            "Overweight" -> context.getColor(R.color.orange_status)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }

    /**
     * Mengembalikan warna berdasarkan Risk Level
     *
     * - HIGH: RED
     * - MEDIUM: YELLOW
     * - LOW: GREEN
     */
    fun getRiskLevelColor(context: Context, riskLevel: String?): Int {
        return when (riskLevel?.uppercase()?.trim()) {
            "HIGH" -> context.getColor(R.color.red_status)
            "MEDIUM" -> context.getColor(R.color.yellow_status)
            "LOW" -> context.getColor(R.color.green_status)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }

    /**
     * Mengembalikan warna dominan berdasarkan kombinasi height dan weight
     *
     * Prioritas (dari tertinggi ke terendah):
     * 1. 🔴 RED - Jika salah satu Severely Stunted/Severely Underweight
     * 2. 🟠 ORANGE - Jika salah satu Stunted/Underweight/Overweight
     * 3. 🟡 YELLOW - Jika salah satu At Risk/Risk of Overweight
     * 4. 🟢 GREEN - Jika keduanya Normal/Tall
     *
     * Contoh:
     * - TB: Normal + BB: Risk of Overweight = YELLOW (peringatan, friendly)
     * - TB: Normal + BB: Normal = GREEN (semua baik)
     * - TB: Stunted + BB: Normal = ORANGE (ada risiko tinggi)
     */
    fun getCombinedStatusColor(
        context: Context,
        classificationHeight: String?,
        classificationWeight: String?
    ): Int {
        val heightColor = getHeightStatusColor(context, classificationHeight)
        val weightColor = getWeightStatusColor(context, classificationWeight)

        val red = context.getColor(R.color.red_status)
        val orange = context.getColor(R.color.orange_status)
        val yellow = context.getColor(R.color.yellow_status)
        val green = context.getColor(R.color.green_status)

        // Prioritas: RED > ORANGE > YELLOW > GREEN
        return when {
            heightColor == red || weightColor == red -> red           // Bahaya
            heightColor == orange || weightColor == orange -> orange  // Risiko Tinggi
            heightColor == yellow || weightColor == yellow -> yellow  // Peringatan
            heightColor == green && weightColor == green -> green     // Normal
            else -> context.getColor(android.R.color.darker_gray)     // Unknown
        }
    }

    /**
     * Mengembalikan status text yang lebih ringkas untuk display
     */
    fun getSimplifiedStatus(
        classificationHeight: String?,
        classificationWeight: String?,
        riskLevel: String?
    ): String {
        val height = classificationHeight ?: "-"
        val weight = classificationWeight ?: "-"
        val risk = riskLevel ?: "-"

        return "TB: $height\nBB: $weight\nRisk: $risk"
    }
}

