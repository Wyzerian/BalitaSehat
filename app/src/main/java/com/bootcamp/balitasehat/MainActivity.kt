package com.bootcamp.balitasehat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.network.ApiClient
import com.bootcamp.balitasehat.utils.StatusUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("USER", MODE_PRIVATE)

        // ===== HEADER =====
        val tvAppName = findViewById<TextView>(R.id.tvNamaAnak)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)

        tvAppName.text = "Balita Sehat"
        tvSubtitle.text = "Prediksi Stunting Balita Sehat"

        // ===== VIEW CARD =====
        val tvNamaCard = findViewById<TextView>(R.id.tvNamaCard)
        val tvBerat = findViewById<TextView>(R.id.tvBB)
        val tvTinggi = findViewById<TextView>(R.id.tvTB)

        val tvLastDate = findViewById<TextView>(R.id.tvLastDate)
        val tvLastDetail = findViewById<TextView>(R.id.tvLastDetail)
        val tvLastStatus = findViewById<TextView>(R.id.tvLastStatus)

        // ===== SHARED PREF =====



        // ===== AMBIL DATA DARI INTENT =====
        val nikFromIntent = intent.getStringExtra("nik_anak")
        val childId = intent.getStringExtra("child_id")
        val namaAnak = intent.getStringExtra("nama")

        // ===== SIMPAN KE PREF JIKA ADA =====
        if (!nikFromIntent.isNullOrEmpty()) {
            prefs.edit().putString("nik_anak", nikFromIntent).apply()
            Log.d("MAIN_DEBUG", "Saved nik_anak to SharedPreferences: $nikFromIntent")
        }
        if (!childId.isNullOrEmpty()) {
            prefs.edit().putString("child_id", childId).apply()
            Log.d("MAIN_DEBUG", "Saved child_id to SharedPreferences: $childId")
        }
        if (!namaAnak.isNullOrEmpty()) {
            prefs.edit().putString("nama", namaAnak).apply()
            Log.d("MAIN_DEBUG", "Saved nama to SharedPreferences: $namaAnak")
        }

        // ===== AMBIL DARI PREF (SUMBER UTAMA) =====
        val nikAnak = prefs.getString("nik_anak", null)
        val childIdFinal = prefs.getString("child_id", null)
        val namaFinal = prefs.getString("nama", "-")

        Log.d("MAIN_ACTIVITY", "NIK: $nikAnak | childId: $childIdFinal")
        Log.d("MAIN_DEBUG", "=== SharedPreferences Content ===")
        prefs.all.forEach { (key, value) ->
            Log.d("MAIN_DEBUG", "$key = $value")
        }

        // ===== SET NAMA =====
        tvNamaCard.text = namaFinal

        // ===== LOAD DATA TERAKHIR ATAU SET DEFAULT =====
        if (!childIdFinal.isNullOrEmpty()) {
            // Load data dari API, jika tidak ada akan set default
            loadLatestMeasurement(
                childIdFinal,
                tvBerat,
                tvTinggi,
                tvLastDate,
                tvLastDetail,
                tvLastStatus
            )

            // ===== LOAD GRAFIK TERAKHIR =====
            val imgChartTB = findViewById<ImageView>(R.id.imgChartTB)
            val imgChartBB = findViewById<ImageView>(R.id.imgChartBB)
            loadLatestCharts(childIdFinal, imgChartTB, imgChartBB)
        } else {
            // Jika child_id tidak ada, set default
            setDefaultMeasurement(
                tvBerat,
                tvTinggi,
                tvLastDate,
                tvLastDetail,
                tvLastStatus
            )
        }

        // ===== NAV INPUT =====
        findViewById<TextView>(R.id.navInput).setOnClickListener {

            if (nikAnak.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "NIK anak tidak ditemukan, silakan login ulang",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intent = Intent(this, InputData::class.java)
            intent.putExtra("nik_anak", nikAnak)      // ⭐ WAJIB
            intent.putExtra("nama_anak", namaAnak)    // ⭐ TAMBAHKAN INI
            startActivity(intent)
        }

        // ===== NAV HISTORY =====
        findViewById<TextView>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh data saat kembali dari InputData
        val childId = prefs.getString("child_id", null)
        if (!childId.isNullOrEmpty()) {
            val tvBerat = findViewById<TextView>(R.id.tvBB)
            val tvTinggi = findViewById<TextView>(R.id.tvTB)
            val tvLastDate = findViewById<TextView>(R.id.tvLastDate)
            val tvLastDetail = findViewById<TextView>(R.id.tvLastDetail)
            val tvLastStatus = findViewById<TextView>(R.id.tvLastStatus)

            loadLatestMeasurement(
                childId,
                tvBerat,
                tvTinggi,
                tvLastDate,
                tvLastDetail,
                tvLastStatus
            )

            // ===== REFRESH GRAFIK =====
            val imgChartTB = findViewById<ImageView>(R.id.imgChartTB)
            val imgChartBB = findViewById<ImageView>(R.id.imgChartBB)
            loadLatestCharts(childId, imgChartTB, imgChartBB)
        }
    }

    private fun setDefaultMeasurement(
        tvBerat: TextView,
        tvTinggi: TextView,
        tvLastDate: TextView,
        tvLastDetail: TextView,
        tvLastStatus: TextView
    ) {
        tvBerat.text = "Berat Badan (kg)\n0.0"
        tvTinggi.text = "Tinggi Badan (cm)\n0.0"
        tvLastDate.text = "Belum ada pemeriksaan"
        tvLastDetail.text = "-"
        tvLastStatus.text = "Status: -"
        tvLastStatus.setTextColor(getColor(android.R.color.darker_gray))

        // ✅ Clear last_bb dan last_tb dari SharedPreferences
        prefs.edit()
            .remove("last_bb")
            .remove("last_tb")
            .apply()
    }

    private fun loadLatestMeasurement(
        childId: String,
        tvBerat: TextView,
        tvTinggi: TextView,
        tvLastDate: TextView,
        tvLastDetail: TextView,
        tvLastStatus: TextView
    ) {
        ApiClient.apiService.getChildHistory(childId)
            .enqueue(object : Callback<HistoryResponse> {

                override fun onResponse(
                    call: Call<HistoryResponse>,
                    response: Response<HistoryResponse>
                ) {
                    val list = response.body()?.data
                    if (list.isNullOrEmpty()) {
                        // ✅ Set default jika belum ada data measurement
                        setDefaultMeasurement(tvBerat, tvTinggi, tvLastDate, tvLastDetail, tvLastStatus)
                        return
                    }

                    // 🔥 DATA TERAKHIR
                    val latest = list.last()

                    tvBerat.text = "Berat Badan (kg)\n${latest.weightKg}"
                    tvTinggi.text = "Tinggi Badan (cm)\n${latest.heightCm}"

                    prefs.edit()
                        .putFloat("last_bb", latest.weightKg.toFloat())
                        .putFloat("last_tb", latest.heightCm.toFloat())
                        .apply()

                    tvLastDate.text = "Tanggal: ${latest.measurementDate}"
                    tvLastDetail.text =
                        "Umur: ${latest.ageMonths} bln | TB: ${latest.heightCm} cm | BB: ${latest.weightKg} kg"

                    // ✅ GUNAKAN CLASSIFICATION BARU
                    if (!latest.classificationHeight.isNullOrEmpty() &&
                        !latest.classificationWeight.isNullOrEmpty()) {

                        val statusText = "TB: ${latest.classificationHeight} | BB: ${latest.classificationWeight}"
                        tvLastStatus.text = "Status: $statusText"

                        val statusColor = StatusUtils.getCombinedStatusColor(
                            this@MainActivity,
                            latest.classificationHeight,
                            latest.classificationWeight
                        )
                        tvLastStatus.setTextColor(statusColor)
                    } else {
                        // Fallback ke status lama
                        val status = latest.stuntingStatus ?: "Unknown"
                        tvLastStatus.text = "Status: $status"

                        when (status) {
                            "Normal" ->
                                tvLastStatus.setTextColor(getColor(R.color.green_status))
                            "At Risk" ->
                                tvLastStatus.setTextColor(getColor(R.color.yellow_status))
                            "Stunted" ->
                                tvLastStatus.setTextColor(getColor(R.color.red_status))
                            else ->
                                tvLastStatus.setTextColor(getColor(android.R.color.darker_gray))
                        }
                    }
                }

                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    Log.e("MAIN_ACTIVITY", "Load history failed", t)
                }
            })
    }

    private fun loadLatestCharts(
        childId: String,
        imgChartTB: ImageView,
        imgChartBB: ImageView
    ) {
        val timestamp = System.currentTimeMillis()

        // URL untuk grafik growth (tinggi badan) dan zscore (berat badan)
        val growthChartUrl = "http://144.208.67.53:8000/static/charts/${childId}_growth.png?t=$timestamp"
        val zscoreChartUrl = "http://144.208.67.53:8000/static/charts/${childId}_zscore.png?t=$timestamp"

        Log.d("MAIN_CHART", "Loading growth chart: $growthChartUrl")
        Log.d("MAIN_CHART", "Loading zscore chart: $zscoreChartUrl")

        // Load Growth Chart (Tinggi Badan)
        Glide.with(this)
            .load(growthChartUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.img_dummy_chart)
            .error(R.drawable.img_dummy_chart)
            .into(imgChartTB)

        // Load Z-Score Chart (Berat Badan)
        Glide.with(this)
            .load(zscoreChartUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.img_dummy_chart)
            .error(R.drawable.img_dummy_chart)
            .into(imgChartBB)
    }
}

