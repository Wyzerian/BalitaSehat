package com.bootcamp.balitasehat

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.AddMeasurementRequest
import com.bootcamp.balitasehat.model.AddMeasurementResponse
import com.bootcamp.balitasehat.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.util.Calendar

class InputData : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_data)

        Log.d("INPUT_DATA", "InputData dibuka")

        // ===== VIEW =====
        val etNama = findViewById<EditText>(R.id.etNama)
        val etTanggal = findViewById<EditText>(R.id.etTanggal)
        val etTinggi = findViewById<EditText>(R.id.etTinggi)
        val etBerat = findViewById<EditText>(R.id.etBerat)
        val btnProses = findViewById<Button>(R.id.btnProses)

        // ===== DATA DARI MAIN =====
        val nikAnak = intent.getStringExtra("nik_anak")
        val namaAnak = intent.getStringExtra("nama_anak")

        if (nikAnak.isNullOrEmpty()) {
            Toast.makeText(this, "NIK anak tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ===== SET NAMA OTOMATIS =====
        etNama.setText(namaAnak ?: "-")
        etNama.isEnabled = false

        // ===== DATE PICKER =====
        etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val date = LocalDate.of(year, month + 1, day)
                    etTanggal.setText(date.toString())
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // ===== SUBMIT =====
        btnProses.setOnClickListener {

            val tinggi = etTinggi.text.toString().toDoubleOrNull()
            val berat = etBerat.text.toString().toDoubleOrNull()
            val tanggal = etTanggal.text.toString()

            if (tinggi == null || berat == null || tanggal.isEmpty()) {
                Toast.makeText(this, "Data belum lengkap", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = AddMeasurementRequest(
                nikAnak = nikAnak,
                heightCm = tinggi,
                weightKg = berat,
                measurementDate = tanggal
            )

            ApiClient.apiService.addMeasurement(request)
                .enqueue(object : Callback<AddMeasurementResponse> {

                    override fun onResponse(
                        call: Call<AddMeasurementResponse>,
                        response: Response<AddMeasurementResponse>
                    ) {
                        val result = response.body()?.data ?: run {
                            Toast.makeText(
                                this@InputData,
                                "Gagal memproses data",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        val childId = result.child.childId

                        // 🔄 TRIGGER GENERATE CHART (NON-BLOCKING)
                        ApiClient.apiService.generateChart(childId, "growth")
                            .enqueue(SimpleCallback())
                        ApiClient.apiService.generateChart(childId, "zscore")
                            .enqueue(SimpleCallback())

                        // 🔀 PINDAH KE RESULT
                        val intent = Intent(this@InputData, ResultActivity::class.java)

                        // ===== CHILD =====
                        intent.putExtra("nama", result.child.name)
                        intent.putExtra("age_months", result.child.ageMonths)
                        intent.putExtra("child_id", childId)

                        // ===== MEASUREMENT =====
                        intent.putExtra("height_cm", result.measurement.heightCm)
                        intent.putExtra("weight_kg", result.measurement.weightKg)
                        intent.putExtra("measurement_date", result.measurement.measurementDate)

                        // ===== CLASSIFICATION (HASIL BACKEND) =====
                        intent.putExtra("height_zscore", result.classification.heightZscore)
                        intent.putExtra("weight_zscore", result.classification.weightZscore)
                        intent.putExtra("height_status", result.classification.classificationHeight)
                        intent.putExtra("weight_status", result.classification.classificationWeight)
                        intent.putExtra("risk_level", result.classification.riskLevel)

                        // ===== CHART URL (STATIC FILE) =====
                        intent.putExtra(
                            "chart_growth",
                            "http://144.208.67.53:8000/static/charts/${childId}_growth.png"
                        )
                        intent.putExtra(
                            "chart_zscore",
                            "http://144.208.67.53:8000/static/charts/${childId}_zscore.png"
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(intent)
                            finish()
                        }, 800)

                    }

                    override fun onFailure(
                        call: Call<AddMeasurementResponse>,
                        t: Throwable
                    ) {
                        Log.e("INPUT_DATA", "API gagal", t)
                        Toast.makeText(
                            this@InputData,
                            "Gagal terhubung ke server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}

// ===== SIMPLE CALLBACK UNTUK GENERATE CHART =====
class SimpleCallback : Callback<Void> {
    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        Log.d("CHART", "Chart generated")
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Log.e("CHART", "Generate chart gagal", t)
    }
}
