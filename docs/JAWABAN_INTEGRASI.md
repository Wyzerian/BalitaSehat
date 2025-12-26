# 📋 JAWABAN: Integrasi dengan Mobile

---

## ❓ Pertanyaan 2: Bagaimana integrasikan ke mobile?

### **JAWABAN LENGKAP:**

---

## 📦 **1. FILE DATASET - APA YANG DIGUNAKAN?**

### ✅ **File WAJIB untuk Backend:**

| File                                      | Ukuran | Fungsi                            | Lokasi  |
| ----------------------------------------- | ------ | --------------------------------- | ------- |
| `WHO Indicators Boys 2 years_Tinggi.csv`  | ~1 KB  | Standar TB laki-laki (0-24 bulan) | Backend |
| `WHO Indicators Girls 2 years_Tinggi.csv` | ~1 KB  | Standar TB perempuan (0-24 bulan) | Backend |
| `WHO Indicators Boys 2 years_Berat.csv`   | ~3 KB  | Standar BB laki-laki (0-60 bulan) | Backend |
| `WHO Indicators Girls 2 years_Berat.csv`  | ~3 KB  | Standar BB perempuan (0-60 bulan) | Backend |

**Total: ~8 KB** (sangat kecil!)

### ❌ **File TIDAK DIPERLUKAN:**

| File                                           | Kenapa Tidak Perlu?                                         |
| ---------------------------------------------- | ----------------------------------------------------------- |
| `stunting_wasting_dataset.csv` (100k data)     | ❌ Hanya untuk **validasi/testing**, bukan untuk production |
| `WHO Indicators Boys/Girls 2 years_Berat.xlsx` | ❌ Sudah dikonversi ke CSV                                  |
| `validate_with_kaggle.py`                      | ❌ Hanya untuk testing akurasi                              |
| `convert_excel_to_csv.py`                      | ❌ Hanya untuk setup awal                                   |

---

## 🏗️ **2. ARSITEKTUR INTEGRASI**

### **Opsi yang Direkomendasikan: REST API**

```
┌──────────────────┐                  ┌──────────────────┐
│   MOBILE APP     │                  │  BACKEND SERVER  │
│  (Flutter/RN)    │ ◄─── JSON ────► │  (Python Flask)  │
│                  │   HTTP Request   │                  │
│  - Input form    │                  │  - WHO Classifier│
│  - Display data  │                  │  - Growth Tracker│
│  - Grafik        │                  │  - 4 WHO CSV     │
└──────────────────┘                  └──────────────────┘
                                              │
                                              ▼
                                      ┌──────────────────┐
                                      │    DATABASE      │
                                      │ (MySQL/SQLite)   │
                                      └──────────────────┘
```

### **Kenapa Pakai REST API?**

✅ **Mobile TIDAK perlu install Python**
✅ **Mobile TIDAK perlu load WHO data**
✅ **Data WHO tetap di server** (aman & terpusat)
✅ **Mobile cukup kirim/terima JSON**
✅ **Bisa dipakai multi-platform** (Android, iOS, Web)

---

## 📂 **3. FILE APA SAJA YANG DIGUNAKAN?**

### **Di Backend Server:**

```
BalitaSehat/
├── api_server.py           ← Main API (REST endpoints)
├── who_classifier.py       ← Core klasifikasi WHO
├── growth_tracker.py       ← Tracking & trend analysis
├── requirements.txt        ← Python dependencies
│
├── data/
│   ├── WHO Indicators Boys 2 years_Tinggi.csv    ← WAJIB
│   ├── WHO Indicators Girls 2 years_Tinggi.csv   ← WAJIB
│   ├── WHO Indicators Boys 2 years_Berat.csv     ← WAJIB
│   └── WHO Indicators Girls 2 years_Berat.csv    ← WAJIB
│
└── (File lain TIDAK diperlukan untuk production)
```

### **Di Mobile App:**

Mobile **HANYA perlu**:

- HTTP Client (untuk request ke API)
- JSON Parser
- UI untuk input/display data
- (Optional) Library untuk grafik

Mobile **TIDAK perlu**:

- ❌ File Python (.py)
- ❌ File WHO CSV
- ❌ Dataset Kaggle
- ❌ Install Python

---

## 🔌 **4. CARA INTEGRASI**

### **Step 1: Setup Backend**

```bash
# Install dependencies
pip install -r requirements.txt

# Jalankan server
python api_server.py

# Server running di: http://localhost:5000
```

### **Step 2: Test API dengan Postman**

**Endpoint 1: Klasifikasi**

```
POST http://localhost:5000/api/classify
Content-Type: application/json

{
  "gender": "Laki-laki",
  "age_months": 12,
  "height_cm": 75.5,
  "weight_kg": 9.2
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "zscore_height": -0.1,
    "zscore_weight": -0.43,
    "stunting_status": "Normal",
    "wasting_status": "Normal weight",
    "risk_alert": {
      "has_risk": false,
      "risk_level": "none"
    }
  }
}
```

### **Step 3: Implementasi di Mobile**

**Flutter Example:**

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

Future<Map<String, dynamic>> classifyChild({
  required String gender,
  required int ageMonths,
  required double heightCm,
  required double weightKg,
}) async {
  final response = await http.post(
    Uri.parse('http://YOUR_SERVER_IP:5000/api/classify'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'gender': gender,
      'age_months': ageMonths,
      'height_cm': heightCm,
      'weight_kg': weightKg,
    }),
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    throw Exception('Failed to classify');
  }
}

