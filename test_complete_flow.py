"""
Test Complete Flow - BalitaSehat API
Test dari tambah data sampai generate grafik

Cara pakai:
1. python run_server.py (di terminal lain)
2. python test_complete_flow.py
"""

import requests
import json
from datetime import datetime, timedelta

BASE_URL = "http://localhost:5000"

print("="*70)
print("🧪 TESTING BALITASEHAT API - COMPLETE FLOW")
print("="*70)

# Step 1: Health Check
print("\n[1] Testing Health Check...")
response = requests.get(f"{BASE_URL}/api/health")
print(f"✓ Status: {response.json()['status']}")
print(f"✓ Database: {response.json()['database']}")

# Step 2: Tambah Data Anak Pertama Kali
print("\n[2] Tambah Data Anak - Bulan 1 (Umur 16 bulan)...")
data_bulan1 = {
    "child_id": "DEMO_BUDI",
    "name": "Budi Santoso",
    "gender": "laki-laki",
    "birth_date": "2024-01-15",
    "age_months": 16,
    "height_cm": 85.0,
    "weight_kg": 13.0,
    "measurement_date": "2024-12-15"
}

response = requests.post(
    f"{BASE_URL}/api/child/measurement",
    headers={"Content-Type": "application/json"},
    json=data_bulan1
)

if response.status_code == 200:
    result = response.json()
    print(f"✓ Data tersimpan!")
    print(f"  - Z-score Tinggi: {result['data'].get('zscore_height', 'N/A')}")
    print(f"  - Z-score Berat: {result['data'].get('zscore_weight', 'N/A')}")
    print(f"  - Status Tinggi: {result['data'].get('classification_height', 'N/A')}")
    print(f"  - Status Berat: {result['data'].get('classification_weight', 'N/A')}")
    print(f"  - Risk Level: {result['data'].get('risk_level', 'N/A')}")
else:
    print(f"✗ Error: {response.text}")
    exit(1)

# Step 3: Tambah Data Bulan 2 (Update)
print("\n[3] Tambah Data Anak - Bulan 2 (Umur 17 bulan)...")
data_bulan2 = {
    "child_id": "DEMO_BUDI",
    "name": "Budi Santoso",
    "gender": "laki-laki",
    "birth_date": "2024-01-15",
    "age_months": 17,
    "height_cm": 86.5,
    "weight_kg": 13.5,
    "measurement_date": "2025-01-15"
}

response = requests.post(
    f"{BASE_URL}/api/child/measurement",
    headers={"Content-Type": "application/json"},
    json=data_bulan2
)

if response.status_code == 200:
    result = response.json()
    print(f"✓ Data bulan 2 tersimpan!")
    if 'trend_analysis' in result['data']:
        trend = result['data']['trend_analysis']
        print(f"  - Trend Tinggi: {trend.get('height_trend', 'N/A')}")
        print(f"  - Trend Berat: {trend.get('weight_trend', 'N/A')}")

# Step 4: Tambah Data Bulan 3
print("\n[4] Tambah Data Anak - Bulan 3 (Umur 18 bulan)...")
data_bulan3 = {
    "child_id": "DEMO_BUDI",
    "name": "Budi Santoso",
    "gender": "laki-laki",
    "birth_date": "2024-01-15",
    "age_months": 18,
    "height_cm": 88.0,
    "weight_kg": 14.0,
    "measurement_date": "2025-02-15"
}

response = requests.post(
    f"{BASE_URL}/api/child/measurement",
    headers={"Content-Type": "application/json"},
    json=data_bulan3
)

if response.status_code == 200:
    print(f"✓ Data bulan 3 tersimpan!")

# Step 5: Lihat History
print("\n[5] Ambil History Lengkap...")
response = requests.get(f"{BASE_URL}/api/child/DEMO_BUDI/history")

