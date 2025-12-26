# 🚀 Quick Start Guide - MySQL Integration

## Untuk Backend Team (5 Menit Setup)

### 1. Setup MySQL

```bash
# Jika pakai XAMPP: Start MySQL di XAMPP Control Panel
# Jika pakai MySQL Server: Pastikan MySQL service running
```

### 2. Buat Database

```sql
-- Via phpMyAdmin atau MySQL command line:
CREATE DATABASE balita_sehat;
```

### 3. Import Tabel

```bash
# Via MySQL command:
mysql -u root -p balita_sehat < init_database.sql

# Atau via phpMyAdmin: Import file init_database.sql
```

### 4. Update Config

Edit `db_config.py`:

```python
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',  # ← Isi password MySQL kamu (kosong jika XAMPP)
    'database': 'balita_sehat',
    'port': 3306
}
```

### 5. Test & Run

```bash
# Test koneksi
python database.py

# Jalankan API server
python api_server_mysql.py

# Test API (di terminal baru)
python test_api_mysql.py
```

✅ **Done!** API server running di `http://localhost:5000`

---

## Untuk Mobile Team (Langsung Pakai)

### 1. Setup Retrofit di Android

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```

### 2. Buat Interface API

```kotlin
interface BalitaSehatAPI {
    @POST("/api/child/measurement")
    suspend fun addMeasurement(@Body data: MeasurementRequest): Response<ApiResponse>

    @GET("/api/child/{childId}/history")
    suspend fun getHistory(@Path("childId") childId: String): Response<HistoryResponse>
}
```

### 3. Call API

```kotlin
// URL dari backend team (tanya IP server mereka)
val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.0.100:5000")  // ← Ganti dengan IP server backend
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(BalitaSehatAPI::class.java)

// Tambah measurement
val measurement = MeasurementRequest(
    child_id = "CHILD001",
    name = "Budi Santoso",
    gender = "laki-laki",
    birth_date = "2024-01-15",
    age_months = 11,
    height_cm = 73.5,
    weight_kg = 9.2
)

val response = api.addMeasurement(measurement)
```

✅ **Done!** Mobile app sudah terhubung ke backend!

---

## Testing Endpoints

### Via Postman/Thunder Client:

**1. Health Check**

```
GET http://localhost:5000/api/health
```

**2. Tambah Measurement**

```
POST http://localhost:5000/api/child/measurement
Content-Type: application/json

{
  "child_id": "CHILD001",
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 11,
  "height_cm": 73.5,
  "weight_kg": 9.2
}
```

**3. Lihat History**

```
GET http://localhost:5000/api/child/CHILD001/history
```

**4. Lihat Semua Anak**

```
GET http://localhost:5000/api/children
```

---

## Troubleshooting

### ❌ "Access denied for user"

→ Cek username/password di `db_config.py`

### ❌ "Unknown database"

→ Database belum dibuat, jalankan `CREATE DATABASE balita_sehat;`

### ❌ "Table doesn't exist"

→ Tabel belum dibuat, import `init_database.sql`

### ❌ "Connection refused"

→ MySQL server belum running (start di XAMPP atau service MySQL)

### ❌ Mobile tidak bisa connect

→ Pastikan:

- API server running
- Gunakan IP lokal (192.168.x.x), bukan localhost
- Mobile dan laptop di WiFi yang sama
- Firewall allow port 5000

---

## 📚 Dokumentasi Lengkap

- **Setup MySQL:** `SETUP_MYSQL.md`
- **Integrasi Mobile:** `INTEGRASI_MOBILE.md`
- **Summary:** `MYSQL_INTEGRATION_COMPLETE.md`

---

**Questions?** Baca troubleshooting di `SETUP_MYSQL.md`!
