# 🔌 PANDUAN INTEGRASI MOBILE

## 📱 Untuk Team Mobile Development

---

## 🏗️ Arsitektur Sistem

```
MOBILE APP (Flutter/React Native)
        │
        │ HTTP Request (JSON)
        ▼
   BACKEND API (Python Flask)
        │
        ├─► WHO Classifier (klasifikasi)
        ├─► Growth Tracker (tracking & trend)
        └─► Database (MySQL/PostgreSQL)
```

---

## 📦 FILE YANG DIPERLUKAN

### Backend (Python Server):

| File                       | Status   | Fungsi           |
| -------------------------- | -------- | ---------------- |
| `api_server.py`            | ✅ WAJIB | REST API server  |
| `who_classifier.py`        | ✅ WAJIB | Core klasifikasi |
| `growth_tracker.py`        | ✅ WAJIB | Tracking system  |
| **WHO Data (4 files CSV)** | ✅ WAJIB | Standar WHO      |

### Dataset Kaggle:

- ❌ **TIDAK PERLU** di production! Hanya untuk testing/validasi

### Mobile App:

- Hanya perlu **konsumsi API** (HTTP Request)
- **TIDAK PERLU** install Python atau file .py
- Data dikirim/diterima dalam format **JSON**

---

## 🚀 SETUP BACKEND

### 1. Install Dependencies

```bash
pip install flask flask-cors pandas numpy
```

### 2. Jalankan Server

```bash
python api_server.py
```

Server akan running di: `http://localhost:5000`

### 3. Test API

```bash
curl http://localhost:5000/api/health
```

Response:

```json
{
  "status": "healthy",
  "message": "BalitaSehat API is running"
}
```

---

## 📡 API ENDPOINTS

### **1. Health Check**

```
GET /api/health
```

Response:

```json
{
  "status": "healthy",
  "message": "BalitaSehat API is running",
  "version": "1.0.0"
}
```

---

### **2. Klasifikasi (Single Measurement)**

```
POST /api/classify
Content-Type: application/json
```

**Request Body:**

```json
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
    "gender": "Laki-laki",
    "age_months": 12,
    "height_cm": 75.5,
    "weight_kg": 9.2,
    "zscore_height": -0.1,
    "zscore_weight": -0.43,
    "stunting_status": "Normal",
    "wasting_status": "Normal weight",
    "risk_alert": {
      "has_risk": false,
      "risk_level": "none",
      "risk_messages": []
    }
  }
}
```

---

### **3. Add Measurement (untuk Tracking)**

```
POST /api/child/measurement
Content-Type: application/json
```

**Request Body:**

```json
{
  "child_id": "ANAK001",
  "name": "Budi",
  "gender": "Laki-laki",
  "age_months": 12,
  "height_cm": 75.5,
  "weight_kg": 9.2,
  "measurement_date": "2025-12-23"
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "measurement": {
      "child_id": "ANAK001",
      "name": "Budi",
      "zscore_height": -0.1,
      "zscore_weight": -0.43,
      "stunting_status": "Normal",
      "wasting_status": "Normal weight",
      "risk_alert": {...}
    },
    "trend_analysis": {
      "height_trend": "📈 Membaik (+0.15)",
      "weight_trend": "➡️ Stabil (-0.05)",
      "prediction": {...},
      "risks": []
    }
  }
}
```

---

### **4. Get Child History**

```
GET /api/child/{child_id}/history
```

**Example:**

```
GET /api/child/ANAK001/history
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "child_id": "ANAK001",
    "name": "Budi",
    "gender": "Laki-laki",
    "total_measurements": 7,
    "measurements": [
      {
        "age_months": 0,
        "height_cm": 50.0,
        "weight_kg": 3.3,
        "zscore_height": 0.06,
        "zscore_weight": -0.1,
        "stunting_status": "Normal",
        "wasting_status": "Normal weight"
      },
      {
        "age_months": 1,
        "height_cm": 54.5,
        "weight_kg": 4.2,
        ...
      }
    ]
  }
}
```

---

### **5. Get Trend Analysis & Early Warning**

```
GET /api/child/{child_id}/analysis
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "child_id": "ANAK001",
    "measurements_count": 7,
    "age_range": "0-12 bulan",
    "height_trend": "➡️ Stabil (-0.11)",
    "weight_trend": "📉 Menurun (-0.32)",
    "height_zscore_change": -0.11,
    "weight_zscore_change": -0.32,
    "prediction": {
      "next_age_months": 13,
      "predicted_height_zscore": -0.12,
      "predicted_weight_zscore": -0.39,
      "predicted_stunting": "Normal",
      "predicted_wasting": "Normal weight",
      "warnings": []
    },
    "risks": [
      "⚠️ Z-score berat menurun signifikan - monitoring ketat diperlukan"
    ],
    "has_risk": true
  }
}
```

---

### **6. Get All Children**

```
GET /api/children
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "total_children": 3,
    "children": [
      {
        "child_id": "ANAK001",
        "name": "Budi",
        "gender": "Laki-laki",
        "last_measurement": "2025-12-23",
        "current_age": 12,
        "stunting_status": "Normal",
        "wasting_status": "Normal weight",
        "total_measurements": 7,
        "has_risk": false
      },
      ...
    ]
  }
}
```

---

### **7. Get WHO Thresholds**

```
GET /api/who/thresholds?gender=Laki-laki&age_months=12
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "gender": "Laki-laki",
    "age_months": 12,
    "height": {
      "SD3neg": 68.6,
      "SD2neg": 71.0,
      "SD1neg": 73.4,
      "SD0": 75.7,
      "SD1": 78.1,
      "SD2": 80.5,
      "SD3": 82.9
    },
    "weight": {
      "SD3neg": 6.9,
      "SD2neg": 7.7,
      "SD1neg": 8.6,
      "SD0": 9.6,
      "SD1": 10.8,
      "SD2": 12.0,
      "SD3": 13.3
    }
  }
}
```

