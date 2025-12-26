"""
BalitaSehat Project
=====================

Sistem Monitoring Pertumbuhan Anak 0-24 Bulan
Berdasarkan Standar WHO

## Struktur Folder

```
BalitaSehat/
├── app/                    # Core application
│   ├── api_server_mysql.py    # REST API server
│   ├── who_classifier.py      # WHO classification logic
│   ├── growth_tracker_mysql.py # Growth tracking with MySQL
│   └── database.py            # Database connection manager
├── config/                 # Configuration
│   └── db_config.py           # Database credentials
├── data/                   # WHO standard data (CSV)
│   ├── WHO Indicators Boys 2 years_Tinggi.csv
│   ├── WHO Indicators Boys 2 years_Berat.csv
│   ├── WHO Indicators Girls 2 years_Tinggi.csv
│   └── WHO Indicators Girls 2 years_Berat.csv
├── static/                 # Static files
│   └── charts/                # Generated charts (PNG)
├── scripts/                # Utility scripts
│   ├── demo_simulation.py     # Demo without mobile app
│   └── init_database.sql      # Database initialization
├── tests/                  # Testing scripts
│   ├── test_api.py
│   └── test_api_mysql.py
├── docs/                   # Documentation
├── run_server.py           # Main entry point untuk server
└── requirements.txt        # Python dependencies
```

## Quick Start

1. **Setup Database**

   ```bash
   mysql -u root -p < scripts/init_database.sql
   ```

2. **Configure Database**

   - Copy `config/db_config.example.py` to `config/db_config.py`
   - Update credentials

3. **Install Dependencies**

   ```bash
   pip install -r requirements.txt
   ```

4. **Run Server**

   ```bash
   python run_server.py
   ```

5. **Test API**
   ```bash
   cd tests
   python test_api_mysql.py
   ```

## API Endpoints

- `GET /api/health` - Health check
- `POST /api/classify` - Classify child growth
- `POST /api/measurement` - Add measurement
- `GET /api/child/{id}/history` - Get history
- `GET /api/child/{id}/trend` - Trend analysis
- `GET /api/child/{id}/chart?type=growth|zscore` - Generate chart
- `GET /api/children` - List all children
- `DELETE /api/child/{id}` - Delete child

## Documentation

Lihat folder `docs/` untuk dokumentasi lengkap:

- `README.md` - Overview
- `QUICKSTART_MYSQL.md` - Quick start guide
- `CARA_PENGGUNAAN_LENGKAP.md` - Complete usage guide
- `INTEGRASI_MOBILE.md` - Mobile integration guide

## Deployment ke VPS

1. Upload semua file ke VPS
2. Setup MySQL database
3. Install dependencies
4. Configure firewall (port 5000)
5. Run dengan `python run_server.py`
6. Gunakan process manager (pm2, systemd) untuk production

---

© 2025 BalitaSehat - Sistem Monitoring Pertumbuhan Anak