// Usage
var result = await classifyChild(
  gender: 'Laki-laki',
  ageMonths: 12,
  heightCm: 75.5,
  weightKg: 9.2,
);

print(result['data']['stunting_status']); // "Normal"
```

---

## 📡 **5. API ENDPOINTS YANG TERSEDIA**

| Endpoint                   | Method | Fungsi                         |
| -------------------------- | ------ | ------------------------------ |
| `/api/health`              | GET    | Cek status server              |
| `/api/classify`            | POST   | Klasifikasi single measurement |
| `/api/child/measurement`   | POST   | Add measurement + tracking     |
| `/api/child/{id}/history`  | GET    | Ambil riwayat anak             |
| `/api/child/{id}/analysis` | GET    | Trend analysis & early warning |
| `/api/children`            | GET    | List semua anak                |
| `/api/who/thresholds`      | GET    | Ambil threshold WHO            |

**Full documentation:** [INTEGRASI_MOBILE.md](INTEGRASI_MOBILE.md)

---

## 🗄️ **6. DATABASE - BAGAIMANA?**

### **Opsi 1: In-Memory (untuk demo)**

- Data tersimpan di RAM
- Hilang saat server restart
- ✅ **Sudah implemented** di `growth_tracker.py`
- Cocok untuk **hackathon demo**

### **Opsi 2: SQLite (file-based)**

- Data tersimpan di file `.db`
- Tidak perlu install database server
- Cocok untuk **deployment sederhana**

### **Opsi 3: MySQL/PostgreSQL**

- Database server proper
- Cocok untuk **production**
- Scalable & reliable

**Rekomendasi untuk hackathon:**

1. Mulai dengan **in-memory** (paling mudah)
2. Jika ada waktu, upgrade ke **SQLite**
3. Production nanti bisa pakai **MySQL**

---

## 🚀 **7. DEPLOYMENT**

### **Untuk Demo Hackathon:**

**Opsi A: Local Network (Same WiFi)**

```bash
# Di laptop (backend)
python api_server.py

# Di mobile
http://192.168.X.X:5000/api/...
```

**Opsi B: Ngrok (Instant Public URL)**

```bash
# Install ngrok
# Run server
python api_server.py

# Di terminal lain
ngrok http 5000

# Dapat URL: https://abc123.ngrok.io
# Pakai di mobile: https://abc123.ngrok.io/api/...
```

### **Untuk Production:**

- **Railway** (gratis, mudah deploy)
- **Heroku** (gratis tier)
- **Google Cloud Run**
- **AWS EC2**

---

## ✅ **CHECKLIST UNTUK TIM**

### **Backend Team (Kamu):**

- [x] Buat API server (`api_server.py`)
- [x] Siapkan WHO data (4 files CSV)
- [x] Test API dengan Postman
- [ ] Deploy ke cloud (Railway/Heroku)
- [ ] Kasih URL API ke mobile team

### **Mobile Team:**

- [ ] Setup HTTP client
- [ ] Test endpoint `/api/classify`
- [ ] Implement form input (gender, umur, TB, BB)
- [ ] Display hasil klasifikasi
- [ ] (Optional) Grafik perkembangan

### **Koordinasi:**

- [ ] Sepakati format JSON
- [ ] Test integrasi bersama
- [ ] Handle error cases
- [ ] Setup database (jika perlu)

---

## 📝 **SUMMARY**

### **Pertanyaan: Dataset ini saya apakan?**

**JAWABAN:**

1. **4 file WHO CSV** → Simpan di folder `data/` di backend server
2. **Dataset Kaggle** → **TIDAK PERLU** di production (hanya testing)
3. **File Excel WHO** → **TIDAK PERLU** (sudah dikonversi ke CSV)

### **Pertanyaan: File apa saja yang digunakan?**

**JAWABAN:**

- **Backend:** `api_server.py`, `who_classifier.py`, `growth_tracker.py`, + 4 WHO CSV
- **Mobile:** Tidak perlu file Python, **cukup konsumsi API via HTTP**

### **Pertanyaan: Bagaimana cara integrasikan?**

**JAWABAN:**

1. ✅ Backend jalankan API server (`python api_server.py`)
2. ✅ Mobile kirim HTTP request (POST/GET)
3. ✅ Data dikirim/diterima dalam format **JSON**
4. ✅ **SIMPLE!** Mobile tidak perlu install Python atau load WHO data

---

## 🎯 **NEXT STEPS**

1. **Test API** dengan file `test_api.py` yang sudah saya buat
2. **Kasih URL server** ke mobile team
3. **Mobile team test** dengan Postman dulu
4. **Integrate** di mobile app
5. **Deploy** ke cloud (Railway recommended)

---

**File terkait:**

- [api_server.py](api_server.py) - Main REST API
- [test_api.py](test_api.py) - Test suite untuk API
- [INTEGRASI_MOBILE.md](INTEGRASI_MOBILE.md) - Dokumentasi lengkap integrasi
- [requirements.txt](requirements.txt) - Dependencies Python

**Sudah saya buatkan semuanya! Tinggal test dan deploy** 🚀
