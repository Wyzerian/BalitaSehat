# 📖 Cara Penggunaan Lengkap - BalitaSehat System

## 🤔 Penjelasan Konsep Dasar

### Apa yang Tersimpan di Database?

✅ **TERSIMPAN di MySQL:**

- Data anak (nama, gender, tanggal lahir)
- Data pengukuran (tinggi, berat, umur, tanggal pengukuran)
- Hasil klasifikasi (Z-score, status stunting/wasting, risk level)
- Rekomendasi (warnings & recommendations dalam bentuk TEXT)
- Trend analysis (perubahan pertumbuhan per bulan)

❌ **TIDAK TERSIMPAN di Database:**

- Grafik (chart/gambar) - Ini di-generate on-the-fly saat dibutuhkan
- File WHO CSV (ini tetap sebagai file di backend)

### Grafik Bagaimana?

Grafik **TIDAK disimpan** di database sebagai file gambar.

**2 Pilihan untuk Grafik:**

**Pilihan A: Backend Generate Grafik (Python)**

- Backend generate grafik pakai `matplotlib`
- Simpan sebagai file PNG di server
- Kirim URL gambar ke mobile
- Mobile download & tampilkan gambar

**Pilihan B: Mobile Generate Grafik (Recommended)**

- Backend kirim **data mentah** dalam JSON
- Mobile generate grafik sendiri pakai library (MPAndroidChart, dll)
- Lebih responsive & customizable

---

## 🔄 Alur Lengkap Sistem

### Skenario Lengkap: Ibu datang ke Posyandu

```
1. Ibu Budi datang ke Posyandu dengan anaknya (Budi, 11 bulan)

2. Petugas ukur:
   - Tinggi: 73.5 cm
   - Berat: 9.2 kg

3. Petugas input ke mobile app

4. Mobile app kirim data ke backend API

5. Backend:
   - Hitung Z-score pakai WHO standards
   - Klasifikasi status gizi (Normal/Stunting/Underweight)
   - Deteksi risk level (NONE/LOW/MEDIUM/HIGH)
   - Generate warnings & recommendations
   - SIMPAN semua hasil ke MySQL database

6. Backend kirim response JSON ke mobile

7. Mobile app tampilkan hasil:
   - Status: "Normal" atau "At Risk" dll
   - Z-score: -0.5
   - Rekomendasi: "Pertahankan asupan gizi..."

8. Bulan depan, Budi datang lagi (sekarang 12 bulan)
   - Input pengukuran baru
   - System otomatis detect ada riwayat sebelumnya
   - Generate TREND ANALYSIS (naik/turun/stabil)
   - Prediksi bulan depan

9. Petugas bisa lihat:
   - History semua pengukuran Budi
   - Grafik pertumbuhan (tinggi & berat)
   - Trend analysis
   - Early warning jika ada penurunan
```

---

## 📊 Detail: Apa yang Disimpan di Database

### Contoh Data Budi di Database:

**Tabel `children`:**

```
id: CHILD001
name: Budi Santoso
gender: laki-laki
birth_date: 2024-01-15
```

**Tabel `measurements`:**

```
Pengukuran 1 (Bulan 11):
  id: 1
  child_id: CHILD001
  measurement_date: 2024-12-15
  age_months: 11
  height_cm: 73.5
  weight_kg: 9.2

Pengukuran 2 (Bulan 12):
  id: 2
  child_id: CHILD001
  measurement_date: 2025-01-15
  age_months: 12
  height_cm: 75.0
  weight_kg: 9.5
```

**Tabel `classifications`:**

```
Hasil Klasifikasi Pengukuran 1:
  id: 1
  measurement_id: 1
  child_id: CHILD001
  height_zscore: -0.97
  weight_zscore: -0.42
  stunting_status: "At Risk (Early Warning)"
  wasting_status: "Normal weight"
  risk_level: MEDIUM
  warnings: "⚠️ Tinggi badan di bawah median WHO (Z-score: -0.97)"
  recommendations: "✓ Konsultasi ke ahli gizi\n✓ Perbanyak protein..."
```

**Tabel `trend_analysis`:**

```
Analisis setelah Pengukuran 2:
  id: 1
  child_id: CHILD001
  analysis_date: 2025-01-15
  height_trend: "Membaik"
  weight_trend: "Membaik"
  height_change_per_month: 1.5
  weight_change_per_month: 0.3
  predicted_height_next_month: 76.5
  predicted_weight_next_month: 9.8
  warnings: ""
```

