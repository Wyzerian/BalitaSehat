package com.bootcamp.balitasehat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bootcamp.balitasehat.model.HistoryAdapter
import com.bootcamp.balitasehat.model.HistoryModel
import com.bootcamp.balitasehat.model.HistoryResponse
import com.bootcamp.balitasehat.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_history)

            val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
            val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)

            rvHistory.layoutManager = LinearLayoutManager(this)

            // Get child_id from SharedPreferences
            val prefs = getSharedPreferences("USER", MODE_PRIVATE)
            val childId = prefs.getString("child_id", null)
            val namaAnak = prefs.getString("nama", "-")
            val nikAnak = prefs.getString("nik_anak", null)

            // Debug logging
            Log.d("HISTORY_DEBUG", "=== DEBUG INFO ===")
            Log.d("HISTORY_DEBUG", "child_id: $childId")
            Log.d("HISTORY_DEBUG", "nama: $namaAnak")
            Log.d("HISTORY_DEBUG", "nik_anak: $nikAnak")
            Log.d("HISTORY_DEBUG", "SharedPreferences contains:")
            prefs.all.forEach { (key, value) ->
                Log.d("HISTORY_DEBUG", "  $key = $value")
            }

            if (childId.isNullOrEmpty()) {
                tvEmpty.text = "Data login tidak ditemukan.\nSilakan login ulang."
                tvEmpty.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
                progressBar.visibility = View.GONE

                Log.e("HISTORY_DEBUG", "child_id is null or empty! User needs to login again.")
                return
            }

            // Show loading
            progressBar.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
            tvEmpty.visibility = View.GONE

            Log.d("HISTORY", "Loading history for child_id: $childId")

            // Load from API
            ApiClient.apiService.getChildHistory(childId)
                .enqueue(object : Callback<HistoryResponse> {
                    override fun onResponse(
                        call: Call<HistoryResponse>,
                        response: Response<HistoryResponse>
                    ) {
                        progressBar.visibility = View.GONE

                        if (!response.isSuccessful) {
                            Log.e("HISTORY", "API error: ${response.code()}")
                            tvEmpty.visibility = View.VISIBLE
                            tvEmpty.text = "Gagal memuat data (Error ${response.code()})"
                            return
                        }

                        val historyItems = response.body()?.data

                        if (historyItems.isNullOrEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                            tvEmpty.text = "Belum ada riwayat pemeriksaan"
                            Log.d("HISTORY", "No history data found")
                            return
                        }

                        Log.d("HISTORY", "Loaded ${historyItems.size} history items")

                        // Convert HistoryItem to HistoryModel
                        val listHistory = historyItems.map { item ->
                            HistoryModel(
                                childId = childId,
                                nama = namaAnak ?: "-",
                                umur = item.ageMonths.toString(),
                                gender = "-",
                                tinggi = item.heightCm.toString(),
                                berat = item.weightKg.toString(),
                                tanggalLahir = "-",
                                tanggalInput = item.measurementDate,
                                zscoreHeight = item.zscoreHeight,
                                zscoreWeight = item.zscoreWeight,
                                classificationHeight = item.classificationHeight,
                                classificationWeight = item.classificationWeight,
                                riskLevel = item.riskLevel,
                                stuntingStatus = item.stuntingStatus,
                                wastingStatus = item.wastingStatus
                            )
                        }.reversed()

                        rvHistory.visibility = View.VISIBLE
                        rvHistory.adapter = HistoryAdapter(listHistory)
                    }

                    override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Gagal terhubung ke server: ${t.message}"
                        Log.e("HISTORY", "Load failed", t)
                    }
                })
        } catch (e: Exception) {
            Log.e("HISTORY_CRASH", "onCreate crashed", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

}
