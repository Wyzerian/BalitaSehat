"""
Test Complete Posyandu Workflow
Simulate petugas posyandu menggunakan sistem
"""

import requests
import json
from datetime import datetime, timedelta

BASE_URL = "http://localhost:5000"

def print_step(step_num, description):
    """Print step dengan format yang rapi"""
    print(f"\n{'='*70}")
    print(f"STEP {step_num}: {description}")
    print('='*70)

def print_response(response):
    """Print response dengan format JSON yang rapi"""
    try:
        data = response.json()
        print(f"Status Code: {response.status_code}")
        print(f"Response:")
        print(json.dumps(data, indent=2, ensure_ascii=False))
    except:
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")

def test_posyandu_workflow():
    """
    Simulate complete workflow:
    1. Ibu datang dengan anak pertama kali
    2. Petugas input NIK (tidak ditemukan)
    3. Petugas register anak baru
    4. Input measurement bulan ke-0
    5. Ibu datang bulan berikutnya
    6. Input measurement bulan ke-1
    7. Generate chart
    """
    
    print("\n" + "="*70)
    print("TEST: COMPLETE POSYANDU WORKFLOW")
    print("="*70)
    
    # Data test
    nik_test = "3303050520240003"
    
    # ========================================================================
    # STEP 1: Check Health
    # ========================================================================
    print_step(1, "Health Check - Cek server aktif")
    
    response = requests.get(f"{BASE_URL}/api/health")
    print_response(response)
    
    if response.status_code != 200:
        print("\n❌ Server tidak aktif! Pastikan server running.")
        return
    
    # ========================================================================
    # STEP 2: Check NIK (Anak Baru - Belum Terdaftar)
    # ========================================================================
    print_step(2, "Check NIK - Ibu datang pertama kali")
    
    response = requests.get(f"{BASE_URL}/api/child/check", params={"nik": nik_test})
    print_response(response)
    
    expected_status = "not_found"
    actual_status = response.json().get('status')
    
    if actual_status == expected_status:
        print("✅ NIK belum terdaftar (expected)")
    else:
        print(f"⚠️  Unexpected status: {actual_status}")
    
    # ========================================================================
    # STEP 3: Register Anak Baru
    # ========================================================================
    print_step(3, "Register Anak - Daftar anak baru")
    
    register_data = {
        "nik_anak": nik_test,
        "name": "Ahmad Rizki",
        "gender": "laki-laki",
        "birth_date": "2024-05-05"
    }
    
    response = requests.post(
        f"{BASE_URL}/api/child/register",
        json=register_data,
        headers={"Content-Type": "application/json"}
    )
    print_response(response)
    
    if response.status_code == 201:
        child_data = response.json()['data']
        child_id = child_data['child_id']
        print(f"✅ Anak berhasil didaftarkan dengan ID: {child_id}")
    else:
        print("❌ Gagal register anak")
        return
    
    # ========================================================================
    # STEP 4: Verify NIK Sekarang Terdaftar
    # ========================================================================
    print_step(4, "Verify - NIK sekarang terdaftar")
    
    response = requests.get(f"{BASE_URL}/api/child/check", params={"nik": nik_test})
    print_response(response)
    
    if response.json().get('status') == 'found':
        print("✅ NIK berhasil ditemukan")
    else:
        print("❌ NIK tidak ditemukan")
        return
    
    # ========================================================================
    # STEP 5: Input Measurement Bulan 0 (Pertama Kali)
    # ========================================================================
    print_step(5, "Input Measurement - Bulan ke-0")
    
    # Hitung tanggal (bayi umur 0 bulan = birth_date)
    birth_date = datetime.strptime("2024-05-05", "%Y-%m-%d")
    measurement_date_0 = birth_date + timedelta(days=15)  # 15 hari setelah lahir
    
    measurement_data_0 = {
        "nik_anak": nik_test,
        "height_cm": 50.5,
        "weight_kg": 3.2,
        "measurement_date": measurement_date_0.strftime("%Y-%m-%d")
    }
    
    response = requests.post(
        f"{BASE_URL}/api/measurement/add",
        json=measurement_data_0,
        headers={"Content-Type": "application/json"}
    )
    print_response(response)
    
    if response.status_code == 201:
        result = response.json()['data']
        print(f"✅ Measurement bulan 0 berhasil disimpan")
        print(f"   - Age: {result['child']['age_months']} bulan")
        print(f"   - Height Z-score: {result['classification'].get('height_zscore', 'N/A')}")
        print(f"   - Weight Z-score: {result['classification'].get('weight_zscore', 'N/A')}")
    else:
        print("❌ Gagal input measurement")
        return
    
    # ========================================================================
    # STEP 6: Input Measurement Bulan 1
    # ========================================================================
    print_step(6, "Input Measurement - Bulan ke-1")
    
    measurement_date_1 = birth_date + timedelta(days=35)  # ~1 bulan
    
    measurement_data_1 = {
        "nik_anak": nik_test,
        "height_cm": 54.2,
        "weight_kg": 4.1,
        "measurement_date": measurement_date_1.strftime("%Y-%m-%d")
    }
    
    response = requests.post(
        f"{BASE_URL}/api/measurement/add",
        json=measurement_data_1,
        headers={"Content-Type": "application/json"}
    )
    print_response(response)
    
    if response.status_code == 201:
        result = response.json()['data']
        print(f"✅ Measurement bulan 1 berhasil disimpan")
        print(f"   - Classification Height: {result['classification'].get('classification_height')}")
        print(f"   - Classification Weight: {result['classification'].get('classification_weight')}")
    else:
        print("❌ Gagal input measurement bulan 1")
        return
    
    # ========================================================================
    # STEP 7: Input Measurement Bulan 2
    # ========================================================================
    print_step(7, "Input Measurement - Bulan ke-2")
    
    measurement_date_2 = birth_date + timedelta(days=65)  # ~2 bulan
    
    measurement_data_2 = {
        "nik_anak": nik_test,
        "height_cm": 57.8,
        "weight_kg": 5.3,
        "measurement_date": measurement_date_2.strftime("%Y-%m-%d")
    }
    
    response = requests.post(
        f"{BASE_URL}/api/measurement/add",
        json=measurement_data_2,
        headers={"Content-Type": "application/json"}
    )
    print_response(response)
    
    # ========================================================================
    # STEP 8: Get Child History
    # ========================================================================
    print_step(8, "Get History - Riwayat pertumbuhan anak")
    
    response = requests.get(f"{BASE_URL}/api/child/{child_id}/history")
    print_response(response)
    
    if response.status_code == 200:
        history = response.json()['data']
        print(f"✅ Total measurements: {len(history)}")
    
    # ========================================================================
    # STEP 9: Generate Growth Chart
    # ========================================================================
    print_step(9, "Generate Chart - Grafik pertumbuhan")
    
    response = requests.get(f"{BASE_URL}/api/child/{child_id}/chart", params={"type": "growth"})
    print_response(response)
    
    if response.status_code == 200:
        chart_url = response.json()['full_url']
        print(f"✅ Growth chart: {chart_url}")
    
    # ========================================================================
    # STEP 10: Generate Z-Score Chart
    # ========================================================================
    print_step(10, "Generate Z-Score Chart")
    
    response = requests.get(f"{BASE_URL}/api/child/{child_id}/chart", params={"type": "zscore"})
    print_response(response)
    
    if response.status_code == 200:
        chart_url = response.json()['full_url']
        print(f"✅ Z-Score chart: {chart_url}")
    
    # ========================================================================
    # SUMMARY
    # ========================================================================
    print("\n" + "="*70)
    print("WORKFLOW COMPLETE!")
    print("="*70)
    print(f"\n✅ Anak berhasil didaftarkan: {child_data['name']}")
    print(f"✅ Child ID: {child_id}")
    print(f"✅ NIK: {nik_test}")
    print(f"✅ Total measurements: 3")
    print(f"\n📊 Charts tersedia di:")
    print(f"   - Growth: http://localhost:5000/static/charts/{child_id}_growth.png")
    print(f"   - Z-Score: http://localhost:5000/static/charts/{child_id}_zscore.png")
    print("\n" + "="*70)

if __name__ == "__main__":
    try:
        test_posyandu_workflow()
    except requests.exceptions.ConnectionError:
        print("\n❌ Error: Tidak dapat terhubung ke server!")
        print("Pastikan server running di http://localhost:5000")
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
