package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class InputData : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_data)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etUmur = findViewById<EditText>(R.id.etUmur)
        val etTinggi = findViewById<EditText>(R.id.etTinggi)
        val etBerat = findViewById<EditText>(R.id.etBerat)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val btnProses = findViewById<Button>(R.id.btnProses)

        // 🔒 Umur TIDAK bisa diedit (selalu otomatis)
        etUmur.isEnabled = false

        val fromHistory = intent.getBooleanExtra("from_history", false)

        if (fromHistory) {
            // ===============================
            // DATA DARI HISTORY (READ-ONLY)
            // ===============================
            etNama.setText(intent.getStringExtra("nama") ?: "")
            etUmur.setText(intent.getStringExtra("umur") ?: "")
            etTinggi.setText(intent.getStringExtra("tinggi") ?: "")
            etBerat.setText(intent.getStringExtra("berat") ?: "")

            when (intent.getStringExtra("gender")) {
                "Laki-laki" -> rgGender.check(R.id.rbLaki)
                "Perempuan" -> rgGender.check(R.id.rbPerempuan)
            }
        } else {
            // ===============================
            // DATA TERBARU (DARI LOGIN)
            // ===============================
            val pref = getSharedPreferences("current_data", MODE_PRIVATE)

            val nama = pref.getString("nama", "")
            val tanggalLahir = pref.getString("tanggal_lahir", "")
            val tinggi = pref.getString("tinggi", "")
            val berat = pref.getString("berat", "")
            val gender = pref.getString("gender", "")

            etNama.setText(nama)

            // ⭐ HITUNG UMUR OTOMATIS DARI TANGGAL LAHIR
            if (!tanggalLahir.isNullOrEmpty()) {
                val umurBulan = hitungUmurBulan(tanggalLahir)
                etUmur.setText(umurBulan.toString())
            }

            etTinggi.setText(tinggi)
            etBerat.setText(berat)

            when (gender) {
                "Laki-laki" -> rgGender.check(R.id.rbLaki)
                "Perempuan" -> rgGender.check(R.id.rbPerempuan)
            }
        }

        // 🔒 Nama selalu terkunci
        etNama.isEnabled = false

        btnProses.setOnClickListener {

            val nama = etNama.text.toString().trim()
            val umurStr = etUmur.text.toString().trim()
            val tinggiStr = etTinggi.text.toString().trim()
            val beratStr = etBerat.text.toString().trim()

            if (umurStr.isEmpty() || tinggiStr.isEmpty() || beratStr.isEmpty()) {
                Toast.makeText(this, "Data belum lengkap!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGenderId = rgGender.checkedRadioButtonId
            if (selectedGenderId == -1) {
                Toast.makeText(this, "Pilih jenis kelamin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val umur = umurStr.toIntOrNull()
            val tinggi = tinggiStr.toDoubleOrNull()
            val berat = beratStr.toDoubleOrNull()

            if (umur == null || umur !in 0..24) {
                Toast.makeText(this, "Umur tidak valid (0–24 bulan)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tinggi == null || tinggi <= 0) {
                Toast.makeText(this, "Tinggi badan tidak valid (cm)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (berat == null || berat <= 0) {
                Toast.makeText(this, "Berat badan tidak valid (kg)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = findViewById<RadioButton>(selectedGenderId).text.toString()

            // ===============================
            // SIMPAN HISTORY
            // ===============================
            saveToHistory(
                nama = nama,
                umur = umur.toString(),
                gender = gender,
                tinggi = tinggi.toString(),
                berat = berat.toString()
            )

            // ===============================
            // SIMPAN DATA TERBARU
            // ===============================
            saveCurrentData(
                nama = nama,
                umur = umur.toString(),
                gender = gender,
                tinggi = tinggi.toString(),
                berat = berat.toString()
            )

            // ===============================
            // KE RESULT
            // ===============================
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("nama", nama)
            intent.putExtra("umur", umur)
            intent.putExtra("gender", gender)
            intent.putExtra("tinggi", tinggi)
            startActivity(intent)
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

    private fun saveToHistory(
        nama: String,
        umur: String,
        gender: String,
        tinggi: String,
        berat: String
    ) {
        val pref = getSharedPreferences("history_data", MODE_PRIVATE)
        val jsonArray = org.json.JSONArray(pref.getString("history_list", "[]"))

        val obj = org.json.JSONObject().apply {
            put("nama", nama)
            put("umur", umur)
            put("gender", gender)
            put("tinggi", tinggi)
            put("berat", berat)
        }

        jsonArray.put(obj)
        pref.edit().putString("history_list", jsonArray.toString()).apply()
    }

    private fun saveCurrentData(
        nama: String,
        umur: String,
        gender: String,
        tinggi: String,
        berat: String
    ) {
        getSharedPreferences("current_data", MODE_PRIVATE).edit()
            .putString("nama", nama)
            .putString("umur", umur) // hanya untuk cache tampilan
            .putString("gender", gender)
            .putString("tinggi", tinggi)
            .putString("berat", berat)
            .apply()
    }
}
