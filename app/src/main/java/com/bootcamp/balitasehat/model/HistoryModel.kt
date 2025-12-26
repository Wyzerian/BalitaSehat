package com.bootcamp.balitasehat.model

data class HistoryModel(
    val childId: String,
    val nama: String,
    val umur: String,
    val gender: String,
    val tinggi: String,
    val berat: String,
    val tanggalLahir: String,
    val tanggalInput: String,
    val zscoreHeight: Double = 0.0,
    val zscoreWeight: Double = 0.0,
    val classificationHeight: String? = null,
    val classificationWeight: String? = null,
    val riskLevel: String? = null,
    // Deprecated
    val stuntingStatus: String? = null,
    val wastingStatus: String? = null
)