---

## 🎯 Cara Penggunaan Lengkap

### STEP 1: Setup Backend (Satu kali saja)

```bash
# 1. Setup MySQL
# - Install XAMPP atau MySQL Server
# - Start MySQL

# 2. Buat database
mysql -u root -p
CREATE DATABASE balita_sehat;

# 3. Import tabel
mysql -u root -p balita_sehat < init_database.sql

# 4. Update db_config.py dengan kredensial MySQL

# 5. Test koneksi
python database.py
# Output: ✓ Berhasil terkoneksi ke database: balita_sehat

# 6. Jalankan API server
python api_server_mysql.py
# Server running di http://192.168.0.100:5000
```

✅ **Backend siap!** Server terus running, mobile bisa akses kapan saja.

---

### STEP 2: Penggunaan Harian di Posyandu

#### **Skenario A: Anak Baru (Pertama Kali Datang)**

**Mobile App:**

```
1. Buka form "Tambah Anak Baru"
2. Input data:
   - ID Anak: CHILD001 (atau auto-generate)
   - Nama: Budi Santoso
   - Jenis Kelamin: Laki-laki
   - Tanggal Lahir: 15 Januari 2024
   - Umur (auto-calculate): 11 bulan

3. Input pengukuran hari ini:
   - Tinggi: 73.5 cm
   - Berat: 9.2 kg
   - Tanggal: 15 Desember 2024 (auto: hari ini)

4. Klik "Simpan & Klasifikasi"
```

**Yang Terjadi di Backend:**

```python
# Mobile kirim POST request:
POST http://192.168.0.100:5000/api/child/measurement
{
  "child_id": "CHILD001",
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 11,
  "height_cm": 73.5,
  "weight_kg": 9.2
}

# Backend proses:
1. Simpan data anak ke tabel `children`
2. Simpan pengukuran ke tabel `measurements`
3. Hitung Z-score:
   - Z tinggi = -0.97
   - Z berat = -0.42
4. Klasifikasi:
   - Stunting: "At Risk (Early Warning)"
   - Wasting: "Normal weight"
   - Risk Level: MEDIUM
5. Generate warnings & recommendations
6. Simpan hasil ke tabel `classifications`
7. Return response JSON
```

**Response JSON yang diterima Mobile:**

```json
{
  "status": "success",
  "data": {
    "measurement_id": 1,
    "child_id": "CHILD001",
    "name": "Budi Santoso",
    "age_months": 11,
    "height_cm": 73.5,
    "weight_kg": 9.2,
    "zscore_height": -0.97,
    "zscore_weight": -0.42,
    "classification_height": "At Risk (Early Warning)",
    "classification_weight": "Normal weight",
    "risk_level": "MEDIUM",
    "warnings": [
      "⚠️ Tinggi badan di bawah median WHO (Z-score: -0.97)",
      "⚠️ Perlu monitoring ketat di pengukuran berikutnya"
    ],
    "recommendations": [
      "✓ Konsultasi ke ahli gizi",
      "✓ Perbanyak protein dalam makanan",
      "✓ Ukur ulang bulan depan"
    ]
  }
}
```

**Mobile App Tampilkan:**

```
┌─────────────────────────────────────┐
│  HASIL PENGUKURAN                   │
│                                     │
│  Nama: Budi Santoso                 │
│  Umur: 11 bulan                     │
│                                     │
│  📏 Tinggi: 73.5 cm                 │
│  Status: ⚠️ At Risk (Early Warning) │
│  Z-score: -0.97                     │
│                                     │
│  ⚖️ Berat: 9.2 kg                   │
│  Status: ✅ Normal weight           │
│  Z-score: -0.42                     │
│                                     │
│  🚨 Risk Level: MEDIUM              │
│                                     │
│  ⚠️ Peringatan:                     │
│  - Tinggi badan di bawah median WHO │
│  - Perlu monitoring ketat           │
│                                     │
│  💡 Rekomendasi:                    │
│  - Konsultasi ke ahli gizi          │
│  - Perbanyak protein                │
│  - Ukur ulang bulan depan           │
└─────────────────────────────────────┘
```

---

#### **Skenario B: Anak Datang Bulan Berikutnya**

**Mobile App:**

