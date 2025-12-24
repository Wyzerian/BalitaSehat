package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.network.ApiClient
import com.bootcamp.balitasehat.model.AddMeasurementResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== HEADER (STATIC) =====
        val tvAppName = findViewById<TextView>(R.id.tvNamaAnak)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)

        tvAppName.text = "Balita Sehat"
        tvSubtitle.text = "Prediksi Stunting Balita Sehat"

        // ===== CARD PROFIL ANAK =====
        val tvNamaCard = findViewById<TextView>(R.id.tvNamaCard)
        val tvBerat = findViewById<TextView>(R.id.tvBB)
        val tvTinggi = findViewById<TextView>(R.id.tvTB)

        // ===== RIWAYAT TERAKHIR =====
        val tvLastDate = findViewById<TextView>(R.id.tvLastDate)
        val tvLastDetail = findViewById<TextView>(R.id.tvLastDetail)
        val tvLastStatus = findViewById<TextView>(R.id.tvLastStatus)

        // ===== DATA DARI LOGIN =====
        val childId = intent.getStringExtra("child_id")
        val namaAnak = intent.getStringExtra("nama") ?: "-"

        tvNamaCard.text = namaAnak

        // ===== DEFAULT (BELUM ADA DATA) =====
        setDefaultMeasurement(tvBerat, tvTinggi, tvLastDate, tvLastDetail, tvLastStatus)

        // ===== LOAD MEASUREMENT TERAKHIR =====
        if (!childId.isNullOrEmpty()) {
            loadLatestMeasurement(
                childId,
                tvBerat,
                tvTinggi,
                tvLastDate,
                tvLastDetail,
                tvLastStatus
            )
        }

        // ===== BOTTOM NAV =====
        findViewById<TextView>(R.id.navInput).setOnClickListener {
            startActivity(Intent(this, InputData::class.java))
        }

        findViewById<TextView>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun setDefaultMeasurement(
        tvBerat: TextView,
        tvTinggi: TextView,
        tvLastDate: TextView,
        tvLastDetail: TextView,
        tvLastStatus: TextView
    ) {
        tvBerat.text = "Berat Badan (kg)\n0"
        tvTinggi.text = "Tinggi Badan (cm)\n0"

        tvLastDate.text = "Belum ada pemeriksaan"
        tvLastDetail.text = "-"
        tvLastStatus.text = "Status: -"
        tvLastStatus.setTextColor(getColor(android.R.color.darker_gray))
    }

    private fun loadLatestMeasurement(
        childId: String,
        tvBerat: TextView,
        tvTinggi: TextView,
        tvLastDate: TextView,
        tvLastDetail: TextView,
        tvLastStatus: TextView
    ) {
        ApiClient.apiService.getLatestMeasurement(childId)
            .enqueue(object : Callback<AddMeasurementResponse> {

                override fun onResponse(
                    call: Call<AddMeasurementResponse>,
                    response: Response<AddMeasurementResponse>
                ) {
                    val data = response.body()?.data
                    if (!response.isSuccessful || data == null) return

                    tvBerat.text = "Berat Badan (kg)\n${data.weightKg}"
                    tvTinggi.text = "Tinggi Badan (cm)\n${data.heightCm}"

                    tvLastDate.text = "Tanggal: ${data.measurementId}" // ganti jika API punya date
                    tvLastDetail.text =
                        "Umur: ${data.ageMonths} bln | TB: ${data.heightCm} cm | BB: ${data.weightKg} kg"

                    val status = data.classification.heightStatus
                    tvLastStatus.text = "Status: $status"

                    when (status) {
                        "Normal" -> tvLastStatus.setTextColor(getColor(R.color.green_status))
                        "Berisiko Stunting" -> tvLastStatus.setTextColor(getColor(R.color.yellow_status))
                        "Stunting" -> tvLastStatus.setTextColor(getColor(R.color.red_status))
                    }
                }

                override fun onFailure(call: Call<AddMeasurementResponse>, t: Throwable) {
                    Log.e("MAIN_ACTIVITY", "Load measurement failed", t)
                }
            })
    }
}
