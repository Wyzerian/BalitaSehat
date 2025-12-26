# Test API Server dengan MySQL Integration

import requests
import json
from datetime import datetime, timedelta

# Base URL API
BASE_URL = "http://localhost:5000"

def print_response(title, response):
    """Helper untuk print response dengan format rapi"""
    print(f"\n{'='*60}")
    print(f"🧪 TEST: {title}")
    print(f"{'='*60}")
    print(f"Status Code: {response.status_code}")
    print(f"Response:")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))

def test_health_check():
    """Test 1: Health Check"""
    response = requests.get(f"{BASE_URL}/api/health")
    print_response("Health Check", response)
    return response.status_code == 200

def test_classify_only():
    """Test 2: Klasifikasi tanpa simpan ke DB"""
    data = {
        "gender": "laki-laki",
        "age_months": 12,
        "height_cm": 75.5,
        "weight_kg": 9.2
    }
    response = requests.post(f"{BASE_URL}/api/classify", json=data)
    print_response("Klasifikasi (Tanpa Simpan)", response)
    return response.status_code == 200

def test_add_measurement_child1():
    """Test 3: Tambah measurement anak pertama"""
    data = {
        "child_id": "TEST001",
        "name": "Budi Santoso",
        "gender": "laki-laki",
        "birth_date": "2024-01-15",
        "age_months": 11,
        "height_cm": 73.5,
        "weight_kg": 9.2
    }
    response = requests.post(f"{BASE_URL}/api/child/measurement", json=data)
    print_response("Tambah Measurement Child 1 (Bulan 11)", response)
    return response.status_code == 200

def test_add_measurement_child1_month2():
    """Test 4: Tambah measurement kedua untuk anak yang sama (untuk trend analysis)"""
    data = {
        "child_id": "TEST001",
        "name": "Budi Santoso",
        "gender": "laki-laki",
        "birth_date": "2024-01-15",
        "age_months": 12,
        "height_cm": 75.0,
        "weight_kg": 9.5
    }
    response = requests.post(f"{BASE_URL}/api/child/measurement", json=data)
    print_response("Tambah Measurement Child 1 (Bulan 12) - Trend Analysis Aktif", response)
    return response.status_code == 200

def test_add_measurement_child2():
    """Test 5: Tambah anak kedua (perempuan)"""
    data = {
        "child_id": "TEST002",
        "name": "Siti Nurhaliza",
        "gender": "perempuan",
        "birth_date": "2024-03-20",
        "age_months": 9,
        "height_cm": 68.5,
        "weight_kg": 7.8
    }
    response = requests.post(f"{BASE_URL}/api/child/measurement", json=data)
    print_response("Tambah Measurement Child 2 (Perempuan)", response)
    return response.status_code == 200

def test_get_history():
    """Test 6: Ambil history anak pertama"""
    response = requests.get(f"{BASE_URL}/api/child/TEST001/history")
    print_response("Get History Child TEST001", response)
    return response.status_code == 200

def test_get_analysis():
    """Test 7: Analisis trend anak pertama"""
    response = requests.get(f"{BASE_URL}/api/child/TEST001/analysis")
    print_response("Trend Analysis Child TEST001", response)
    return response.status_code == 200

def test_get_all_children():
    """Test 8: Lihat semua anak"""
    response = requests.get(f"{BASE_URL}/api/children")
    print_response("Get All Children", response)
    return response.status_code == 200

def test_get_who_thresholds():
    """Test 9: Ambil WHO thresholds"""
    params = {
        "gender": "laki-laki",
        "age_months": 12
    }
    response = requests.get(f"{BASE_URL}/api/who/thresholds", params=params)
    print_response("Get WHO Thresholds (Laki-laki, 12 bulan)", response)
    return response.status_code == 200

def test_delete_child():
    """Test 10: Hapus data testing"""
    print("\n" + "="*60)
    print("🧹 CLEANUP: Hapus Data Testing")
    print("="*60)
    
    # Hapus TEST001
    response1 = requests.delete(f"{BASE_URL}/api/child/TEST001")
    print(f"Delete TEST001: {response1.status_code} - {response1.json()}")
    
    # Hapus TEST002
    response2 = requests.delete(f"{BASE_URL}/api/child/TEST002")
    print(f"Delete TEST002: {response2.status_code} - {response2.json()}")
    
    return response1.status_code == 200 and response2.status_code == 200

def run_all_tests():
    """Jalankan semua test"""
    print("\n" + "🚀"*30)
    print("TESTING API SERVER DENGAN MySQL INTEGRATION")
    print("🚀"*30)
    
    tests = [
        ("Health Check", test_health_check),
        ("Klasifikasi Tanpa Simpan", test_classify_only),
        ("Tambah Measurement Child 1 (Bulan 11)", test_add_measurement_child1),
        ("Tambah Measurement Child 1 (Bulan 12)", test_add_measurement_child1_month2),
        ("Tambah Measurement Child 2", test_add_measurement_child2),
        ("Get History", test_get_history),
        ("Trend Analysis", test_get_analysis),
        ("Get All Children", test_get_all_children),
        ("Get WHO Thresholds", test_get_who_thresholds),
        ("Cleanup (Delete Test Data)", test_delete_child)
    ]
    
    results = []
    for test_name, test_func in tests:
        try:
            success = test_func()
            results.append((test_name, "✅ PASSED" if success else "❌ FAILED"))
        except Exception as e:
            results.append((test_name, f"❌ ERROR: {str(e)}"))
    
    # Summary
    print("\n" + "="*60)
    print("📊 TEST SUMMARY")
    print("="*60)
    for test_name, result in results:
        print(f"{result}: {test_name}")
    
    passed = sum(1 for _, r in results if "✅" in r)
    total = len(results)
    print(f"\n✅ Passed: {passed}/{total}")
    print(f"❌ Failed: {total - passed}/{total}")
    
    if passed == total:
        print("\n🎉 ALL TESTS PASSED! MySQL Integration Working! 🎉")
    else:
        print("\n⚠️ Some tests failed. Check errors above.")

if __name__ == "__main__":
    print("⚠️ IMPORTANT: Make sure the API server is running!")
    print("   Run: python api_server_mysql.py")
    input("\nPress Enter to start testing...")
    
    try:
        run_all_tests()
    except requests.exceptions.ConnectionError:
        print("\n❌ ERROR: Cannot connect to API server!")
        print("   Make sure 'python api_server_mysql.py' is running.")
    except Exception as e:
        print(f"\n❌ ERROR: {e}")
