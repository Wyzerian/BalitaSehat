package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== Header Profil =====
        val tvNamaAnak = findViewById<TextView>(R.id.tvNamaAnak)
        val tvSubTitle = findViewById<TextView>(R.id.tvSubtitle)

        // ===== Card Input =====
        val tvBerat = findViewById<TextView>(R.id.tvBB)
        val tvTinggi = findViewById<TextView>(R.id.tvTB)

        // ===== Riwayat Pemeriksaan =====
        val tvLastDate = findViewById<TextView>(R.id.tvLastDate)
        val tvLastDetail = findViewById<TextView>(R.id.tvLastDetail)
        val tvLastStatus = findViewById<TextView>(R.id.tvLastStatus)

        // ===== Ambil data terbaru =====
        val pref = getSharedPreferences("current_data", MODE_PRIVATE)

        val nama = pref.getString("nama", "-")
        val umur = pref.getString("umur", "-")
        val gender = pref.getString("gender", "-")
        val tinggi = pref.getString("tinggi", "-")
        val berat = pref.getString("berat", "-")
        val tanggalInput = pref.getString("tanggal_input", "-")

        // ===== Set Header =====
        tvNamaAnak.text = nama
        tvSubTitle.text = "Prediksi Stunting Balita Sehat"

        // ===== Set Card Input =====
        tvBerat.text = "Berat Badan (kg)\n$berat"
        tvTinggi.text = "Tinggi Badan (cm)\n$tinggi"

        // ===== Set Riwayat Terakhir =====
        tvLastDate.text = "Tanggal: $tanggalInput"
        tvLastDetail.text = "Umur: $umur bln | TB: $tinggi cm | BB: $berat kg"

        // Status sederhana (opsional bisa dari z-score)
        val status = pref.getString("status", "-") ?: "-"

        tvLastStatus.text = "Status: $status"

        when (status) {
            "Normal" -> {
                tvLastStatus.setTextColor(getColor(R.color.green_status))
            }
            "Berisiko Stunting" -> {
                tvLastStatus.setTextColor(getColor(R.color.yellow_status))
            }
            "Stunting" -> {
                tvLastStatus.setTextColor(getColor(R.color.red_status))
            }
            else -> {
                tvLastStatus.setTextColor(getColor(android.R.color.darker_gray))
            }
        }

        // ===== Tombol Input Data (Bottom Nav) =====
        val navInput = findViewById<TextView>(R.id.navInput)

        navInput.setOnClickListener {
            val intent = Intent(this, InputData::class.java)
            startActivity(intent)
        }

        // ===== Tombol Riwayat (Bottom Nav) =====
        val navHistory = findViewById<TextView>(R.id.navHistory)

        navHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }
}
