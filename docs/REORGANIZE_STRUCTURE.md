# 📁 Struktur Folder Baru - BalitaSehat (Rapi & Terorganisir)

## 🎯 Struktur Folder yang Direkomendasikan

```
BalitaSehat/
│
├── 📁 app/                          # Core aplikasi (Python files)
│   ├── api_server_mysql.py          # API server utama (MySQL)
│   ├── who_classifier.py            # WHO classification algorithm
│   ├── growth_tracker_mysql.py      # Growth tracker dengan MySQL
│   ├── database.py                  # Database connection manager
│   └── visualize_growth.py          # Generate grafik (matplotlib)
│
├── 📁 config/                       # Konfigurasi
│   ├── db_config.py                 # MySQL credentials (jangan commit!)
│   └── db_config.example.py         # Template config
│
├── 📁 data/                         # Data & Standards
│   ├── WHO Indicators Boys 2 years_Tinggi.csv
│   ├── WHO Indicators Girls 2 years_Tinggi.csv
│   ├── WHO Indicators Boys 2 years_Berat.csv
│   ├── WHO Indicators Girls 2 years_Berat.csv
│   └── stunting_wasting_dataset.csv # Kaggle dataset (validasi)
│
├── 📁 static/                       # File statis untuk web
│   ├── charts/                      # Grafik PNG yang di-generate
│   │   ├── CHILD001_growth.png
│   │   ├── CHILD001_zscore.png
│   │   └── .gitkeep
│   └── css/                         # (optional untuk web UI)
│
├── 📁 scripts/                      # Utility scripts
│   ├── init_database.sql            # SQL schema
│   ├── demo_simulation.py           # Demo/simulasi tanpa mobile
│   ├── convert_excel_to_csv.py      # Convert WHO Excel to CSV
│   ├── verify_who_calculation.py    # Verify Z-score accuracy
│   └── cleanup_old_charts.py        # Hapus grafik lama (> 7 hari)
│
├── 📁 tests/                        # Testing scripts
│   ├── test_api_mysql.py            # API integration tests
│   ├── validate_with_kaggle.py      # Validasi akurasi
│   └── test_who_classifier.py       # Unit tests untuk classifier
│
├── 📁 docs/                         # Dokumentasi
│   ├── SETUP_MYSQL.md               # Panduan setup MySQL
│   ├── INTEGRASI_MOBILE.md          # Panduan integrasi mobile
│   ├── CARA_PENGGUNAAN_LENGKAP.md   # Cara penggunaan lengkap
│   ├── GRAFIK_AUTO_UPDATE.md        # Penjelasan grafik auto-update
│   ├── QUICKSTART_MYSQL.md          # Quick start guide
│   ├── CHEAT_SHEET.md               # Quick reference
│   └── API_DOCUMENTATION.md         # API endpoints documentation
│
├── 📁 legacy/                       # File lama (backup)
│   ├── api_server.py                # In-memory version (backup)
│   ├── growth_tracker.py            # In-memory version (backup)
│   └── demo_new_threshold.py        # Demo lama
│
├── 📄 requirements.txt              # Python dependencies
├── 📄 .gitignore                    # Git ignore rules
├── 📄 README.md                     # Main README
├── 📄 run_server.py                 # Script untuk jalankan server
└── 📄 run_demo.py                   # Script untuk jalankan demo

```

---

## 📋 Langkah Reorganisasi

### 1. Buat folder structure
```bash
mkdir app config static/charts scripts tests docs legacy
```

### 2. Pindahkan file ke folder yang sesuai

**Core aplikasi → `app/`:**
```bash
move api_server_mysql.py app/
move who_classifier.py app/
move growth_tracker_mysql.py app/
move database.py app/
move visualize_growth.py app/
```

**Konfigurasi → `config/`:**
```bash
move db_config.py config/
move db_config.example.py config/
```

**Scripts utility → `scripts/`:**
```bash
move init_database.sql scripts/
move convert_excel_to_csv.py scripts/
move verify_who_calculation.py scripts/
```

**Testing → `tests/`:**
```bash
move test_api_mysql.py tests/
move validate_with_kaggle.py tests/
```

**Dokumentasi → `docs/`:**
```bash
move SETUP_MYSQL.md docs/
move INTEGRASI_MOBILE.md docs/
move CARA_PENGGUNAAN_LENGKAP.md docs/
move GRAFIK_AUTO_UPDATE.md docs/
move VISUALISASI_UPDATE_GRAFIK.md docs/
move QUICKSTART_MYSQL.md docs/
move CHEAT_SHEET.md docs/
move MYSQL_INTEGRATION_COMPLETE.md docs/
move RINGKASAN_MYSQL.md docs/
move PENJELASAN_THRESHOLD.md docs/
move DIAGRAM_VISUAL.md docs/
move INTEGRASI_MOBILE.md docs/
move JAWABAN_INTEGRASI.md docs/
```

**Legacy/backup → `legacy/`:**
```bash
move api_server.py legacy/
move growth_tracker.py legacy/
move demo_new_threshold.py legacy/
move test_api.py legacy/
move README_LENGKAP.md legacy/
move README_NEW.md legacy/
```

---

## 🗂️ Struktur Setelah Reorganisasi

