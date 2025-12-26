# Panduan Setup MySQL Database untuk BalitaSehat API

## 📋 Ringkasan

API BalitaSehat sekarang menggunakan **MySQL database** untuk menyimpan data anak dan hasil pengukuran. Mobile app (Kotlin/Android) akan mengakses data melalui REST API (format JSON), **tidak langsung ke MySQL**.

### Arsitektur:

```
Mobile App (Kotlin/Android)
    ↓ HTTP Request (JSON)
REST API (Python Flask)
    ↓ SQL Query
MySQL Database
```

---

## 🚀 Langkah Setup

### 1. Install MySQL

**Pilihan A: XAMPP (Recommended untuk pemula)**

- Download XAMPP: https://www.apachefriends.org/
- Install XAMPP
- Buka XAMPP Control Panel
- Start Apache dan MySQL

**Pilihan B: MySQL Server Standalone**

- Download MySQL: https://dev.mysql.com/downloads/mysql/
- Install dan jalankan MySQL Server

---

### 2. Buat Database

**Via phpMyAdmin (jika pakai XAMPP):**

1. Buka browser: http://localhost/phpmyadmin
2. Klik "New" di sidebar kiri
3. Nama database: `balita_sehat`
4. Klik "Create"

**Via MySQL Command Line:**

```bash
mysql -u root -p
```

```sql
CREATE DATABASE balita_sehat;
```

---

### 3. Jalankan Script SQL untuk Buat Tabel

**Via phpMyAdmin:**

1. Klik database `balita_sehat`
2. Klik tab "SQL"
3. Copy semua isi file `init_database.sql`
4. Paste ke SQL editor
5. Klik "Go"

**Via MySQL Command Line:**

```bash
mysql -u root -p balita_sehat < init_database.sql
```

**Atau via Python:**

```bash
python -c "from database import DatabaseConnection; DatabaseConnection.test_connection()"
```

Tabel yang akan dibuat:

- ✅ `children` - Data anak (ID, nama, gender, tanggal lahir)
- ✅ `measurements` - Data pengukuran (tinggi, berat, umur)
- ✅ `classifications` - Hasil klasifikasi WHO (Z-score, status, risk level)
- ✅ `trend_analysis` - Analisis trend pertumbuhan

---

### 4. Konfigurasi Koneksi Database

Edit file `db_config.py`:

```python
DB_CONFIG = {
    'host': 'localhost',        # Ganti jika MySQL di server lain
    'user': 'root',             # Username MySQL kamu
    'password': '',             # Password MySQL kamu (kosong jika default XAMPP)
    'database': 'balita_sehat', # Nama database
    'port': 3306,
    'charset': 'utf8mb4',
    'autocommit': True
}
```

**Contoh jika pakai password:**

```python
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'admin123',  # <-- Password MySQL kamu
    'database': 'balita_sehat',
    'port': 3306
}
```

**Contoh jika database di server cloud:**

```python
DB_CONFIG = {
    'host': 'db.example.com',      # IP/domain server
    'user': 'balitasehat_user',
    'password': 'strongpassword',
    'database': 'balita_sehat',
    'port': 3306
}
```

---

### 5. Test Koneksi Database

```bash
python database.py
```

Output yang diharapkan:

```
Testing database connection...
✓ Berhasil terkoneksi ke database: balita_sehat
```

Jika error:

- ❌ `Access denied` → Cek username/password di `db_config.py`
- ❌ `Unknown database` → Database belum dibuat, lihat langkah 2
- ❌ `Connection refused` → MySQL server belum jalan

---

### 6. Jalankan API Server dengan MySQL

```bash
python api_server_mysql.py
```

Output:

```
Testing database connection...
✓ Berhasil terkoneksi ke database: balita_sehat
✓ Database connected successfully!
Initializing WHO Classifier...
✓ Data WHO berhasil dimuat
✓ Server ready!
 * Running on http://127.0.0.1:5000
 * Running on http://192.168.0.100:5000
```

---

## 🔧 File-File Penting

| File                      | Fungsi                                                         |
| ------------------------- | -------------------------------------------------------------- |
| `db_config.py`            | Konfigurasi koneksi MySQL (host, user, password, dll)          |
| `database.py`             | Class untuk manage koneksi & query ke MySQL                    |
| `init_database.sql`       | Script SQL untuk buat tabel-tabel                              |
| `growth_tracker_mysql.py` | Tracker dengan MySQL integration (pengganti growth_tracker.py) |
| `api_server_mysql.py`     | API server dengan MySQL (pengganti api_server.py)              |

---

## 📊 Struktur Database

### Tabel `children`

```sql
id           VARCHAR(50)  PRIMARY KEY
name         VARCHAR(100)
gender       ENUM('laki-laki', 'perempuan')
birth_date   DATE
created_at   TIMESTAMP
updated_at   TIMESTAMP
```

### Tabel `measurements`

```sql
id                INT           PRIMARY KEY AUTO_INCREMENT
child_id          VARCHAR(50)   FOREIGN KEY → children.id
measurement_date  DATE
age_months        INT
height_cm         DECIMAL(5,2)
weight_kg         DECIMAL(5,2)
created_at        TIMESTAMP
```

### Tabel `classifications`

