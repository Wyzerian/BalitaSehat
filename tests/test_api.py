"""
Test API Endpoints
Untuk memastikan semua endpoint bekerja dengan baik
"""

import requests
import json

# Base URL
BASE_URL = 'http://localhost:5000/api'

def print_response(title, response):
    """Print formatted response"""
    print("\n" + "=" * 70)
    print(f"📡 {title}")
    print("=" * 70)
    print(f"Status Code: {response.status_code}")
    print(f"Response:")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))


def test_health_check():
    """Test endpoint /api/health"""
    print("\n🔍 Testing: Health Check")
    response = requests.get(f'{BASE_URL}/health')
    print_response("Health Check", response)
    return response.status_code == 200


def test_classify():
    """Test endpoint /api/classify"""
    print("\n🔍 Testing: Classify")
    
    data = {
        "gender": "Laki-laki",
        "age_months": 12,
        "height_cm": 75.5,
        "weight_kg": 9.2
    }
    
    response = requests.post(
        f'{BASE_URL}/classify',
        headers={'Content-Type': 'application/json'},
        json=data
    )
    
    print_response("Classify - Normal Case", response)
    
    # Test at risk case
    data_at_risk = {
        "gender": "Laki-laki",
        "age_months": 12,
        "height_cm": 71.0,  # At SD2neg
        "weight_kg": 8.5
    }
    
    response2 = requests.post(
        f'{BASE_URL}/classify',
        headers={'Content-Type': 'application/json'},
        json=data_at_risk
    )
    
    print_response("Classify - At Risk Case", response2)
    
    return response.status_code == 200


def test_add_measurement():
    """Test endpoint /api/child/measurement"""
    print("\n🔍 Testing: Add Measurement (Tracking)")
    
    # Add measurement 1
    data1 = {
        "child_id": "ANAK001",
        "name": "Budi",
        "gender": "Laki-laki",
        "age_months": 0,
        "height_cm": 50.0,
        "weight_kg": 3.3,
        "measurement_date": "2025-01-01"
    }
    
    response1 = requests.post(
        f'{BASE_URL}/child/measurement',
        headers={'Content-Type': 'application/json'},
        json=data1
    )
    
    print_response("Add Measurement #1 (age 0)", response1)
    
    # Add measurement 2 (untuk trigger trend analysis)
    data2 = {
        "child_id": "ANAK001",
        "name": "Budi",
        "gender": "Laki-laki",
        "age_months": 1,
        "height_cm": 54.5,
        "weight_kg": 4.2,
        "measurement_date": "2025-02-01"
    }
    
    response2 = requests.post(
        f'{BASE_URL}/child/measurement',
        headers={'Content-Type': 'application/json'},
        json=data2
    )
    
    print_response("Add Measurement #2 (age 1) - dengan Trend Analysis", response2)
    
    # Add more measurements
    measurements = [
        (2, 58.0, 5.1),
        (3, 61.0, 5.8),
        (6, 67.5, 7.5),
        (9, 72.0, 8.8),
        (12, 75.5, 9.2),
    ]
    
    for age, height, weight in measurements:
        data = {
            "child_id": "ANAK001",
            "name": "Budi",
            "gender": "Laki-laki",
            "age_months": age,
            "height_cm": height,
            "weight_kg": weight
        }
        requests.post(
            f'{BASE_URL}/child/measurement',
            headers={'Content-Type': 'application/json'},
            json=data
        )
    
    return response1.status_code == 200


def test_get_history():
    """Test endpoint /api/child/{child_id}/history"""
    print("\n🔍 Testing: Get Child History")
    
    response = requests.get(f'{BASE_URL}/child/ANAK001/history')
    print_response("Get History - ANAK001", response)
    
    return response.status_code == 200


def test_get_analysis():
    """Test endpoint /api/child/{child_id}/analysis"""
    print("\n🔍 Testing: Get Trend Analysis")
    
    response = requests.get(f'{BASE_URL}/child/ANAK001/analysis')
    print_response("Get Analysis - ANAK001", response)
    
    return response.status_code == 200


def test_get_all_children():
    """Test endpoint /api/children"""
    print("\n🔍 Testing: Get All Children")
    
    response = requests.get(f'{BASE_URL}/children')
    print_response("Get All Children", response)
    
    return response.status_code == 200


def test_get_who_thresholds():
    """Test endpoint /api/who/thresholds"""
    print("\n🔍 Testing: Get WHO Thresholds")
    
    response = requests.get(f'{BASE_URL}/who/thresholds?gender=Laki-laki&age_months=12')
    print_response("Get WHO Thresholds - Laki-laki 12 bulan", response)
    
    return response.status_code == 200


def run_all_tests():
    """Jalankan semua test"""
    print("=" * 70)
    print("🧪 API TESTING SUITE")
    print("=" * 70)
    print("\nPastikan server sudah running: python api_server.py")
    input("Press Enter to continue...")
    
    tests = [
        ("Health Check", test_health_check),
        ("Classify", test_classify),
        ("Add Measurement", test_add_measurement),
        ("Get History", test_get_history),
        ("Get Analysis", test_get_analysis),
        ("Get All Children", test_get_all_children),
        ("Get WHO Thresholds", test_get_who_thresholds),
    ]
    
    results = []
    
    for name, test_func in tests:
        try:
            success = test_func()
            results.append((name, success))
        except Exception as e:
            print(f"\n❌ ERROR in {name}: {str(e)}")
            results.append((name, False))
    
    # Summary
    print("\n" + "=" * 70)
    print("📊 TEST SUMMARY")
    print("=" * 70)
    
    for name, success in results:
        status = "✅ PASS" if success else "❌ FAIL"
        print(f"{status} - {name}")
    
    total_pass = sum(1 for _, success in results if success)
    total_tests = len(results)
    
    print(f"\nTotal: {total_pass}/{total_tests} tests passed")
    
    if total_pass == total_tests:
        print("\n🎉 All tests passed!")
    else:
        print("\n⚠️ Some tests failed. Check the output above.")


if __name__ == "__main__":
    try:
        run_all_tests()
    except requests.exceptions.ConnectionError:
        print("\n❌ ERROR: Cannot connect to server!")
        print("Make sure the server is running: python api_server.py")
