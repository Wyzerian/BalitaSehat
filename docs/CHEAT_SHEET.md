# 📋 Cheat Sheet - BalitaSehat Quick Reference

## 🎯 Ringkasan Ultra Singkat

### Apa yang Tersimpan di Database?

```
✅ Data anak → MySQL table: children
✅ Pengukuran → MySQL table: measurements
✅ Hasil klasifikasi → MySQL table: classifications
✅ Trend analysis → MySQL table: trend_analysis

❌ Grafik → TIDAK di database (generate saat dibutuhkan)
```

### Cara Kerja Sistem

```
1. Mobile input data → POST ke API
2. API classify pakai WHO → simpan ke MySQL
3. API return hasil dalam JSON
4. Mobile tampilkan hasil
5. Bulan depan: input lagi → dapat trend analysis
6. Lihat grafik: generate dari data di MySQL
```

---

## 📝 Flow Penggunaan Sehari-hari

### Anak Pertama Kali Datang

```
Mobile: Input data anak + pengukuran → Send
API: Classify → Save to DB → Return hasil
Mobile: Tampilkan status + warnings + recommendations
```

### Anak Datang Bulan Berikutnya

```
Mobile: Search anak → Input pengukuran baru → Send
API: Classify → Compare dengan bulan lalu → Trend analysis → Save → Return
Mobile: Tampilkan hasil + trend (naik/turun) + prediksi bulan depan
```

### Lihat Riwayat

```
Mobile: Request history
API: Query database → Return semua pengukuran
Mobile: Tampilkan table/list
```

### Lihat Grafik

```
Option A (Backend generate):
  Mobile: Request chart → API: Generate PNG → Return URL
  Mobile: Download & display image

Option B (Mobile generate - Recommended):
  Mobile: Request data → API: Return JSON
  Mobile: Generate chart pakai MPAndroidChart
```

---

## 🗄️ Database Schema (Simplified)

```sql
-- Tabel 1: Data anak
children (id, name, gender, birth_date)

-- Tabel 2: Pengukuran
measurements (id, child_id, date, age, height, weight)

-- Tabel 3: Hasil klasifikasi
classifications (id, measurement_id, z_height, z_weight,
                 stunting_status, wasting_status, risk_level,
                 warnings, recommendations)

-- Tabel 4: Analisis trend
trend_analysis (id, child_id, date, height_trend, weight_trend,
                height_change_per_month, weight_change_per_month,
                predicted_height, predicted_weight, warnings)
```

---

## 🔌 API Endpoints (8 Total)

| Endpoint                   | Method | Fungsi            | Simpan DB? |
| -------------------------- | ------ | ----------------- | ---------- |
| `/api/health`              | GET    | Cek server status | ❌         |
| `/api/classify`            | POST   | Klasifikasi saja  | ❌         |
| `/api/child/measurement`   | POST   | Input + save      | ✅         |
| `/api/child/{id}/history`  | GET    | Lihat riwayat     | -          |
| `/api/child/{id}/analysis` | GET    | Trend analysis    | -          |
| `/api/children`            | GET    | Semua anak        | -          |
| `/api/who/thresholds`      | GET    | WHO thresholds    | -          |
| `/api/child/{id}`          | DELETE | Hapus anak        | ✅         |

---

## 💻 Code Examples

### Python (Backend)

```python
# Jalankan server
python api_server_mysql.py

# Test koneksi database
python database.py

# Test API
python test_api_mysql.py
```

### Kotlin (Mobile)

```kotlin
// Setup Retrofit
val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.0.100:5000")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Add measurement
val data = MeasurementRequest(
    child_id = "CHILD001",
    name = "Budi",
    gender = "laki-laki",
    birth_date = "2024-01-15",
    age_months = 11,
    height_cm = 73.5,
    weight_kg = 9.2
)
val response = api.addMeasurement(data)

// Get history
val history = api.getHistory("CHILD001")

// Generate chart (MPAndroidChart)
val entries = history.data.map {
    Entry(it.age_months.toFloat(), it.height_cm)
}
val dataSet = LineDataSet(entries, "Tinggi")
lineChart.data = LineData(dataSet)
```

### HTTP Request (Postman/cURL)

```bash
# Test health
curl http://localhost:5000/api/health

# Add measurement
curl -X POST http://localhost:5000/api/child/measurement \
  -H "Content-Type: application/json" \
  -d '{
    "child_id": "CHILD001",
    "name": "Budi Santoso",
    "gender": "laki-laki",
    "birth_date": "2024-01-15",
    "age_months": 11,
    "height_cm": 73.5,
    "weight_kg": 9.2
  }'

# Get history
curl http://localhost:5000/api/child/CHILD001/history
```

---

## 🎨 Grafik: 2 Pilihan

### Pilihan A: Backend Generate (matplotlib)

```
Pros: Simple untuk mobile, konsisten
Cons: Butuh storage, slow, tidak interaktif
Good for: Demo/hackathon
```

### Pilihan B: Mobile Generate (MPAndroidChart)

```
Pros: No storage, fast, interaktif, customizable
Cons: Mobile harus implement
Good for: Production
```

**Recommended: Pilihan B untuk production**

---

## 📊 Storage Estimation

```
1000 anak × 12 pengukuran masing-masing:
├─ MySQL Database: ~9 MB (data)
└─ Server Files:
   ├─ WHO CSV: 40 KB (permanent)
   └─ Chart PNG: 400 MB (optional, bisa dihapus)

Recommendation: Pakai mobile-generated charts → hemat 400 MB!
```

---

## ⚙️ Setup Checklist

### Backend:

- [ ] Install MySQL (XAMPP/MySQL Server)
- [ ] `CREATE DATABASE balita_sehat;`
- [ ] `mysql ... < init_database.sql`
- [ ] Edit `db_config.py` (host, user, password)
- [ ] `python database.py` → harus "✓ Berhasil"
- [ ] `python api_server_mysql.py` → running
- [ ] Catat IP server untuk mobile team

### Mobile:

- [ ] Dapat URL API dari backend
- [ ] Setup Retrofit
- [ ] Implement POST `/api/child/measurement`
- [ ] Implement GET `/api/child/{id}/history`
- [ ] Implement chart (MPAndroidChart)
- [ ] Test end-to-end

---

## ❓ FAQ Ultra Singkat

**Q: Grafik tersimpan di database?**  
A: TIDAK. Di-generate on-demand dari data.

**Q: Mobile bisa langsung ke MySQL?**  
A: TIDAK. Harus lewat API (security).

**Q: Dataset Kaggle untuk apa?**  
A: Validasi akurasi saja. Operational pakai MySQL.

**Q: WHO CSV masih dipakai?**  
A: YA. Untuk calculate Z-score di backend.

**Q: Storage habis berapa untuk 1000 anak?**  
A: ~9 MB (database only, tanpa grafik PNG).

---

## 📚 Dokumentasi Lengkap

- **Cara Penggunaan:** `CARA_PENGGUNAAN_LENGKAP.md`
- **Diagram Visual:** `DIAGRAM_VISUAL.md`
- **Setup MySQL:** `SETUP_MYSQL.md`
- **Quick Start:** `QUICKSTART_MYSQL.md`
- **Integrasi Mobile:** `INTEGRASI_MOBILE.md`

---

## 🚀 Quick Commands

```bash
# Setup
mysql -u root -p balita_sehat < init_database.sql
pip install -r requirements.txt

# Run
python database.py              # Test DB
python api_server_mysql.py      # Start server
python test_api_mysql.py        # Test API

# Check
curl http://localhost:5000/api/health
```

---

**Print this as reference! 📌**