---

## 💻 CONTOH IMPLEMENTASI DI MOBILE

### Flutter Example:

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class BalitaSehatAPI {
  static const String baseUrl = 'http://localhost:5000/api';

  // Klasifikasi
  Future<Map<String, dynamic>> classify({
    required String gender,
    required int ageMonths,
    required double heightCm,
    required double weightKg,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/classify'),
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

  // Add Measurement
  Future<Map<String, dynamic>> addMeasurement({
    required String childId,
    required String name,
    required String gender,
    required int ageMonths,
    required double heightCm,
    required double weightKg,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/child/measurement'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'child_id': childId,
        'name': name,
        'gender': gender,
        'age_months': ageMonths,
        'height_cm': heightCm,
        'weight_kg': weightKg,
        'measurement_date': DateTime.now().toIso8601String(),
      }),
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to add measurement');
    }
  }

  // Get History
  Future<Map<String, dynamic>> getChildHistory(String childId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/child/$childId/history'),
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to get history');
    }
  }

  // Get Analysis
  Future<Map<String, dynamic>> getTrendAnalysis(String childId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/child/$childId/analysis'),
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to get analysis');
    }
  }
}
```

**Usage:**

```dart
// Klasifikasi
var api = BalitaSehatAPI();
var result = await api.classify(
  gender: 'Laki-laki',
  ageMonths: 12,
  heightCm: 75.5,
  weightKg: 9.2,
);
print(result['data']['stunting_status']); // "Normal"

// Add measurement
var measurement = await api.addMeasurement(
  childId: 'ANAK001',
  name: 'Budi',
  gender: 'Laki-laki',
  ageMonths: 12,
  heightCm: 75.5,
  weightKg: 9.2,
);
```

---

### React Native Example:

```javascript
const API_BASE_URL = "http://localhost:5000/api";

// Klasifikasi
export const classify = async (gender, ageMonths, heightCm, weightKg) => {
  const response = await fetch(`${API_BASE_URL}/classify`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      gender,
      age_months: ageMonths,
      height_cm: heightCm,
      weight_kg: weightKg,
    }),
  });

  return await response.json();
};

// Add Measurement
export const addMeasurement = async (childData) => {
  const response = await fetch(`${API_BASE_URL}/child/measurement`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      ...childData,
      measurement_date: new Date().toISOString(),
    }),
  });

  return await response.json();
};

// Get History
export const getChildHistory = async (childId) => {
  const response = await fetch(`${API_BASE_URL}/child/${childId}/history`);
  return await response.json();
};
```

---

## 🗄️ DATABASE INTEGRATION

### Opsi 1: Simpan di Memory (sementara)

- Data hilang saat server restart
- Cocok untuk **demo/testing**
- **Sudah implemented** di `growth_tracker.py`

### Opsi 2: SQLite (file-based database)

- Data tersimpan di file
- Tidak perlu install database server
- Cocok untuk **deployment sederhana**

### Opsi 3: MySQL/PostgreSQL (production)

- Database server proper
- Cocok untuk **production**
- Scalable & reliable

**Rekomendasi:** Mulai dengan **in-memory** (demo), lalu upgrade ke **SQLite** atau **MySQL**.

---

## 📂 STRUKTUR DEPLOYMENT

```
Backend Server:
├── api_server.py           ← Main API
├── who_classifier.py       ← Core logic
├── growth_tracker.py       ← Tracking logic
├── data/
│   ├── WHO Indicators Boys 2 years_Tinggi.csv
│   ├── WHO Indicators Girls 2 years_Tinggi.csv
│   ├── WHO Indicators Boys 2 years_Berat.csv
│   └── WHO Indicators Girls 2 years_Berat.csv
└── requirements.txt        ← Dependencies

Mobile App:
└── Cukup konsumsi API via HTTP
```

---

## 🌐 DEPLOYMENT OPTIONS

### 1. **Local Network (untuk demo)**

```bash
# Run server
python api_server.py

# Access dari mobile (same WiFi)
http://192.168.1.100:5000/api/...
```

### 2. **Cloud Hosting (production)**

- **Heroku** (free tier available)
- **Railway** (mudah deploy)
- **Google Cloud Run**
- **AWS EC2**

---

## ✅ CHECKLIST UNTUK TEAM MOBILE

- [ ] Install Postman/Thunder Client untuk test API
- [ ] Test semua endpoint dengan sample data
- [ ] Implement HTTP client di mobile app
- [ ] Handle error responses (404, 400, 500)
- [ ] Implement loading states
- [ ] Cache data lokal (offline mode)
- [ ] Handle network timeout
- [ ] Implement authentication (jika perlu)

---

## 📞 KOORDINASI DENGAN BACKEND

**Yang backend siapkan:**

- ✅ REST API endpoints
- ✅ JSON response format
- ✅ Error handling
- ✅ Documentation (ini!)

**Yang mobile perlu:**

- HTTP client library
- JSON parsing
- UI untuk display data
- Grafik visualization (optional - bisa pakai library chart)

---

## 🎯 NEXT STEPS

1. **Test API** dengan Postman
2. **Integrate** di mobile app (mulai dari endpoint `/classify`)
3. **Implement tracking** (endpoint `/child/measurement`)
4. **Add visualization** (grafik dari data history)
5. **Deploy** ke cloud (Heroku/Railway)

---

**File terkait:**

- [api_server.py](api_server.py) - Main API server
- [who_classifier.py](who_classifier.py) - Core klasifikasi
- [growth_tracker.py](growth_tracker.py) - Tracking system