```
1. Search anak: "Budi" atau scan QR/barcode CHILD001
2. Muncul data anak yang sudah ada
3. Klik "Tambah Pengukuran Baru"
4. Input pengukuran bulan ini:
   - Umur: 12 bulan (auto-calculate dari birth_date)
   - Tinggi: 75.0 cm
   - Berat: 9.5 kg
   - Tanggal: 15 Januari 2025
5. Klik "Simpan & Analisis"
```

**Yang Terjadi di Backend:**

```python
# Mobile kirim POST request (sama seperti sebelumnya):
POST http://192.168.0.100:5000/api/child/measurement
{
  "child_id": "CHILD001",  # <- ID yang sama
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 12,
  "height_cm": 75.0,
  "weight_kg": 9.5
}

# Backend proses:
1. Cek ada riwayat sebelumnya? ✅ YA (pengukuran bulan 11)
2. Simpan pengukuran baru ke `measurements`
3. Klasifikasi (sama seperti sebelumnya)
4. Simpan ke `classifications`
5. **BONUS: TREND ANALYSIS** (karena ada > 1 measurement)
   - Bandingkan dengan pengukuran sebelumnya
   - Hitung perubahan per bulan:
     * Tinggi: +1.5 cm/bulan (75.0 - 73.5)
     * Berat: +0.3 kg/bulan (9.5 - 9.2)
   - Deteksi trend: "Membaik" (Z-score naik)
   - Prediksi bulan depan:
     * Tinggi: 76.5 cm
     * Berat: 9.8 kg
6. Simpan trend ke `trend_analysis`
7. Return response dengan trend analysis
```

**Response JSON:**

```json
{
  "status": "success",
  "data": {
    "measurement_id": 2,
    "child_id": "CHILD001",
    "classification_height": "Normal",
    "classification_weight": "Normal weight",
    "risk_level": "NONE",
    "trend_analysis": {
      "status": "success",
      "height_trend": "Membaik",
      "weight_trend": "Membaik",
      "height_change_per_month": 1.5,
      "weight_change_per_month": 0.3,
      "current_height_zscore": -0.36,
      "current_weight_zscore": -0.1,
      "prediction_next_month": {
        "next_month_age": 13,
        "predicted_height_cm": 76.5,
        "predicted_weight_kg": 9.8
      },
      "warnings": []
    }
  }
}
```

**Mobile App Tampilkan:**

```
┌─────────────────────────────────────┐
│  HASIL PENGUKURAN                   │
│                                     │
│  Nama: Budi Santoso                 │
│  Umur: 12 bulan                     │
│                                     │
│  📏 Tinggi: 75.0 cm                 │
│  Status: ✅ Normal                  │
│  Z-score: -0.36 (Naik dari -0.97!)  │
│                                     │
│  ⚖️ Berat: 9.5 kg                   │
│  Status: ✅ Normal weight           │
│  Z-score: -0.10                     │
│                                     │
│  🎉 Risk Level: NONE                │
│                                     │
│  📈 TREND ANALYSIS:                 │
│  ┌─────────────────────────────┐   │
│  │ Tinggi: 📈 Membaik          │   │
│  │ Perubahan: +1.5 cm/bulan    │   │
│  │                             │   │
│  │ Berat: 📈 Membaik           │   │
│  │ Perubahan: +0.3 kg/bulan    │   │
│  └─────────────────────────────┘   │
│                                     │
│  🔮 Prediksi Bulan Depan (13 bln):  │
│  - Tinggi: 76.5 cm                  │
│  - Berat: 9.8 kg                    │
│                                     │
│  [Lihat Grafik] [Lihat Riwayat]    │
└─────────────────────────────────────┘
```

---

#### **Skenario C: Lihat Riwayat & Grafik**

**Mobile App - User klik "Lihat Riwayat":**

```kotlin
// Mobile request:
GET http://192.168.0.100:5000/api/child/CHILD001/history
```

**Response JSON (Semua pengukuran dari database):**

```json
{
  "status": "success",
  "child_id": "CHILD001",
  "total_measurements": 2,
  "data": [
    {
      "measurement_id": 1,
      "measurement_date": "2024-12-15",
      "age_months": 11,
      "height_cm": 73.5,
      "weight_kg": 9.2,
      "zscore_height": -0.97,
      "zscore_weight": -0.42,
      "classification_height": "At Risk",
      "classification_weight": "Normal weight"
    },
    {
      "measurement_id": 2,
      "measurement_date": "2025-01-15",
      "age_months": 12,
      "height_cm": 75.0,
      "weight_kg": 9.5,
      "zscore_height": -0.36,
      "zscore_weight": -0.1,
      "classification_height": "Normal",
      "classification_weight": "Normal weight"
    }
  ]
}
```

