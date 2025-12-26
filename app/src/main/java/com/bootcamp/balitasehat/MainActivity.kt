package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.AddMeasurementResponse
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val prefs = getSharedPreferences("USER", MODE_PRIVATE)

        // ===== AMBIL DATA DARI INTENT =====
        val nikFromIntent = intent.getStringExtra("nik_anak")
        val childId = intent.getStringExtra("child_id")
        val namaAnak = intent.getStringExtra("nama")

        // ===== SIMPAN KE PREF JIKA ADA =====
        if (!nikFromIntent.isNullOrEmpty()) {
            prefs.edit().putString("nik_anak", nikFromIntent).apply()
        }
        if (!childId.isNullOrEmpty()) {
            prefs.edit().putString("child_id", childId).apply()
        }
        if (!namaAnak.isNullOrEmpty()) {
            prefs.edit().putString("nama", namaAnak).apply()
        }

        // ===== AMBIL DARI PREF (SUMBER UTAMA) =====
        val nikAnak = prefs.getString("nik_anak", null)
        val childIdFinal = prefs.getString("child_id", null)
        val namaFinal = prefs.getString("nama", "-")

        Log.d("MAIN_ACTIVITY", "NIK: $nikAnak | childId: $childIdFinal")

        // ===== SET NAMA =====
        tvNamaCard.text = namaFinal

        // ===== DEFAULT =====
        setDefaultMeasurement(tvBerat, tvTinggi, tvLastDate, tvLastDetail, tvLastStatus)

        // ===== LOAD DATA TERAKHIR =====
        if (!childIdFinal.isNullOrEmpty()) {
            loadLatestMeasurement(
                childIdFinal,
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
        ApiClient.apiService.getChildHistory(childId)
            .enqueue(object : Callback<HistoryResponse> {

                override fun onResponse(
                    call: Call<HistoryResponse>,
                    response: Response<HistoryResponse>
                ) {
                    val list = response.body()?.data
                    if (list.isNullOrEmpty()) return

                    // 🔥 DATA TERAKHIR
                    val latest = list.last()

                    tvBerat.text = "Berat Badan (kg)\n${latest.weightKg}"
                    tvTinggi.text = "Tinggi Badan (cm)\n${latest.heightCm}"

                    tvLastDate.text = "Tanggal: ${latest.measurementDate}"
                    tvLastDetail.text =
                        "Umur: ${latest.ageMonths} bln | TB: ${latest.heightCm} cm | BB: ${latest.weightKg} kg"

                    val status = latest.stuntingStatus
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

                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    Log.e("MAIN_ACTIVITY", "Load history failed", t)
                }
            })
    }
}

