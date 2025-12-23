package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvDetail = findViewById<TextView>(R.id.tvDetail)
        val btnInputData = findViewById<Button>(R.id.btnInputData)
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        // 🔹 Ambil data TERBARU anak (berdasarkan NIK login)
        val pref = getSharedPreferences("current_data", MODE_PRIVATE)

        val nama = pref.getString("nama", "-") ?: "-"
        val gender = pref.getString("gender", "-") ?: "-"
        val tanggalLahir = pref.getString("tanggal_lahir", "-") ?: "-"
        val tinggi = pref.getString("tinggi", "-") ?: "-"
        val berat = pref.getString("berat", "-") ?: "-"

        // ⭐ HITUNG UMUR OTOMATIS (BULAN)
        val umurBulan = if (tanggalLahir != "-") {
            hitungUmurBulan(tanggalLahir)
        } else {
            0
        }

        tvNama.text = nama

        tvDetail.text = """
            Tanggal Lahir : $tanggalLahir
            Umur          : $umurBulan bulan
            Gender        : $gender
            Tinggi Badan  : $tinggi cm
            Berat Badan   : $berat kg
        """.trimIndent()

        // ➕ Tombol Input / Update Data
        btnInputData.setOnClickListener {
            startActivity(Intent(this, InputData::class.java))
        }

        // 📜 Tombol Lihat History
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    // ===============================
    // HITUNG UMUR DARI TANGGAL LAHIR
    // ===============================
    private fun hitungUmurBulan(tanggalLahir: String): Int {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val birthDate = sdf.parse(tanggalLahir) ?: return 0

        val calBirth = Calendar.getInstance().apply { time = birthDate }
        val calNow = Calendar.getInstance()

        var umur = (calNow.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR)) * 12
        umur += calNow.get(Calendar.MONTH) - calBirth.get(Calendar.MONTH)

        if (calNow.get(Calendar.DAY_OF_MONTH) < calBirth.get(Calendar.DAY_OF_MONTH)) {
            umur--
        }

        return umur.coerceAtLeast(0)
    }
}