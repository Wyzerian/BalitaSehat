package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.network.ApiClient
import com.bootcamp.balitasehat.utils.StatusUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // ===== TOMBOL BACK =====
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // ===== VIEW =====
        val tvNama = findViewById<TextView>(R.id.tvNamaAnak)
        val tvUmur = findViewById<TextView>(R.id.tvUmur)
        val tvTinggi = findViewById<TextView>(R.id.tvTinggi)
        val tvBerat = findViewById<TextView>(R.id.tvBerat)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggalInput)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvZScore = findViewById<TextView>(R.id.tvZScore)

        val imgGrowth = findViewById<ImageView>(R.id.imgGrowthChart)
        val imgZscore = findViewById<ImageView>(R.id.imgZscoreChart)

        // ===== DATA DARI INTENT =====
        val nama = intent.getStringExtra("nama") ?: "-"
        val umur = intent.getIntExtra("age_months", 0)
        val tinggi = intent.getDoubleExtra("height_cm", 0.0)
        val berat = intent.getDoubleExtra("weight_kg", 0.0)
        val tanggal = intent.getStringExtra("measurement_date") ?: "-"

        // ===== Z-SCORE DAN CLASSIFICATION DARI INTENT =====
        val heightZscore = intent.getDoubleExtra("height_zscore", 0.0)
        val weightZscore = intent.getDoubleExtra("weight_zscore", 0.0)
        val classificationHeight = intent.getStringExtra("classification_height")
        val classificationWeight = intent.getStringExtra("classification_weight")
        val riskLevel = intent.getStringExtra("risk_level")

        // ===== SET TEXT =====
        tvNama.text = nama
        tvUmur.text = "Umur : $umur bulan"
        tvTinggi.text = "Tinggi : $tinggi cm"
        tvBerat.text = "Berat : $berat kg"
        tvTanggal.text = "Tanggal : $tanggal"

        // ===== SET Z-SCORE DAN STATUS =====
        if (heightZscore != 0.0 || weightZscore != 0.0) {
            val zTbText = if (heightZscore.isFinite()) "%.2f".format(heightZscore) else "-"
            val zBbText = if (weightZscore.isFinite()) "%.2f".format(weightZscore) else "-"

            tvZScore.text = """
                Z-Score TB/U : $zTbText
                Z-Score BB/U : $zBbText
            """.trimIndent()

            // ✅ GUNAKAN CLASSIFICATION BARU
            if (!classificationHeight.isNullOrEmpty() && !classificationWeight.isNullOrEmpty()) {
                val statusText = """
                    TB: $classificationHeight
                    BB: $classificationWeight
                    Risk Level: ${riskLevel ?: "Unknown"}
                """.trimIndent()

                tvStatus.text = "Status : $statusText"

                // Gunakan warna berdasarkan kombinasi height dan weight
                val statusColor = StatusUtils.getCombinedStatusColor(
                    this,
                    classificationHeight,
                    classificationWeight
                )
                tvStatus.setTextColor(statusColor)

                Log.d("RESULT_DEBUG", "Classification - TB: $classificationHeight, BB: $classificationWeight, Risk: $riskLevel")
            } else {
                // Fallback ke sistem lama
                val statusText = when {
                    heightZscore <= -3 -> "Stunting"
                    heightZscore <= -1 -> "Berisiko Stunting"
                    else -> "Normal"
                }

                tvStatus.text = "Status : $statusText"

                when (statusText) {
                    "Normal" -> tvStatus.setTextColor(getColor(R.color.green_status))
                    "Berisiko Stunting" -> tvStatus.setTextColor(getColor(R.color.yellow_status))
                    "Stunting" -> tvStatus.setTextColor(getColor(R.color.red_status))
                    else -> tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                }

                Log.d("RESULT_DEBUG", "Fallback Status - Z-Score TB: $zTbText, BB: $zBbText, Status: $statusText")
            }
        } else {
            tvStatus.text = "Status : -"
            tvStatus.setTextColor(getColor(android.R.color.darker_gray))
            tvZScore.text = "Memuat Z-Score..."
        }

        // ===== AMBIL CHILD ID =====
        val childId = intent.getStringExtra("child_id")
        if (childId.isNullOrEmpty()) {
            Log.e("RESULT_DEBUG", "child_id tidak ditemukan")
            return
        }

        val timestamp = System.currentTimeMillis()

        // ===== URL GRAFIK (HASIL GENERATE BACKEND) =====
        val growthUrl =
            "http://144.208.67.53:8000/static/charts/${childId}_growth.png?t=$timestamp"

        val zscoreUrl =
            "http://144.208.67.53:8000/static/charts/${childId}_zscore.png?t=$timestamp"

        Log.d("RESULT_DEBUG", "growthUrl = $growthUrl")
        Log.d("RESULT_DEBUG", "zscoreUrl = $zscoreUrl")

        // ===== CLICK LISTENER UNTUK GRAFIK (FULL-SCREEN VIEWER) =====
        imgGrowth.setOnClickListener {
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image_url", growthUrl)
            intent.putExtra("image_title", "Grafik Pertumbuhan Tinggi Badan")
            startActivity(intent)
        }

        imgZscore.setOnClickListener {
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image_url", zscoreUrl)
            intent.putExtra("image_title", "Grafik Z-Score Berat Badan")
            startActivity(intent)
        }

        // ===== LOAD GRAFIK =====
        loadZScoreChartWithRetry(
            imgZscore,
            zscoreUrl
        )

        imgGrowth.postDelayed({
            // ⚠️ Safety check: Jangan load jika activity sedang finishing/destroyed
            if (!isFinishing && !isDestroyed) {
                try {
                    Glide.with(this)
                        .load(growthUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_error)
                        .into(imgGrowth)
                } catch (e: Exception) {
                    Log.e("RESULT", "Failed to load growth chart", e)
                }
            }
        }, 1500)

        // ===== FALLBACK: Load dari API jika Intent data kosong =====
        if (heightZscore == 0.0 && weightZscore == 0.0) {
            Log.d("RESULT_DEBUG", "Z-Score dari Intent kosong, load dari API...")

            ApiClient.apiService.getChildHistory(childId)
                .enqueue(object : Callback<HistoryResponse> {
                    override fun onResponse(
                        call: Call<HistoryResponse>,
                        response: Response<HistoryResponse>
                    ) {
                        val body = response.body()
                        if (body == null || body.data.isEmpty()) {
                            Log.e("RESULT_DEBUG", "API response kosong")
                            return
                        }

                        val latest = body.data.maxByOrNull { it.measurementDate } ?: return

                        val zTb = latest.zscoreHeight
                        val zBb = latest.zscoreWeight
                        val zTbText = if (zTb.isFinite()) "%.2f".format(zTb) else "-"
                        val zBbText = if (zBb.isFinite()) "%.2f".format(zBb) else "-"

                        runOnUiThread {
                            tvZScore.text = """
                                Z-Score TB/U : $zTbText
                                Z-Score BB/U : $zBbText
                            """.trimIndent()

                            // ✅ GUNAKAN CLASSIFICATION DARI API
                            if (!latest.classificationHeight.isNullOrEmpty() &&
                                !latest.classificationWeight.isNullOrEmpty()) {

                                val statusText = """
                                    TB: ${latest.classificationHeight}
                                    BB: ${latest.classificationWeight}
                                    Risk Level: ${latest.riskLevel ?: "Unknown"}
                                """.trimIndent()

                                tvStatus.text = "Status : $statusText"

                                val statusColor = StatusUtils.getCombinedStatusColor(
                                    this@ResultActivity,
                                    latest.classificationHeight,
                                    latest.classificationWeight
                                )
                                tvStatus.setTextColor(statusColor)

                                Log.d("RESULT_DEBUG", "API Classification - TB: ${latest.classificationHeight}, BB: ${latest.classificationWeight}")
                            } else {
                                // Fallback ke sistem lama
                                val statusText = when {
                                    zTb <= -3 -> "Stunting"
                                    zTb <= -1 -> "Berisiko Stunting"
                                    else -> "Normal"
                                }

                                tvStatus.text = "Status : $statusText"

                                when (statusText) {
                                    "Normal" -> tvStatus.setTextColor(getColor(R.color.green_status))
                                    "Berisiko Stunting" -> tvStatus.setTextColor(getColor(R.color.yellow_status))
                                    "Stunting" -> tvStatus.setTextColor(getColor(R.color.red_status))
                                    else -> tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                                }

                                Log.d("RESULT_DEBUG", "API Fallback - Z-Score TB: $zTbText, BB: $zBbText, Status: $statusText")
                            }
                        }
                    }

                    override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                        Log.e("RESULT", "Gagal ambil history", t)
                        runOnUiThread {
                            tvZScore.text = "Gagal memuat Z-Score"
                        }
                    }
                })
        } else {
            Log.d("RESULT_DEBUG", "Menggunakan Z-Score dari Intent (tidak perlu API)")
        }
    }

    private fun loadZScoreChartWithRetry(
        imageView: ImageView,
        url: String,
        retry: Int = 3,
        delayMs: Long = 1200
    ) {
        // ⚠️ Safety check: Jangan load jika activity sedang finishing/destroyed
        if (isFinishing || isDestroyed) {
            Log.w("Z_SCORE", "Activity is finishing/destroyed, skipping chart load")
            return
        }

        try {
            Glide.with(imageView)
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_loading) // jangan langsung error
                .into(imageView)
        } catch (e: Exception) {
            Log.e("Z_SCORE", "Failed to load zscore chart", e)
            return
        }

        imageView.postDelayed({
            if (!isFinishing && !isDestroyed && retry > 0) {
                Log.d("Z_SCORE", "Retry load zscore chart, remaining=$retry")
                loadZScoreChartWithRetry(
                    imageView,
                    url + "&retry=$retry",
                    retry - 1,
                    delayMs
                )
            }
        }, delayMs)
    }

}
