# BalitaSehat - WHO-based Growth Monitoring System

Sistem monitoring pertumbuhan anak berbasis standar WHO untuk aplikasi Posyandu.

## 📁 Struktur Project

```
BalitaSehat/
├── data/
│   ├── stunting_wasting_dataset.csv          # Dataset Kaggle (untuk validasi)
│   ├── WHO Indicators Boys 2 years_Tinggi.csv   # Standar WHO Laki-laki
│   └── WHO Indicators Girls 2 years_Tinggi.csv  # Standar WHO Perempuan
│
├── who_classifier.py          # ✅ Algoritma klasifikasi berbasis WHO
├── validate_with_kaggle.py    # ✅ Validasi dengan dataset Kaggle
├── growth_tracker.py          # ✅ Sistem tracking & trend analysis
└── api_example.py             # 🔜 Contoh API untuk mobile team
```

## 🎯 Cara Kerja Sistem

### 1. **WHO Classifier** (`who_classifier.py`)

Klasifikasi status gizi anak menggunakan Z-score WHO.

**Formula:**

```
Z-score = [(nilai/M)^L - 1] / (L × S)
```

**Input:**

- Jenis Kelamin
- Umur (bulan)
- Tinggi Badan (cm)
- Berat Badan (kg)

**Output:**

- Status Stunting (Severely Stunted, Stunted, Normal, Tall)
- Status Wasting (Severely Underweight, Underweight, Normal weight, Risk of Overweight, Overweight)
- Z-score untuk TB dan BB
- Risk Alert (warning jika mendekati threshold)

### 2. **Validasi** (`validate_with_kaggle.py`)

Memvalidasi akurasi algoritma dengan dataset Kaggle yang sudah memiliki label.

**Target Akurasi:** ~100% (karena rule-based, bukan ML)

### 3. **Growth Tracker** (`growth_tracker.py`)

Sistem untuk tracking perkembangan anak per bulan.

**Fitur:**

- Menyimpan riwayat pengukuran per anak
- Analisis trend pertumbuhan
- Deteksi risiko (Z-score menurun)
- Prediksi sederhana untuk bulan depan
- Alert jika mendekati threshold berbahaya

## 🚀 Cara Menggunakan

### 1. Install Dependencies

```bash
pip install pandas numpy tqdm
```

### 2. Jalankan Demo WHO Classifier

```bash
python who_classifier.py
```

Output:

```
WHO GROWTH CLASSIFICATION SYSTEM
================================================================
✓ Data WHO berhasil dimuat
  - Laki-laki: 25 bulan data
  - Perempuan: 25 bulan data

📊 Contoh 1: Anak Laki-laki, 12 bulan
Gender        : Laki-laki
Umur          : 12 bulan
Tinggi Badan  : 75.5 cm (Z-score: -0.23)
Berat Badan   : 9.2 kg (Z-score: 0.15)
Status Stunting: Normal
Status Wasting : Normal weight
✅ Status: Normal, tidak ada risiko terdeteksi
```

### 3. Validasi dengan Dataset Kaggle

```bash
python validate_with_kaggle.py
```

### 4. Demo Growth Tracking

```bash
python growth_tracker.py
```

## 📊 Contoh Penggunaan di Code

### Klasifikasi Single Data

```python
from who_classifier import WHOClassifier

# Inisialisasi
classifier = WHOClassifier(
    who_boys_path='data/WHO Indicators Boys 2 years_Tinggi.csv',
    who_girls_path='data/WHO Indicators Girls 2 years_Tinggi.csv'
)

# Klasifikasi
result = classifier.classify(
    gender='Laki-laki',
    age_months=12,
    height_cm=75.5,
    weight_kg=9.2
)

print(result)
# Output:
# {
#   'stunting_status': 'Normal',
#   'wasting_status': 'Normal weight',
#   'zscore_height': -0.23,
#   'zscore_weight': 0.15,
#   'risk_alert': {...}
# }
```

### Tracking Pertumbuhan Anak

```python
from growth_tracker import GrowthTracker
from who_classifier import WHOClassifier

classifier = WHOClassifier(...)
tracker = GrowthTracker(classifier)

# Input data bulan 0
tracker.add_measurement(
    child_id='ANAK001',
    name='Budi',
    gender='Laki-laki',
    age_months=0,
    height_cm=50.0,
    weight_kg=3.3
)

# Input data bulan 1
tracker.add_measurement(
    child_id='ANAK001',
    name='Budi',
    gender='Laki-laki',
    age_months=1,
    height_cm=54.5,
    weight_kg=4.2
)

# Lihat riwayat
history = tracker.get_child_history('ANAK001')
print(history)

# Analisis trend
trend = tracker.analyze_trend('ANAK001')
print(trend)
```

## 🔧 Integrasi dengan Mobile Team

Sistem ini siap diintegrasikan sebagai:

### Option 1: REST API (Flask/FastAPI)

```python
from flask import Flask, request, jsonify

app = Flask(__name__)
classifier = WHOClassifier(...)

@app.route('/api/classify', methods=['POST'])
def classify():
    data = request.json
    result = classifier.classify(
        gender=data['gender'],
        age_months=data['age_months'],
        height_cm=data['height_cm'],
        weight_kg=data['weight_kg']
    )
    return jsonify(result)
```

### Option 2: Batch Processing

Proses file CSV dari mobile → return hasil klasifikasi

### Option 3: Database Integration

Save langsung ke database (SQLite/PostgreSQL/MySQL)

## 📈 Next Steps

1. ✅ **Selesai:** Algoritma klasifikasi WHO
2. ✅ **Selesai:** Validasi dengan Kaggle
3. ✅ **Selesai:** Growth tracking system
4. 🔜 **TODO:** REST API untuk mobile
5. 🔜 **TODO:** Database integration
6. 🔜 **TODO:** Grafik visualisasi (matplotlib/plotly)
7. 🔜 **TODO:** Export laporan PDF

## 📚 Referensi

- [WHO Child Growth Standards](https://www.who.int/tools/child-growth-standards)
- [WHO LMS Method](https://www.who.int/tools/child-growth-standards/standards)

## 👥 Tim

- **Machine Learning/Data Mining:** [Nama kamu]
- **Mobile Development:** [Nama team mobile]

## 📝 Catatan Penting

### Dataset Kaggle vs Data Real

- **Dataset Kaggle (100k data):** Hanya untuk **validasi** algoritma
- **Data WHO:** Standar/threshold untuk klasifikasi
- **Data Posyandu (yang kalian buat):** Data operasional dengan tracking per anak

### Ini BUKAN Machine Learning Tradisional

- Sistem ini **rule-based** menggunakan standar medis WHO
- **Tidak perlu training model** (sudah ada standar baku)
- Fokus di **data mining** & **trend analysis**

### Kapan Perlu ML?

ML bisa ditambahkan untuk:

- **Clustering:** Kelompokkan anak dengan pola pertumbuhan serupa
- **Advanced Forecasting:** LSTM untuk prediksi lebih akurat
- **Risk Scoring:** Jika ada data tambahan (ASI, imunisasi, dll)
