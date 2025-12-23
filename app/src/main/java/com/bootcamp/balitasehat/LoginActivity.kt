package com.bootcamp.balitasehat

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etNik = findViewById<EditText>(R.id.etNik)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val nik = etNik.text.toString().trim()

            if (nik != "1234567890123456") {
                Toast.makeText(this, "NIK tidak terdaftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ===============================
            // 📌 DATA DUMMY (SEMENTARA)
            // ===============================
            val tanggalLahirDummy = "24-07-2024" // < 24 bulan
            val namaDummy = "Budi"
            val genderDummy = "Laki-laki"

            val pref = getSharedPreferences("current_data", MODE_PRIVATE)
            pref.edit()
                .putString("nik", nik)
                .putString("nama", namaDummy)
                .putString("gender", genderDummy)
                .putString("tanggal_lahir", tanggalLahirDummy)
                .apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}