**Mobile App - Generate Grafik:**

**Pilihan A: Pakai Library Chart di Android (MPAndroidChart)**

```kotlin
// Parse JSON data
val measurements = response.data

// Extract data untuk grafik
val ages = measurements.map { it.age_months }  // [11, 12]
val heights = measurements.map { it.height_cm } // [73.5, 75.0]
val weights = measurements.map { it.weight_kg } // [9.2, 9.5]

// Generate line chart dengan MPAndroidChart
val entries = mutableListOf<Entry>()
for (i in measurements.indices) {
    entries.add(Entry(ages[i].toFloat(), heights[i].toFloat()))
}

val lineDataSet = LineDataSet(entries, "Tinggi Badan")
lineDataSet.color = Color.BLUE
lineDataSet.setCircleColor(Color.BLUE)

val lineData = LineData(lineDataSet)
chart.data = lineData
chart.invalidate() // Refresh chart
```

**Pilihan B: Backend Generate Grafik (Python)**

```python
# Jika mau grafik di-generate backend:
# File: visualize_growth.py sudah ada!

# Mobile request:
GET http://192.168.0.100:5000/api/child/CHILD001/chart?type=growth

# Backend:
import matplotlib.pyplot as plt
from visualize_growth import plot_growth_chart

# Generate grafik dari database
history = tracker.get_child_history('CHILD001')
plot_growth_chart(history, 'laki-laki', 'Budi Santoso')

# Save sebagai file PNG
plt.savefig('static/charts/CHILD001_growth.png')

# Return URL
return {
  "chart_url": "http://192.168.0.100:5000/static/charts/CHILD001_growth.png"
}

# Mobile download & tampilkan image
```

---

## 📊 Tentang Grafik (Penjelasan Detail)

### Grafik TIDAK Tersimpan di Database!

**Kenapa?**

- Grafik itu file gambar (.png/.jpg) yang ukurannya besar
- Database MySQL untuk data terstruktur (angka, text), bukan file
- Grafik bisa di-generate ulang kapan saja dari data yang ada

**2 Cara Handle Grafik:**

### **Cara 1: Backend Generate (Python matplotlib)**

**Kelebihan:**

- Mobile tidak perlu library charting
- Grafik konsisten (semua user lihat grafik yang sama)

**Kekurangan:**

- Harus simpan file PNG di server (butuh storage)
- Mobile harus download image (lebih lambat)
- Tidak interaktif (tidak bisa zoom, pan, dll)

**Flow:**

```
1. Mobile request grafik
2. Backend baca data dari MySQL
3. Backend generate PNG pakai matplotlib
4. Simpan PNG di folder `static/charts/`
5. Return URL ke mobile
6. Mobile download & tampilkan image
```

**File tersimpan di server:**

```
backend/
  static/
    charts/
      CHILD001_growth.png       ← Grafik tinggi/berat
      CHILD001_zscore.png       ← Grafik Z-score
      CHILD002_growth.png
      ...
```

**Cleanup otomatis:**

```python
# Hapus file grafik lama (> 7 hari) untuk hemat storage
import os
from datetime import datetime, timedelta

for file in os.listdir('static/charts/'):
    file_path = f'static/charts/{file}'
    if os.path.getmtime(file_path) < (datetime.now() - timedelta(days=7)):
        os.remove(file_path)
```

---

### **Cara 2: Mobile Generate (Recommended untuk production)**

**Kelebihan:**

- Tidak butuh storage di server
- Grafik interaktif (bisa zoom, pan, tooltip)
- Lebih cepat (no download image)
- Customizable per user preference

**Kekurangan:**

- Mobile harus implement charting library

**Flow:**

```
1. Mobile request data (JSON)
2. Backend kirim data mentah
3. Mobile generate grafik langsung di app
```

**Library Android untuk Charting:**

- **MPAndroidChart** (most popular)
- **PhilJay Chart**
- **HelloCharts**
- **AnyChart**

**Contoh dengan MPAndroidChart:**

