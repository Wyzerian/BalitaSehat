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

                        if (result.status == "found") {
                            // ✅ LOGIN BERHASIL
                            Toast.makeText(
                                this@LoginActivity,
                                "Login berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("nik", result.data?.nikAnak)
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
