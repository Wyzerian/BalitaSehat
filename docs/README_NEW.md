# BalitaSehat - WHO Growth Monitoring System with MySQL

Sistem monitoring pertumbuhan anak berbasis standar WHO dengan backend API dan MySQL database untuk aplikasi mobile Posyandu.

## 🎯 Overview

Aplikasi ini menggunakan **data mining/analytics approach** (bukan machine learning supervised):

- ✅ Rule-based system menggunakan standar WHO resmi
- ✅ Z-score calculation dengan LMS method
- ✅ Early warning system (threshold -1 SD sesuai praktek Posyandu)
- ✅ REST API untuk integrasi dengan mobile app
- ✅ MySQL database untuk data persistence

**Arsitektur:**

```
Mobile App (Kotlin/Android)
    ↓ HTTP Request (JSON)
REST API (Python Flask)
    ↓ SQL Query
MySQL Database
```

---

## 📁 Struktur Project

```
BalitaSehat/
├── data/                                    # WHO Standards & Dataset
│   ├── WHO Indicators Boys 2 years_Tinggi.csv
│   ├── WHO Indicators Girls 2 years_Tinggi.csv
│   ├── WHO Indicators Boys 2 years_Berat.csv
│   ├── WHO Indicators Girls 2 years_Berat.csv
│   └── stunting_wasting_dataset.csv        # Kaggle (validasi)
│
├── who_classifier.py                        # ✅ WHO Classification Algorithm
├── growth_tracker_mysql.py                  # ✅ Growth Tracker (MySQL version)
├── api_server_mysql.py                      # ✅ REST API Server (MySQL)
├── database.py                              # ✅ Database Connection Manager
├── db_config.py                             # ⚙️ MySQL Configuration
├── init_database.sql                        # ✅ Database Schema (SQL Script)
│
├── validate_with_kaggle.py                  # Validasi akurasi (82%/70%)
├── visualize_growth.py                      # Generate grafik pertumbuhan
├── test_api_mysql.py                        # Testing script
│
└── Documentation/
    ├── QUICKSTART_MYSQL.md                  # 🚀 Quick start guide (5 menit)
    ├── SETUP_MYSQL.md                       # 📖 Panduan lengkap setup MySQL
    ├── INTEGRASI_MOBILE.md                  # 📱 Cara integrase mobile app
    ├── MYSQL_INTEGRATION_COMPLETE.md        # ✅ Summary integrasi MySQL
    └── RINGKASAN_MYSQL.md                   # 📝 Ringkasan untuk tim
```

---

## 🚀 Quick Start

### Backend Team (5 Menit Setup)

```bash
# 1. Setup MySQL (via XAMPP atau MySQL Server)
# 2. Buat database
mysql -u root -p
CREATE DATABASE balita_sehat;

# 3. Import tabel
mysql -u root -p balita_sehat < init_database.sql

# 4. Update konfigurasi (edit db_config.py)
# Isi username, password, database name

# 5. Install dependencies
pip install -r requirements.txt

# 6. Test koneksi
python database.py

# 7. Jalankan API server
python api_server_mysql.py

# 8. Test API (terminal baru)
python test_api_mysql.py
```

✅ API server running di `http://localhost:5000`

**Lihat:** [`QUICKSTART_MYSQL.md`](QUICKSTART_MYSQL.md) untuk panduan detail.

---

## 📱 Mobile Integration

Mobile app **TIDAK PERLU** koneksi langsung ke MySQL!

Mobile hanya perlu:

1. HTTP client (Retrofit/Volley/OkHttp)
2. URL API server (dari backend team)
3. Parse JSON response

**Contoh Kotlin/Retrofit:**

```kotlin
interface BalitaSehatAPI {
    @POST("/api/child/measurement")
    suspend fun addMeasurement(@Body data: MeasurementRequest): Response<ApiResponse>

    @GET("/api/child/{childId}/history")
    suspend fun getHistory(@Path("childId") childId: String): Response<HistoryResponse>
}
```

**Lihat:** [`INTEGRASI_MOBILE.md`](INTEGRASI_MOBILE.md) untuk contoh lengkap.

---

## 🔌 API Endpoints

| Endpoint                   | Method | Deskripsi                      |
| -------------------------- | ------ | ------------------------------ |
| `/api/health`              | GET    | Cek status server & database   |
| `/api/classify`            | POST   | Klasifikasi tanpa simpan ke DB |
| `/api/child/measurement`   | POST   | Tambah measurement + simpan    |
| `/api/child/{id}/history`  | GET    | Lihat riwayat pengukuran       |
| `/api/child/{id}/analysis` | GET    | Analisis trend pertumbuhan     |
| `/api/children`            | GET    | Lihat semua anak terdaftar     |
| `/api/who/thresholds`      | GET    | Ambil WHO thresholds           |
| `/api/child/{id}`          | DELETE | Hapus data anak (testing)      |

**Contoh Request:**

```bash
# Tambah measurement
POST http://localhost:5000/api/child/measurement
{
  "child_id": "CHILD001",
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 11,
  "height_cm": 73.5,
  "weight_kg": 9.2
}

# Lihat history
GET http://localhost:5000/api/child/CHILD001/history
```

---

## 🗄️ Database Schema

### Tabel: `children`

Data anak (ID, nama, gender, tanggal lahir)

### Tabel: `measurements`

Data pengukuran (tinggi, berat, umur, tanggal)

### Tabel: `classifications`

Hasil klasifikasi WHO (Z-score, status, risk level, warnings, recommendations)

### Tabel: `trend_analysis`