```kotlin
// 1. Add dependency
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

// 2. Get data dari API
val history = api.getHistory("CHILD001")

// 3. Generate chart
val entries = history.data.map {
    Entry(it.age_months.toFloat(), it.height_cm.toFloat())
}

val dataSet = LineDataSet(entries, "Tinggi Badan (cm)")
dataSet.color = Color.BLUE
dataSet.lineWidth = 2f
dataSet.setDrawCircles(true)
dataSet.setCircleColor(Color.BLUE)

val lineData = LineData(dataSet)
lineChart.data = lineData
lineChart.description.text = "Grafik Pertumbuhan Tinggi"
lineChart.invalidate()
```

---

## 🎯 Rekomendasi untuk Tim

### Untuk Demo Hackathon:

**Cara 1 (Backend Generate)** - Lebih cepat implement!

- Grafik siap pakai dari Python
- Mobile tinggal tampilkan image
- Cocok untuk demo karena cepat

### Untuk Production (Posyandu Real):

**Cara 2 (Mobile Generate)** - Lebih baik jangka panjang!

- User experience lebih baik (interaktif)
- Hemat server storage
- Scalable (banyak user tidak masalah)

---

## 📝 Summary Flow Lengkap

```
┌──────────────────────────────────────────────────────────┐
│  FLOW LENGKAP SISTEM BALITASEHAT                         │
└──────────────────────────────────────────────────────────┘

1. SETUP (Satu Kali)
   Backend: Setup MySQL → Import tabel → Jalankan API server
   Mobile: Setup Retrofit → Configure base URL

2. PENGGUNAAN HARIAN

   Anak Baru:
   Mobile → Input data → POST /api/child/measurement
   Backend → Classify → Save to MySQL → Return JSON
   Mobile → Display hasil (status, Z-score, warnings)

   Anak Datang Lagi:
   Mobile → Search anak → Input pengukuran → POST /api/child/measurement
   Backend → Classify → Compare dengan sebelumnya → Trend analysis
   Backend → Save to MySQL → Return JSON + trend
   Mobile → Display hasil + trend (naik/turun/stabil) + prediksi

   Lihat Riwayat:
   Mobile → GET /api/child/{id}/history
   Backend → Query MySQL → Return semua pengukuran
   Mobile → Display table/list riwayat

   Lihat Grafik:
   Option A: Backend generate PNG → Return URL → Mobile display image
   Option B: Mobile generate dari data JSON (recommended)

3. DATA YANG TERSIMPAN
   MySQL Database:
   ✅ Data anak (children table)
   ✅ Semua pengukuran (measurements table)
   ✅ Semua hasil klasifikasi (classifications table)
   ✅ Trend analysis (trend_analysis table)

   Server Files:
   ✅ WHO CSV files (standar referensi)
   ⚠️ Grafik PNG (jika pakai backend generate - optional)

   Mobile:
   ❌ Tidak simpan data lokal (semua dari server)
   ⚠️ Cache response untuk offline viewing (optional)
```

---

## ✅ Checklist Implementasi

### Backend (Sudah Selesai ✅):

- [x] Database schema (4 tabel)
- [x] API endpoints (8 endpoints)
- [x] WHO classification algorithm
- [x] Growth tracker dengan trend analysis
- [x] Visualisasi grafik (visualize_growth.py)
- [x] Testing script

### Mobile (Tim Mobile Kerja):

- [ ] Setup Retrofit/HTTP client
- [ ] Screen: Input anak baru
- [ ] Screen: Search anak existing
- [ ] Screen: Input pengukuran
- [ ] Screen: Display hasil klasifikasi
- [ ] Screen: Display trend analysis
- [ ] Screen: Riwayat pengukuran (table/list)
- [ ] Screen: Grafik pertumbuhan (chart)
- [ ] Handle error & loading states

---

## 🎉 Kesimpulan

**Apa yang tersimpan di database:**

- Data anak, pengukuran, klasifikasi, trend analysis (TEXT/NUMBER)

**Grafik:**

- **TIDAK tersimpan** di database sebagai file
- **Option A:** Backend generate PNG → simpan di folder server → kirim URL
- **Option B:** Mobile generate langsung dari data JSON (recommended)

**Penggunaan:**

1. Setup backend sekali
2. Mobile input data → API simpan ke MySQL → return hasil
3. Bulan depan input lagi → dapat trend analysis
4. Lihat riwayat & grafik kapan saja (data dari MySQL)

**Semua jelas sekarang?** Jika masih bingung bagian tertentu, tanya lagi! 🚀
