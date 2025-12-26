/**
 * CONTOH IMPLEMENTASI: Register Activity
 * Flow: Input NIK → Check NIK → Register jika belum terdaftar
 */

package com.example.balitasehat.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.balitasehat.R
import com.example.balitasehat.api.*
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var etNik: EditText
    private lateinit var etName: EditText
    private lateinit var etParentName: EditText
    private lateinit var etAddress: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var etBirthDate: EditText
    private lateinit var btnCheckNik: Button
    private lateinit var btnRegister: Button
    
    private var selectedBirthDate: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialize views
        etNik = findViewById(R.id.et_nik)
        etName = findViewById(R.id.et_name)
        etParentName = findViewById(R.id.et_parent_name)
        etAddress = findViewById(R.id.et_address)
        rgGender = findViewById(R.id.rg_gender)
        rbMale = findViewById(R.id.rb_male)
        rbFemale = findViewById(R.id.rb_female)
        etBirthDate = findViewById(R.id.et_birth_date)
        btnCheckNik = findViewById(R.id.btn_check_nik)
        btnRegister = findViewById(R.id.btn_register)
        
        // Setup listeners
        btnCheckNik.setOnClickListener { checkNik() }
        btnRegister.setOnClickListener { registerChild() }
        etBirthDate.setOnClickListener { showDatePicker() }
        
        // Disable register button initially
        btnRegister.isEnabled = false
    }
    
    // ============================================================================
    // STEP 1: Check NIK
    // ============================================================================
    private fun checkNik() {
        val nik = etNik.text.toString().trim()
        
        // Validasi NIK (harus 16 digit)
        if (nik.length != 16 || !nik.all { it.isDigit() }) {
            Toast.makeText(this, "NIK harus 16 digit angka", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        btnCheckNik.isEnabled = false
        btnCheckNik.text = "Mengecek..."
        
        // API Call: Check NIK
        RetrofitClient.apiService.checkNik(nik).enqueue(object : Callback<CheckNikResponse> {
            override fun onResponse(
                call: Call<CheckNikResponse>,
                response: Response<CheckNikResponse>
            ) {
                btnCheckNik.isEnabled = true
                btnCheckNik.text = "Cek NIK"
                
                if (response.isSuccessful) {
                    val result = response.body()
                    
                    when (result?.status) {
                        "found" -> {
                            // NIK sudah terdaftar
                            val childData = result.data!!
                            Toast.makeText(
                                this@RegisterActivity,
                                "NIK sudah terdaftar atas nama: ${childData.name}",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Bisa redirect ke halaman measurement langsung
                            // atau tampilkan data anak
                        }
                        
                        "not_found" -> {
                            // NIK belum terdaftar, enable form register
                            Toast.makeText(
                                this@RegisterActivity,
                                "NIK belum terdaftar. Silakan isi form registrasi.",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            btnRegister.isEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            override fun onFailure(call: Call<CheckNikResponse>, t: Throwable) {
                btnCheckNik.isEnabled = true
                btnCheckNik.text = "Cek NIK"
                
                Toast.makeText(
                    this@RegisterActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    // ============================================================================
    // STEP 2: Register Child
    // ============================================================================
    private fun registerChild() {
        val parentName = etParentName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rb_male -> "laki-laki"
            R.id.rb_female -> "perempuan"
            else -> ""
        }
        val birthDate = selectedBirthDate
        
        // Validasi form
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (parentName.isEmpty()) {
            Toast.makeText(this, "Nama orang tua harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (address.isEmpty()) {
            Toast.makeText(this, "Alamat harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (gender.isEmpty()) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (birthDate.isEmpty()) {
            Toast.makeText(this, "Pilih tanggal lahir", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        btnRegister.isEnabled = false
        btnRegister.text = "Mendaftar..."
        
        // Prepare request
        val request = RegisterChildRequest(
            nikAnak = nik,
            name = name,
            parentName = parentName,
            address = addressegisterChildRequest(
            nikAnak = nik,
            name = name,
            gender = gender,
            birthDate = birthDate
        )
        
        // API Call: Register
        RetrofitClient.apiService.registerChild(request).enqueue(object : Callback<RegisterChildResponse> {
            override fun onResponse(
                call: Call<RegisterChildResponse>,
                response: Response<RegisterChildResponse>
            ) {
                btnRegister.isEnabled = true
                btnRegister.text = "Daftar"
                
                if (response.isSuccessful) {
                    val result = response.body()
                    
                    if (result?.status == "success") {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registrasi berhasil! Child ID: ${result.data?.childId}",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Redirect ke halaman measurement atau home
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            result?.message ?: "Registrasi gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            override fun onFailure(call: Call<RegisterChildResponse>, t: Throwable) {
                btnRegister.isEnabled = true
                btnRegister.text = "Daftar"
                
                Toast.makeText(
                    this@RegisterActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    // ============================================================================
    // DATE PICKER
    // ============================================================================
    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal Lahir")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            
            // Format: YYYY-MM-DD
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            selectedBirthDate = sdf.format(calendar.time)
            
            // Format untuk display: DD/MM/YYYY
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            etBirthDate.setText(displayFormat.format(calendar.time))
        }
        
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }
}