Analisis trend pertumbuhan (opsional)

**Lihat:** [`init_database.sql`](init_database.sql) untuk schema lengkap.

---

## 🧪 WHO Classification Algorithm

### Formula Z-score (LMS Method):

```
Z-score = [(value/M)^L - 1] / (L × S)
```

**Dimana:**

- L = Power in Box-Cox transformation
- M = Median
- S = Coefficient of variation

### Classification Thresholds:

**Stunting (Height-for-Age):**

- Z < -3: Severely Stunted
- -3 ≤ Z < -2: Stunted
- -2 ≤ Z < -1: At Risk (Early Warning) ⚠️
- -1 ≤ Z ≤ 2: Normal
- Z > 2: Tall

**Wasting (Weight-for-Age):**

- Z < -3: Severely Underweight
- -3 ≤ Z < -2: Underweight
- -2 ≤ Z < -1: At Risk (Early Warning) ⚠️
- -1 ≤ Z ≤ 1: Normal weight
- 1 < Z ≤ 2: Risk of Overweight
- Z > 2: Overweight

### Early Warning System:

- **HIGH Risk:** Z < -3 (Emergency)
- **MEDIUM Risk:** -3 ≤ Z < -1 (Detected)
- **LOW Risk:** Approaching threshold
- **NONE:** Normal range

**Note:** Threshold -1 SD untuk early warning sesuai praktek Posyandu.

---

## 📊 Validasi & Akurasi

Validasi dengan Kaggle dataset (100k records):

- ✅ Stunting: **82.4%** accuracy
- ✅ Wasting: **70.7%** accuracy

Perbedaan akurasi karena:

- Kaggle dataset tidak distinguish "Tall" vs "Normal"
- WHO algorithm lebih precise dengan kategori detail
- **Algoritma WHO lebih akurat** untuk medical purpose

**File:** [`validate_with_kaggle.py`](validate_with_kaggle.py)

---

## 📖 Dokumentasi Lengkap

| File                                                             | Deskripsi                                   |
| ---------------------------------------------------------------- | ------------------------------------------- |
| [`QUICKSTART_MYSQL.md`](QUICKSTART_MYSQL.md)                     | 🚀 Quick start guide (5 menit setup)        |
| [`SETUP_MYSQL.md`](SETUP_MYSQL.md)                               | 📖 Panduan lengkap setup MySQL step-by-step |
| [`INTEGRASI_MOBILE.md`](INTEGRASI_MOBILE.md)                     | 📱 Cara mobile app consume REST API         |
| [`MYSQL_INTEGRATION_COMPLETE.md`](MYSQL_INTEGRATION_COMPLETE.md) | ✅ Summary integrasi MySQL                  |
| [`RINGKASAN_MYSQL.md`](RINGKASAN_MYSQL.md)                       | 📝 Ringkasan untuk koordinasi tim           |
| [`PENJELASAN_THRESHOLD.md`](PENJELASAN_THRESHOLD.md)             | 📊 Penjelasan threshold WHO                 |

---

## 🔧 Dependencies

```bash
pip install -r requirements.txt
```

**Main packages:**

- Flask 3.1.2 - Web framework
- Flask-CORS 6.0.2 - CORS support
- pandas 2.3.3 - Data processing
- numpy 2.4.0 - Numerical computation
- mysql-connector-python 9.5.0 - MySQL database connector
- matplotlib 3.10.8 - Visualization (optional)

---

## ❓ FAQ

**Q: Kenapa mobile tidak bisa langsung ke MySQL?**  
A: Security & standard practice. Mobile → API → Database lebih aman dan scalable.

**Q: Apakah ini machine learning?**  
A: Bukan. Ini data mining/analytics dengan rule-based system menggunakan standar WHO resmi.

**Q: Dataset Kaggle untuk apa?**  
A: Untuk validasi akurasi algoritma saja. Operational data disimpan di MySQL.

**Q: File WHO CSV masih dipakai?**  
A: Ya, untuk calculate Z-score. WHO CSV tetap di backend, tidak perlu di mobile.

**Q: Bisa pakai SQLite?**  
A: Bisa, tapi tim sudah buat MySQL jadi pakai MySQL untuk consistency.

---

## 👥 Tim Development

**Backend/ML Team:**

- Setup MySQL database
- Konfigurasi API server
- Testing & validation

**Mobile Team:**

- Implement Retrofit/HTTP client
- Consume REST API endpoints
- UI untuk input & display results

---

## 📞 Support & Troubleshooting

Lihat bagian Troubleshooting di [`SETUP_MYSQL.md`](SETUP_MYSQL.md) untuk solusi error umum:

- Access denied
- Connection refused
- Table doesn't exist
- Mobile tidak bisa connect

---

## 📄 License

Educational project untuk hackathon kampus.

---

## 🎉 Status

✅ **COMPLETE - Ready for Integration!**

- [x] WHO Classification Algorithm
- [x] MySQL Database Integration
- [x] REST API (8 endpoints)
- [x] Growth Tracking & Trend Analysis
- [x] Early Warning System
- [x] Complete Documentation
- [x] Testing Suite
- [ ] Mobile App Implementation (Mobile Team)
- [ ] Cloud Deployment (Optional)

---

**Last Updated:** 23 Desember 2025  
**Version:** 2.0 (MySQL Integration)

**Quick Links:**

- 🚀 [Quick Start](QUICKSTART_MYSQL.md)
- 📖 [Setup MySQL](SETUP_MYSQL.md)
- 📱 [Mobile Integration](INTEGRASI_MOBILE.md)