if response.status_code == 200:
    result = response.json()
    print(f"✓ Total pengukuran: {result['total_measurements']}")
    print(f"\n📊 Riwayat Lengkap:")
    for idx, measurement in enumerate(result['data'], 1):
        print(f"\n  Pengukuran {idx}:")
        print(f"    Tanggal: {measurement.get('measurement_date')}")
        print(f"    Umur: {measurement.get('age_months')} bulan")
        print(f"    Tinggi: {measurement.get('height_cm')} cm")
        print(f"    Berat: {measurement.get('weight_kg')} kg")
        print(f"    Z-score TB: {measurement.get('zscore_height')}")
        print(f"    Z-score BB: {measurement.get('zscore_weight')}")

# Step 6: Generate Grafik Pertumbuhan
print("\n[6] Generate Grafik Pertumbuhan...")
response = requests.get(f"{BASE_URL}/api/child/DEMO_BUDI/chart?type=growth")

if response.status_code == 200:
    result = response.json()
    print(f"✓ Grafik berhasil dibuat!")
    print(f"  File: {result['filename']}")
    print(f"  URL: {result['full_url']}")
    print(f"\n  🌐 Buka di browser:")
    print(f"     {result['full_url']}")
    growth_url = result['full_url']
else:
    print(f"✗ Error: {response.text}")

# Step 7: Generate Grafik Z-Score
print("\n[7] Generate Grafik Z-Score...")
response = requests.get(f"{BASE_URL}/api/child/DEMO_BUDI/chart?type=zscore")

if response.status_code == 200:
    result = response.json()
    print(f"✓ Grafik Z-Score berhasil dibuat!")
    print(f"  File: {result['filename']}")
    print(f"  URL: {result['full_url']}")
    print(f"\n  🌐 Buka di browser:")
    print(f"     {result['full_url']}")
    zscore_url = result['full_url']
else:
    print(f"✗ Error: {response.text}")

# Step 8: Trend Analysis
print("\n[8] Analisis Trend...")
response = requests.get(f"{BASE_URL}/api/child/DEMO_BUDI/analysis")

if response.status_code == 200:
    result = response.json()
    trend = result['data']
    print(f"✓ Analisis Trend:")
    print(f"  - Trend Tinggi: {trend.get('height_trend')}")
    print(f"  - Trend Berat: {trend.get('weight_trend')}")
    print(f"  - Perubahan TB/bulan: {trend.get('height_change_per_month', 0):.2f} cm")
    print(f"  - Perubahan BB/bulan: {trend.get('weight_change_per_month', 0):.2f} kg")
    
    if 'prediction_next_month' in trend:
        pred = trend['prediction_next_month']
        print(f"\n  🔮 Prediksi Bulan Depan ({pred['next_month_age']} bulan):")
        print(f"     Tinggi: {pred['predicted_height_cm']:.1f} cm")
        print(f"     Berat: {pred['predicted_weight_kg']:.1f} kg")

# Step 9: List All Children
print("\n[9] List Semua Anak...")
response = requests.get(f"{BASE_URL}/api/children")

if response.status_code == 200:
    result = response.json()
    print(f"✓ Total anak terdaftar: {result['total']}")
    for child in result['data']:
        print(f"\n  - {child['name']} ({child['id']})")
        print(f"    Gender: {child['gender']}")
        print(f"    Total pengukuran: {child.get('total_measurements', 0)}")
        print(f"    Risk Level: {child.get('latest_risk_level', 'N/A')}")

print("\n" + "="*70)
print("✅ TESTING SELESAI!")
print("="*70)
print("\n📁 File grafik tersimpan di: static/charts/")
print("   - DEMO_BUDI_growth.png")
print("   - DEMO_BUDI_zscore.png")
print("\n🌐 Buka grafik di browser:")
print(f"   {BASE_URL}/static/charts/DEMO_BUDI_growth.png")
print(f"   {BASE_URL}/static/charts/DEMO_BUDI_zscore.png")
print("")
