package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        val nama = pref.getString("nama", "-")
        val umur = pref.getString("umur", "-")
        val gender = pref.getString("gender", "-")
        val tinggi = pref.getString("tinggi", "-")
        val berat = pref.getString("berat", "-")

        tvNama.text = nama
        tvDetail.text = """
            Umur   : $umur bulan
            Gender : $gender
            TB     : $tinggi cm
            BB     : $berat kg
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
}
