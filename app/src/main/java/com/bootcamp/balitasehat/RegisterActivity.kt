package com.bootcamp.balitasehat

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.bootcamp.balitasehat.model.RegisterChildRequest
import com.bootcamp.balitasehat.model.RegisterChildResponse


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNik = findViewById<EditText>(R.id.etNik)
        val etNama = findViewById<EditText>(R.id.etNama)
        val etTanggalLahir = findViewById<EditText>(R.id.etTanggalLahir)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // 🔗 LINK KE LOGIN
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 📅 Date Picker
        etTanggalLahir.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    cal.set(y, m, d)
                    etTanggalLahir.setText(sdf.format(cal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnRegister.setOnClickListener {

            val nik = etNik.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val tanggalLahir = etTanggalLahir.text.toString().trim()
            val genderId = rgGender.checkedRadioButtonId

            if (nik.isEmpty() || nama.isEmpty() || tanggalLahir.isEmpty() || genderId == -1) {
                Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = findViewById<RadioButton>(genderId).text.toString().lowercase()

            val request = RegisterChildRequest(
                nikAnak = nik,
                name = nama,
                gender = gender.lowercase(), // "laki-laki" / "perempuan"
                birthDate = tanggalLahir      // yyyy-MM-dd
            )

            // 🚀 CALL API
            ApiClient.apiService.registerChild(request)
                .enqueue(object : Callback<RegisterChildResponse> {

                    override fun onResponse(
                        call: Call<RegisterChildResponse>,
                        response: Response<RegisterChildResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {

                            Log.d("REGISTER_API", "Register sukses: ${response.body()}")

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registrasi berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(this@RegisterActivity, LoginActivity::class.java)
                            )
                            finish()

                        } else {
                            Log.e(
                                "REGISTER_API",
                                "Register gagal | code=${response.code()} | body=${response.errorBody()?.string()}"
                            )

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registrasi gagal",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterChildResponse>, t: Throwable) {
                        Log.e("REGISTER_API", "Register error", t)

                        Toast.makeText(
                            this@RegisterActivity,
                            "Tidak dapat terhubung ke server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
