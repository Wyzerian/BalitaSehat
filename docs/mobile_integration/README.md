# Mobile Integration Guide - BalitaSehat

Panduan lengkap untuk consume API BalitaSehat dari aplikasi Android/Kotlin.

## 📋 File Structure

```
app/src/main/java/com/example/balitasehat/
├── api/
│   ├── ApiService.kt          # Interface API endpoints
│   ├── DataModels.kt          # Data classes untuk request/response
│   └── RetrofitClient.kt      # HTTP client setup
└── ui/
    └── RegisterActivity.kt    # Contoh implementasi register
```

## 🚀 Setup

### 1. Tambahkan Dependencies

Edit `build.gradle.kts` (app level):

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Glide (untuk load chart)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

### 2. Tambahkan Internet Permission

Edit `AndroidManifest.xml`:

```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:usesCleartextTraffic="true" <!-- Untuk HTTP non-SSL -->
        ...>
        ...
    </application>
</manifest>
```

### 3. Copy Files

Copy semua file dari folder ini ke project Android kamu:

- `ApiService.kt` → `app/src/main/java/com/example/balitasehat/api/`
- `DataModels.kt` → `app/src/main/java/com/example/balitasehat/api/`
- `RetrofitClient.kt` → `app/src/main/java/com/example/balitasehat/api/`
- `RegisterActivity.kt` → `app/src/main/java/com/example/balitasehat/ui/`

### 4. Konfigurasi Base URL

Edit `RetrofitClient.kt` line 16-17:

```kotlin
// Development (testing dengan HP di jaringan sama)
private const val BASE_URL_DEV = "http://10.10.150.20:5000/"

// Production (VPS)
private const val BASE_URL_PROD = "http://your-vps-ip:5000/"
```

**Cara dapat IP lokal:**

- Windows: `ipconfig` → cari IPv4 Address
- Pastikan HP dan laptop di WiFi yang sama
- Pastikan Flask app running dengan `host='0.0.0.0'`

## 📱 Flow Register

### Step 1: User Input NIK

```
[EditText NIK: 16 digit]
[Button: Cek NIK]
```

### Step 2: Check NIK via API

```kotlin
RetrofitClient.apiService.checkNik(nik).enqueue(...)
```

**Response jika sudah terdaftar:**

```json
{
  "status": "found",
  "data": {
    "child_id": "BUDI001",
    "name": "Budi Santoso",
    ...
  }
}
```

→ Tampilkan pesan: "NIK sudah terdaftar atas nama Budi Santoso"

**Response jika belum terdaftar:**

```json
{
  "status": "not_found",
  "message": "NIK belum terdaftar"
}
```

→ Enable form registrasi

### Step 3: User Isi Form Register

```
[EditText Nama]
[RadioGroup Gender: Laki-laki / Perempuan]
[DatePicker Tanggal Lahir]
[Button: Daftar]
```

### Step 4: Submit Register

```kotlin
val request = RegisterChildRequest(
    nikAnak = "3301234567890123",
    name = "Budi Santoso",
    gender = "laki-laki",
    birthDate = "2024-07-24"
)

RetrofitClient.apiService.registerChild(request).enqueue(...)
```

**Response sukses:**

```json
{
  "status": "success",
  "message": "Anak berhasil didaftarkan",
  "data": {
    "child_id": "BUDI001",
    "nik_anak": "3301234567890123",
    ...
  }
}
```

## 🧪 Testing

### Test dengan Emulator

```
BASE_URL = "http://10.0.2.2:5000/"  // 10.0.2.2 = localhost dari emulator
```

### Test dengan HP (WiFi sama)

```
BASE_URL = "http://10.10.150.20:5000/"  // IP laptop kamu
```

### Test Koneksi

Buka browser di HP:

```
http://10.10.150.20:5000/api/health
```

Harus muncul:

```json
{
  "status": "healthy",
  "message": "BalitaSehat API is running",
  ...
}
```

## 🔍 Debugging

### Enable Logging

`RetrofitClient.kt` sudah include logging interceptor. Cek Logcat:

```
D/OkHttp: --> POST http://10.10.150.20:5000/api/child/register
D/OkHttp: Content-Type: application/json
D/OkHttp: {"nik_anak":"3301234567890123","name":"Budi",...}
D/OkHttp: --> END POST
D/OkHttp: <-- 200 OK http://10.10.150.20:5000/api/child/register
D/OkHttp: {"status":"success","message":"Anak berhasil didaftarkan",...}
```

### Common Errors

**1. Connection Failed**

```
Koneksi gagal: Failed to connect to /10.10.150.20:5000
```

→ Pastikan Flask running dengan `host='0.0.0.0'`
→ Pastikan HP & laptop di WiFi sama

**2. HTTP 400 Bad Request**

```
Error: 400
```

→ Cek format request JSON
→ Lihat response body untuk detail error

**3. Cleartext Traffic**

```
Cleartext HTTP traffic not permitted
```

→ Tambahkan `android:usesCleartextTraffic="true"` di AndroidManifest.xml

## 📚 Next Steps

Setelah register selesai, implementasi fitur lain:

1. **Input Measurement** → Lihat file `MeasurementActivity.kt` (coming soon)
2. **History & Chart** → Lihat file `HistoryActivity.kt` (coming soon)
3. **Analysis** → Lihat file `AnalysisActivity.kt` (coming soon)

## 🎯 Best Practices

1. **Error Handling**: Selalu handle `onFailure()` dan HTTP error codes
2. **Loading State**: Tampilkan loading indicator saat API call
3. **Validation**: Validasi input sebelum kirim ke server
4. **User Feedback**: Tampilkan Toast/Dialog untuk inform user
5. **Networking**: Gunakan coroutines atau RxJava untuk async operations (advanced)

## 📞 Support

Butuh bantuan? Cek:

- Backend API Documentation: `/docs/API_POSYANDU_WORKFLOW.md`
- Test endpoint via Postman/Thunder Client dulu sebelum implement di mobile
