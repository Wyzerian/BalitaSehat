package com.bootcamp.balitasehat

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.network.ApiClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

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

        // ===== DATA DARI INPUTDATA =====
        val nama = intent.getStringExtra("nama") ?: "-"
        val umur = intent.getIntExtra("age_months", 0)
        val tinggi = intent.getDoubleExtra("height_cm", 0.0)
        val berat = intent.getDoubleExtra("weight_kg", 0.0)
        val tanggal = intent.getStringExtra("measurement_date") ?: "-"

        var status: String? = null

        // ===== SET TEXT =====
        tvNama.text = nama
        tvUmur.text = "Umur : $umur bulan"
        tvTinggi.text = "Tinggi : $tinggi cm"
        tvBerat.text = "Berat : $berat kg"
        tvTanggal.text = "Tanggal : $tanggal"
        tvStatus.text = "Status : -"
        tvStatus.setTextColor(getColor(android.R.color.darker_gray))

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

        // ===== LOAD GRAFIK =====
        loadZScoreChartWithRetry(
            imgZscore,
            zscoreUrl
        )

        imgGrowth.postDelayed({
            Glide.with(this)
                .load(growthUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .into(imgGrowth)
        }, 1500)

        ApiClient.apiService.getChildHistory(childId)
            .enqueue(object : Callback<HistoryResponse> {
                override fun onResponse(
                    call: Call<HistoryResponse>,
                    response: Response<HistoryResponse> ) {
                    val body = response.body()
                    if (body == null || body.data.isEmpty()) return

                    val latest = body.data.maxByOrNull { it.measurementDate } ?: return

                    val zTb = latest.zscoreHeight

                    val zBb = latest.zscoreWeight
                    val zTbText = if (zTb.isFinite()) "%.2f".format(zTb) else "-"
                    val zBbText = if (zBb.isFinite()) "%.2f".format(zBb) else "-"

                    runOnUiThread { tvZScore.text = """ 
                        Z-Score TB/U : $zTbText 
                        Z-Score BB/U : $zBbText 
                        """.trimIndent()

                        val statusText = when {
                            zTb <= -3 -> "Stunting"
                            zTb <= -1 -> "Berisiko Stunting"
                            else -> "Normal"
                        }

                        tvStatus.text = "Status : $statusText"

                        when (statusText) {
                            "Normal" -> tvStatus.setTextColor(getColor(R.color.green_status))
                            "At Risk" -> tvStatus.setTextColor(getColor(R.color.yellow_status))
                            "Stunted" -> tvStatus.setTextColor(getColor(R.color.red_status))
                            else -> tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                        }
                    }
                }
                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    Log.e("RESULT", "Gagal ambil history", t)
                }
            })
    }

    private fun loadZScoreChartWithRetry(
        imageView: ImageView,
        url: String,
        retry: Int = 3,
        delayMs: Long = 1200
    ) {
        Glide.with(imageView)
            .load(url)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.ic_loading) // jangan langsung error
            .into(imageView)

        imageView.postDelayed({
            if (retry > 0) {
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
