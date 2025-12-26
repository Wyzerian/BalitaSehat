package com.bootcamp.balitasehat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.network.ApiClient
import com.bootcamp.balitasehat.network.ApiService
import com.bootcamp.balitasehat.model.CheckNikResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme normal sebelum super.onCreate() untuk mengganti splash screen
        setTheme(R.style.Theme_BalitaSehat)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etNik = findViewById<EditText>(R.id.etNik)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)

        // Retrofit init
        apiService = ApiClient.apiService

        btnLogin.setOnClickListener {
            val nikInput = etNik.text.toString().trim()

            if (nikInput.isEmpty()) {
                Toast.makeText(this, "Masukkan NIK", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔄 PANGGIL API CHECK NIK
            apiService.checkNik(nikInput).enqueue(object : Callback<CheckNikResponse> {

                override fun onResponse(
                    call: Call<CheckNikResponse>,
                    response: Response<CheckNikResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val result = response.body()!!

                        // 🔍 DEBUG: Log API Response
                        Log.d("LOGIN_API", "=== API RESPONSE DEBUG ===")
                        Log.d("LOGIN_API", "Status: ${result.status}")
                        Log.d("LOGIN_API", "Data: ${result.data}")
                        Log.d("LOGIN_API", "child_id: ${result.data?.childId}")
                        Log.d("LOGIN_API", "nik_anak: ${result.data?.nikAnak}")
                        Log.d("LOGIN_API", "nama: ${result.data?.name}")
                        Log.d("LOGIN_API", "gender: ${result.data?.gender}")
                        Log.d("LOGIN_API", "birth_date: ${result.data?.birthDate}")

                        if (result.status == "found") {

                            // ⚠️ CRITICAL: Check if child_id exists
                            val childId = result.data?.childId
                            if (childId.isNullOrEmpty()) {
                                Log.e("LOGIN_API", "⚠️ child_id is NULL from backend!")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error: child_id tidak ditemukan di server. Hubungi admin.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            }

                            val prefs = getSharedPreferences("USER", MODE_PRIVATE)

                            // ✅ CLEAR data user sebelumnya
                            prefs.edit().clear().apply()

                            // ✅ Simpan data user baru
                            prefs.edit()
                                .putString("child_id", childId)
                                .putString("nik_anak", result.data?.nikAnak)
                                .putString("nama", result.data?.name)
                                .apply()

                            // 🔍 Verify data saved
                            Log.d("LOGIN_API", "=== SAVED TO SHAREDPREFERENCES ===")
                            Log.d("LOGIN_API", "child_id: ${prefs.getString("child_id", null)}")
                            Log.d("LOGIN_API", "nik_anak: ${prefs.getString("nik_anak", null)}")
                            Log.d("LOGIN_API", "nama: ${prefs.getString("nama", null)}")

                            // ✅ LOGIN BERHASIL
                            Toast.makeText(
                                this@LoginActivity,
                                "Login berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("nik_anak", result.data?.nikAnak)
                            intent.putExtra("child_id", childId)
                            intent.putExtra("nama", result.data?.name)
                            intent.putExtra("gender", result.data?.gender)
                            intent.putExtra("birth_date", result.data?.birthDate)
                            startActivity(intent)
                            finish()

                        } else {
                            // ❌ NIK TIDAK DITEMUKAN
                            Toast.makeText(
                                this@LoginActivity,
                                "NIK belum terdaftar, silakan Register",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Log.e("LOGIN_API", "Response error: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@LoginActivity,
                            "Gagal login (server error)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CheckNikResponse>, t: Throwable) {
                    Log.e("LOGIN_API", "API gagal", t)
                    Toast.makeText(
                        this@LoginActivity,
                        "Tidak dapat terhubung ke server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // ➕ PINDAH KE REGISTER
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