```sql
id                  INT           PRIMARY KEY AUTO_INCREMENT
measurement_id      INT           FOREIGN KEY → measurements.id
child_id            VARCHAR(50)   FOREIGN KEY → children.id
height_zscore       DECIMAL(5,2)
weight_zscore       DECIMAL(5,2)
stunting_status     VARCHAR(50)
wasting_status      VARCHAR(50)
risk_level          ENUM('NONE','LOW','MEDIUM','HIGH')
warnings            TEXT
recommendations     TEXT
created_at          TIMESTAMP
```

---

## 🧪 Testing API dengan MySQL

### 1. Test via Postman/Thunder Client

**Tambah Measurement Baru:**

```http
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

**Lihat History Anak:**

```http
GET http://localhost:5000/api/child/CHILD001/history
```

**Lihat Semua Anak:**

```http
GET http://localhost:5000/api/children
```

### 2. Cek Data di Database

Via phpMyAdmin atau MySQL command line:

```sql
-- Lihat semua anak
SELECT * FROM children;

-- Lihat semua measurements
SELECT * FROM measurements;

-- Lihat hasil klasifikasi
SELECT * FROM classifications;

-- Laporan lengkap (gabungan)
SELECT * FROM vw_child_report;
```

---

## 📱 Integrasi dengan Mobile (Kotlin/Android)

Mobile app **TIDAK PERLU** koneksi langsung ke MySQL!

Mobile hanya perlu:

1. ✅ HTTP client (Retrofit/Volley/OkHttp)
2. ✅ Akses ke API endpoint (http://your-server:5000)
3. ✅ Parse JSON response

**Contoh Retrofit (Kotlin):**

```kotlin
interface BalitaSehatAPI {
    @POST("/api/child/measurement")
    suspend fun addMeasurement(@Body data: MeasurementRequest): Response<ApiResponse>

    @GET("/api/child/{childId}/history")
    suspend fun getHistory(@Path("childId") childId: String): Response<HistoryResponse>
}

// Data class
data class MeasurementRequest(
    val child_id: String,
    val name: String,
    val gender: String,
    val birth_date: String,
    val age_months: Int,
    val height_cm: Double,
    val weight_kg: Double
)
```

Lihat file `INTEGRASI_MOBILE.md` untuk contoh lengkap.

---

## 🌐 Deploy ke Cloud (Opsional)

### Option 1: Railway

1. Daftar di railway.app
2. Buat MySQL database
3. Deploy Python app
4. Set environment variables untuk DB_CONFIG

### Option 2: Heroku + ClearDB

1. Daftar di heroku.com
2. Install ClearDB MySQL addon
3. Deploy Python Flask app
4. Update db_config.py dengan kredensial ClearDB

### Option 3: VPS (DigitalOcean/AWS)

1. Setup VPS dengan Ubuntu
2. Install MySQL Server
3. Install Python dependencies
4. Run Flask app dengan Gunicorn
5. Setup Nginx sebagai reverse proxy

---

## ❓ Troubleshooting

### Error: "Access denied for user"

**Solusi:** Cek username dan password di `db_config.py`

### Error: "Unknown database 'balita_sehat'"

**Solusi:** Database belum dibuat, jalankan `CREATE DATABASE balita_sehat;`

### Error: "Table doesn't exist"

**Solusi:** Tabel belum dibuat, jalankan script `init_database.sql`

### Error: "No module named 'mysql.connector'"

**Solusi:** Install package: `pip install mysql-connector-python`

### Mobile app tidak bisa connect ke API

**Solusi:**

- Pastikan API server running
- Gunakan IP lokal (192.168.x.x), bukan localhost
- Cek firewall Windows (allow port 5000)
- Pastikan mobile dan laptop di WiFi yang sama

### Data tidak tersimpan ke database

**Solusi:**

- Cek autocommit di db_config.py
- Pastikan foreign key constraints tidak error
- Cek error log di terminal API server

---

## 📚 Next Steps

1. ✅ Setup database MySQL
2. ✅ Konfigurasi db_config.py
3. ✅ Test koneksi database
4. ✅ Jalankan API server
5. ✅ Test endpoints dengan Postman
6. ✅ Koordinasi dengan mobile team untuk integrasi
7. ✅ Deploy ke cloud (optional untuk demo)

---

## 🎯 Checklist Integrasi

- [ ] MySQL sudah running (XAMPP/MySQL Server)
- [ ] Database `balita_sehat` sudah dibuat
- [ ] Semua tabel sudah dibuat (jalankan init_database.sql)
- [ ] File db_config.py sudah dikonfigurasi dengan benar
- [ ] Test koneksi database berhasil
- [ ] API server bisa jalan tanpa error
- [ ] Test POST /api/child/measurement berhasil
- [ ] Cek data masuk ke tabel MySQL
- [ ] Mobile team sudah dapat URL API
- [ ] Mobile team sudah test integrasi

---

**Tim yang sudah buat database:**
Konfirmasi sama tim kamu:

- Host MySQL: **\_\_\_\_** (localhost atau IP server)
- Username: **\_\_\_\_**
- Password: **\_\_\_\_**
- Nama Database: **\_\_\_\_** (ganti di db_config.py jika beda)
- Port: **\_\_\_\_** (default 3306)

Update info di atas ke `db_config.py` dan kamu siap! 🚀