```
BalitaSehat/
├── app/
│   ├── __init__.py
│   ├── api_server_mysql.py
│   ├── who_classifier.py
│   ├── growth_tracker_mysql.py
│   ├── database.py
│   └── visualize_growth.py
│
├── config/
│   ├── db_config.py
│   └── db_config.example.py
│
├── data/
│   ├── WHO*.csv (4 files)
│   └── stunting_wasting_dataset.csv
│
├── static/
│   └── charts/
│       └── .gitkeep
│
├── scripts/
│   ├── init_database.sql
│   ├── demo_simulation.py
│   ├── convert_excel_to_csv.py
│   ├── verify_who_calculation.py
│   └── cleanup_old_charts.py
│
├── tests/
│   ├── test_api_mysql.py
│   └── validate_with_kaggle.py
│
├── docs/
│   ├── SETUP_MYSQL.md
│   ├── INTEGRASI_MOBILE.md
│   ├── CARA_PENGGUNAAN_LENGKAP.md
│   └── ... (semua .md files)
│
├── legacy/
│   ├── api_server.py
│   ├── growth_tracker.py
│   └── ... (file lama)
│
├── requirements.txt
├── .gitignore
├── README.md
├── run_server.py
└── run_demo.py
```

---

## 🔧 Update Import Paths

Setelah reorganisasi, update import di file Python:

**Before (old structure):**
```python
from who_classifier import WHOClassifier
from growth_tracker_mysql import GrowthTrackerMySQL
from database import DatabaseConnection
```

**After (new structure):**
```python
from app.who_classifier import WHOClassifier
from app.growth_tracker_mysql import GrowthTrackerMySQL
from app.database import DatabaseConnection
```

---

## 📝 File Baru yang Perlu Dibuat

### 1. `app/__init__.py`
```python
"""BalitaSehat App Package"""
__version__ = '2.0.0'
```

### 2. `run_server.py` (di root)
```python
"""Main entry point untuk jalankan server"""
import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from app.api_server_mysql import app

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
```

### 3. `run_demo.py` (di root)
```python
"""Demo simulation tanpa mobile app"""
import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from scripts.demo_simulation import main

if __name__ == '__main__':
    main()
```

### 4. `static/charts/.gitkeep`
```
# Folder untuk menyimpan grafik yang di-generate
# File .gitkeep ini agar folder ter-commit ke Git
```

---

## ⚙️ Update Configuration Paths

### Update `app/api_server_mysql.py`:
```python
# Before
from who_classifier import WHOClassifier

# After
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from app.who_classifier import WHOClassifier
from app.growth_tracker_mysql import GrowthTrackerMySQL
from app.database import DatabaseConnection

# Update paths
classifier = WHOClassifier(
    who_boys_height_path='data/WHO Indicators Boys 2 years_Tinggi.csv',
    who_girls_height_path='data/WHO Indicators Girls 2 years_Tinggi.csv',
    who_boys_weight_path='data/WHO Indicators Boys 2 years_Berat.csv',
    who_girls_weight_path='data/WHO Indicators Girls 2 years_Berat.csv'
)
```

### Update `config/db_config.py`:
```python
# Database Configuration
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'balita_sehat',
    'port': 3306,
    'charset': 'utf8mb4',
    'autocommit': True
}
```

---

## 🎯 Benefits Struktur Baru

✅ **Organized:** Semua file terkelompok sesuai fungsi  
✅ **Scalable:** Mudah tambah fitur baru  
✅ **Clean:** Dokumentasi terpisah dari code  
✅ **Professional:** Siap untuk production/deployment  
✅ **Easy Deployment:** Struktur jelas untuk VPS  
✅ **Git Friendly:** .gitignore bisa lebih spesifik  

---

## 🚀 Deploy ke VPS

Dengan struktur baru, deploy ke VPS jadi mudah:

```bash
# 1. Clone/upload ke VPS
git clone <repo> /var/www/balitasehat

# 2. Setup virtual environment
cd /var/www/balitasehat
python3 -m venv venv
source venv/bin/activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Setup database
mysql -u root -p < scripts/init_database.sql

# 5. Configure
cp config/db_config.example.py config/db_config.py
nano config/db_config.py  # Edit credentials

# 6. Run with Gunicorn (production)
gunicorn -w 4 -b 0.0.0.0:5000 run_server:app

# Or systemd service
sudo systemctl start balitasehat
```

---

## 📊 Folder static/charts

**Fungsi:** Menyimpan grafik PNG yang di-generate backend

**Struktur:**
```
static/charts/
├── CHILD001_growth_20241223.png      # Grafik pertumbuhan
├── CHILD001_zscore_20241223.png      # Grafik Z-score
├── CHILD002_growth_20241223.png
├── CHILD002_zscore_20241223.png
└── .gitkeep
```

**Auto Cleanup:**
```python
# scripts/cleanup_old_charts.py
import os
from datetime import datetime, timedelta

def cleanup_old_charts(days=7):
    chart_dir = 'static/charts'
    now = datetime.now()
    
    for file in os.listdir(chart_dir):
        if file.endswith('.png'):
            file_path = os.path.join(chart_dir, file)
            file_time = datetime.fromtimestamp(os.path.getmtime(file_path))
            
            if now - file_time > timedelta(days=days):
                os.remove(file_path)
                print(f"Deleted old chart: {file}")

# Jalankan dengan cron job setiap hari
```

---

## ✅ Checklist Reorganisasi

- [ ] Buat folder structure baru
- [ ] Pindahkan file ke folder sesuai
- [ ] Update import paths di semua Python files
- [ ] Buat `app/__init__.py`
- [ ] Buat `run_server.py` dan `run_demo.py`
- [ ] Update path di `.gitignore`
- [ ] Test import: `python -c "from app.who_classifier import WHOClassifier"`
- [ ] Test server: `python run_server.py`
- [ ] Test demo: `python run_demo.py`
- [ ] Commit ke Git
- [ ] Ready for VPS deployment!

---

**Next:** Jalankan script reorganisasi otomatis! 🚀
