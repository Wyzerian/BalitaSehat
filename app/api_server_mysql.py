"""
REST API dengan MySQL Integration
Menggunakan Flask + MySQL untuk data persistence
"""

from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import numpy as np
import pandas as pd
from datetime import datetime
import os
import matplotlib
matplotlib.use('Agg')  # Backend untuk server (no display)
import matplotlib.pyplot as plt

# Import classifier dan tracker MySQL version
from app.who_classifier import WHOClassifier
from app.growth_tracker_mysql import GrowthTrackerMySQL
from app.database import DatabaseConnection

# Get absolute paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIC_DIR = os.path.join(BASE_DIR, 'static')
CHARTS_DIR = os.path.join(STATIC_DIR, 'charts')

# Inisialisasi Flask
app = Flask(__name__)
CORS(app)  # Enable CORS untuk mobile app

# Ensure static/charts folder exists
os.makedirs(CHARTS_DIR, exist_ok=True)

# Test koneksi database
print("Testing database connection...")
if DatabaseConnection.test_connection():
    print("✓ Database connected successfully!")
else:
    print("✗ Database connection failed! Check db_config.py")

# Inisialisasi WHO Classifier (load saat startup)
print("Initializing WHO Classifier...")
classifier = WHOClassifier(
    who_boys_height_path='data/WHO Indicators Boys 2 years_Tinggi.csv',
    who_girls_height_path='data/WHO Indicators Girls 2 years_Tinggi.csv',
    who_boys_weight_path='data/WHO Indicators Boys 2 years_Berat.csv',
    who_girls_weight_path='data/WHO Indicators Girls 2 years_Berat.csv'
)

# Inisialisasi Growth Tracker dengan MySQL
tracker = GrowthTrackerMySQL(classifier)

print("✓ Server ready!")


# ============================================================================
# HELPER FUNCTIONS
# ============================================================================
def _generate_recommendations(stunting_status, wasting_status, risk_level):
    """
    Generate rekomendasi berdasarkan status gizi
    """
    recommendations = []
    
    # Rekomendasi untuk stunting
    if 'Severely Stunted' in stunting_status:
        recommendations.append("🏥 Segera konsultasi ke dokter anak atau ahli gizi")
        recommendations.append("📋 Program pemberian makanan tambahan (PMT) intensif")
        recommendations.append("💊 Suplementasi vitamin dan mineral sesuai anjuran dokter")
    elif 'Stunted' in stunting_status:
        recommendations.append("👨‍⚕️ Konsultasi ke puskesmas atau ahli gizi")
        recommendations.append("🍎 Tingkatkan asupan protein hewani (telur, ikan, daging)")
        recommendations.append("🥛 Berikan susu dan produk olahan susu")
        recommendations.append("📊 Monitoring pertumbuhan setiap bulan")
    elif 'At Risk' in stunting_status:
        recommendations.append("⚠️ Perhatikan pola makan bergizi seimbang")
        recommendations.append("🍳 Pastikan protein hewani dalam menu harian")
        recommendations.append("📅 Monitoring pertumbuhan rutin setiap bulan")
    
    # Rekomendasi untuk wasting
    if 'Severely' in wasting_status and 'underweight' in wasting_status.lower():
        recommendations.append("🚨 Butuh intervensi medis segera")
        recommendations.append("🍽️ Program pemberian makanan tambahan (PMT)")
        recommendations.append("💉 Cek kemungkinan infeksi atau penyakit penyerta")
    elif 'Underweight' in wasting_status or 'underweight' in wasting_status.lower():
        recommendations.append("📈 Tingkatkan frekuensi makan (5-6x sehari)")
        recommendations.append("🥑 Berikan makanan padat kalori (alpukat, kacang, minyak)")
        recommendations.append("🥛 Tambahkan camilan bergizi di antara waktu makan")
    elif 'Overweight' in wasting_status:
        recommendations.append("⚖️ Batasi makanan tinggi gula dan lemak")
        recommendations.append("🏃 Tingkatkan aktivitas fisik sesuai usia")
        recommendations.append("🥗 Perbanyak sayur dan buah")
    
    # Rekomendasi umum untuk normal
    if risk_level == 'none' and len(recommendations) == 0:
        recommendations.append("✅ Pertahankan pola makan bergizi seimbang")
        recommendations.append("🏃 Ajak anak bermain dan bergerak aktif")
        recommendations.append("📅 Lakukan pemantauan rutin setiap bulan")
    
    return recommendations


# ============================================================================
# ENDPOINT 1: Health Check
# ============================================================================
@app.route('/api/health', methods=['GET'])
def health_check():
    """
    Cek status server dan database
    
    GET /api/health
    """
    db_status = DatabaseConnection.test_connection()
    
    return jsonify({
        'status': 'healthy' if db_status else 'database_error',
        'message': 'BalitaSehat API is running',
        'database': 'connected' if db_status else 'disconnected',
        'version': '2.0.0-mysql'
    })


# ============================================================================
# ENDPOINT 2: Check NIK (Cek apakah anak sudah terdaftar)
# ============================================================================
@app.route('/api/child/check', methods=['GET'])
def check_nik():
    """
    Cek apakah NIK anak sudah terdaftar di sistem
    
    GET /api/child/check?nik=3301234567890123
    
    Response jika ditemukan:
    {
        "status": "found",
        "data": {
            "child_id": "BUDI001",
            "nik_anak": "3301234567890123",
            "name": "Budi Santoso",
            "gender": "laki-laki",
            "birth_date": "2024-08-01",
            "created_at": "2025-12-23 10:00:00"
        }
    }
    
    Response jika tidak ditemukan:
    {
        "status": "not_found",
        "message": "NIK belum terdaftar"
    }
    """
    try:
        nik_anak = request.args.get('nik')
        
        if not nik_anak:
            return jsonify({
                'status': 'error',
                'message': 'Parameter NIK diperlukan'
            }), 400
        
        # Validasi NIK format (16 digit)
        if not nik_anak.isdigit() or len(nik_anak) != 16:
            return jsonify({
                'status': 'error',
                'message': 'NIK harus 16 digit angka'
            }), 400
        
        # Cari anak berdasarkan NIK
        child = tracker.get_child_by_nik(nik_anak)
        
        if child:
            return jsonify({
                'status': 'found',
                'data': child
            })
        else:
            return jsonify({
                'status': 'not_found',
                'message': 'NIK belum terdaftar. Silakan daftar anak terlebih dahulu.'
            })
    
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 3: Register Anak Baru
# ============================================================================
@app.route('/api/child/register', methods=['POST'])
def register_child():
    """
    Daftarkan anak baru ke sistem posyandu
    
    POST /api/child/register
    Body:
    {
        "nik_anak": "3301234567890123",
        "name": "Budi Santoso",
        "parent_name": "Ibu Siti",
        "address": "Jl. Merdeka No. 123, Jakarta",
        "gender": "laki-laki",
        "birth_date": "2024-08-01"
    }
    
    Response:
    {
        "status": "success",
        "message": "Anak berhasil didaftarkan",
        "data": {
            "child_id": "BUDI001",
            "nik_anak": "3301234567890123",
            "name": "Budi Santoso",
            "parent_name": "Ibu Siti",
            "address": "Jl. Merdeka No. 123, Jakarta"
        }
    }
    """
    try:
        data = request.get_json()
        
        # Validasi input
        required_fields = ['nik_anak', 'name', 'parent_name', 'address', 'gender', 'birth_date']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    'status': 'error',
                    'message': f'Field {field} diperlukan'
                }), 400
        
        nik_anak = data['nik_anak']
        
        # Validasi NIK format
        if not nik_anak.isdigit() or len(nik_anak) != 16:
            return jsonify({
                'status': 'error',
                'message': 'NIK harus 16 digit angka'
            }), 400
        
        # Cek apakah NIK sudah terdaftar
        existing = tracker.get_child_by_nik(nik_anak)
        if existing:
            return jsonify({
                'status': 'error',
                'message': f'NIK sudah terdaftar atas nama {existing["name"]}',
                'data': existing
            }), 409  # Conflict
        
        # Generate child_id dari nama (ambil 4 huruf pertama + random)
        import re
        from datetime import datetime
        
        name_clean = re.sub(r'[^A-Za-z]', '', data['name'])[:4].upper()
        timestamp = datetime.now().strftime('%H%M%S')[-3:]
        child_id = f"{name_clean}{timestamp}"
        
        # Tambahkan anak ke database
        result = tracker.add_child(
            child_id=child_id,
            nik_anak=nik_anak,
            name=data['name'],
            parent_name=data['parent_name'],
            address=data['address'],
            gender=data['gender'],
            birth_date=data['birth_date']
        )
        
        if result['status'] == 'success':
            return jsonify({
                'status': 'success',
                'message': 'Anak berhasil didaftarkan',
                'data': {
                    'child_id': child_id,
                    'nik_anak': nik_anak,
                    'name': data['name'],
                    'parent_name': data['parent_name'],
                    'address': data['address'],
                    'gender': data['gender'],
                    'birth_date': data['birth_date']
                }
            }), 201
        else:
            return jsonify(result), 500
    
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 4: Input Measurement (Simplified - Posyandu Workflow)
# ============================================================================
@app.route('/api/measurement/add', methods=['POST'])
def add_measurement_simple():
    """
    Input measurement baru - SIMPLIFIED untuk workflow posyandu
    Petugas cukup input NIK + tinggi + berat
    
    POST /api/measurement/add
    Body:
    {
        "nik_anak": "3301150120240001",
        "height_cm": 85.0,
        "weight_kg": 13.0,
        "measurement_date": "2024-12-23"  // optional, default: today
    }
    
    Response:
    {
        "status": "success",
        "message": "Data pengukuran berhasil disimpan",
        "data": {
            "measurement_id": 5,
            "child": {
                "child_id": "BUDI001",
                "name": "Budi Santoso",
                "age_months": 11
            },
            "measurement": {
                "height_cm": 85.0,
                "weight_kg": 13.0,
                "measurement_date": "2024-12-23"
            },
            "classification": {
                "height_zscore": 1.85,
                "weight_zscore": 0.32,
                "classification_height": "Normal",
                "classification_weight": "Normal",
                "risk_level": "NONE"
            },
            "chart_urls": {
                "growth": "http://localhost:5000/static/charts/BUDI001_growth.png",
                "zscore": "http://localhost:5000/static/charts/BUDI001_zscore.png"
            }
        }
    }
    """
    try:
        data = request.get_json()
        
        # Validasi input
        required_fields = ['nik_anak', 'height_cm', 'weight_kg']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    'status': 'error',
                    'message': f'Field {field} diperlukan'
                }), 400
        
        nik_anak = data['nik_anak']
        
        # Step 1: Lookup child by NIK
        child = tracker.get_child_by_nik(nik_anak)
        if not child:
            return jsonify({
                'status': 'error',
                'message': f'NIK {nik_anak} tidak ditemukan. Silakan daftar anak terlebih dahulu.',
                'suggestion': 'Gunakan endpoint POST /api/child/register'
            }), 404
        
        # Step 2: Calculate age_months from birth_date
        from datetime import datetime
        measurement_date = data.get('measurement_date', datetime.now().strftime('%Y-%m-%d'))
        
        birth_date = datetime.strptime(child['birth_date'], '%Y-%m-%d')
        measure_date = datetime.strptime(measurement_date, '%Y-%m-%d')
        
        age_months = (measure_date.year - birth_date.year) * 12 + (measure_date.month - birth_date.month)
        
        # Validasi umur (0-24 bulan)
        if age_months < 0 or age_months > 24:
            return jsonify({
                'status': 'error',
                'message': f'Umur anak ({age_months} bulan) di luar range 0-24 bulan'
            }), 400
        
        # Step 3: Add measurement
        result = tracker.add_measurement(
            child_id=child['id'],
            name=child['name'],
            gender=child['gender'],
            birth_date=child['birth_date'],
            age_months=age_months,
            height_cm=float(data['height_cm']),
            weight_kg=float(data['weight_kg']),
            measurement_date=measurement_date
        )
        
        if result['status'] == 'error':
            return jsonify(result), 500
        
        # Step 4: Auto-generate charts (growth and zscore)
        child_id = child['id']
        base_url = request.host_url.rstrip('/')
        
        try:
            # Generate both charts by calling internal chart generation
            import requests as req
            
            # Generate growth chart
            req.get(f"{base_url}/api/child/{child_id}/chart?type=growth", timeout=5)
            
            # Generate zscore chart  
            req.get(f"{base_url}/api/child/{child_id}/chart?type=zscore", timeout=5)
        except Exception as e:
            # Don't fail if chart generation fails
            print(f"Warning: Chart auto-generation failed: {e}")
        
        # Step 5: Prepare response with chart URLs
        
        return jsonify({
            'status': 'success',
            'message': 'Data pengukuran berhasil disimpan',
            'data': {
                'measurement_id': result.get('measurement_id'),
                'child': {
                    'child_id': child['id'],
                    'nik_anak': nik_anak,
                    'name': child['name'],
                    'gender': child['gender'],
                    'age_months': age_months
                },
                'measurement': {
                    'height_cm': float(data['height_cm']),
                    'weight_kg': float(data['weight_kg']),
                    'measurement_date': measurement_date
                },
                'classification': result.get('classification', {}),
                'chart_urls': {
                    'growth': f"{base_url}/static/charts/{child['id']}_growth.png",
                    'zscore': f"{base_url}/static/charts/{child['id']}_zscore.png"
                }
            }
        }), 201
        
    except ValueError as e:
        return jsonify({
            'status': 'error',
            'message': f'Format data tidak valid: {str(e)}'
        }), 400
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 5: Klasifikasi Single Measurement (Tanpa Simpan ke DB)
# ============================================================================
@app.route('/api/classify', methods=['POST'])
def classify_measurement():
    """
    Klasifikasi status gizi anak berdasarkan input (tidak disimpan ke database)
    
    POST /api/classify
    Body:
    {
        "gender": "laki-laki",
        "age_months": 12,
        "height_cm": 75.5,
        "weight_kg": 9.2
    }
    
    Response:
    {
        "status": "success",
        "data": {
            "zscore_height": -0.1,
            "zscore_weight": -0.43,
            "classification_height": "Normal",
            "classification_weight": "Normal weight",
            "risk_level": "NONE",
            "warnings": [],
            "recommendations": []
        }
    }
    """
    try:
        data = request.get_json()
        
        # Validasi input
        required_fields = ['gender', 'age_months', 'height_cm', 'weight_kg']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    'status': 'error',
                    'message': f'Missing required field: {field}'
                }), 400
        
        # Klasifikasi
        result = classifier.classify(
            gender=data['gender'],
            age_months=int(data['age_months']),
            height_cm=float(data['height_cm']),
            weight_kg=float(data['weight_kg'])
        )
        
        # Format response sesuai struktur API
        risk_info = result.get('risk_alert', {})
        
        response_data = {
            'zscore_height': result.get('zscore_height', 0),
            'zscore_weight': result.get('zscore_weight', 0),
            'classification_height': result.get('stunting_status', 'Unknown'),
            'classification_weight': result.get('wasting_status', 'Unknown'),
            'risk_level': risk_info.get('risk_level', 'none').upper(),
            'warnings': risk_info.get('risk_messages', []),
            'recommendations': _generate_recommendations(
                result.get('stunting_status', ''),
                result.get('wasting_status', ''),
                risk_info.get('risk_level', 'none')
            )
        }
        
        return jsonify({
            'status': 'success',
            'data': response_data
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 3: Tambah Measurement + Simpan ke Database
# ============================================================================
@app.route('/api/child/measurement', methods=['POST'])
def add_measurement():
    """
    Tambahkan pengukuran baru untuk anak dan simpan ke database
    
    POST /api/child/measurement
    Body:
    {
        "child_id": "CHILD001",
        "name": "Budi Santoso",
        "gender": "laki-laki",
        "birth_date": "2024-01-15",
        "age_months": 11,
        "height_cm": 73.5,
        "weight_kg": 9.2,
        "measurement_date": "2024-12-15"  // optional, default: today
    }
    
    Response:
    {
        "status": "success",
        "data": {
            "measurement_id": 1,
            "child_id": "CHILD001",
            "classification": {...},
            "trend_analysis": {...}  // jika ada > 1 measurement
        }
    }
    """
    try:
        data = request.get_json()
        
        # Validasi input
        required_fields = ['child_id', 'name', 'gender', 'birth_date', 
                          'age_months', 'height_cm', 'weight_kg']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    'status': 'error',
                    'message': f'Missing required field: {field}'
                }), 400
        
        # Tambah measurement
        result = tracker.add_measurement(
            child_id=data['child_id'],
            name=data['name'],
            gender=data['gender'],
            birth_date=data['birth_date'],
            age_months=int(data['age_months']),
            height_cm=float(data['height_cm']),
            weight_kg=float(data['weight_kg']),
            measurement_date=data.get('measurement_date')
        )
        
        if result['status'] == 'error':
            return jsonify(result), 500
        
        return jsonify({
            'status': 'success',
            'data': result
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 4: Get Child History
# ============================================================================
@app.route('/api/child/<child_id>/history', methods=['GET'])
def get_child_history(child_id):
    """
    Ambil seluruh riwayat pengukuran seorang anak
    
    GET /api/child/{child_id}/history
    
    Response:
    {
        "status": "success",
        "child_id": "CHILD001",
        "total_measurements": 3,
        "data": [
            {
                "measurement_date": "2024-10-15",
                "age_months": 9,
                "height_cm": 71.0,
                "weight_kg": 8.5,
                "zscore_height": -1.2,
                "zscore_weight": -0.8,
                "stunting_status": "At Risk",
                "wasting_status": "Normal weight"
            },
            ...
        ]
    }
    """
    try:
        df = tracker.get_child_history(child_id)
        
        if df.empty:
            return jsonify({
                'status': 'error',
                'message': f'No data found for child_id: {child_id}'
            }), 404
        
        # Convert DataFrame to dict
        history = df.to_dict('records')
        
        # Convert date objects to string
        for record in history:
            if 'measurement_date' in record:
                record['measurement_date'] = str(record['measurement_date'])
        
        return jsonify({
            'status': 'success',
            'child_id': child_id,
            'total_measurements': len(history),
            'data': history
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 5: Get Trend Analysis
# ============================================================================
@app.route('/api/child/<child_id>/analysis', methods=['GET'])
def get_trend_analysis(child_id):
    """
    Analisis trend pertumbuhan anak
    
    GET /api/child/{child_id}/analysis
    
    Response:
    {
        "status": "success",
        "data": {
            "height_trend": "Membaik",
            "weight_trend": "Stabil",
            "height_change_per_month": 2.3,
            "weight_change_per_month": 0.15,
            "prediction_next_month": {
                "next_month_age": 12,
                "predicted_height_cm": 75.8,
                "predicted_weight_kg": 9.5
            },
            "warnings": [...]
        }
    }
    """
    try:
        analysis = tracker.analyze_trend(child_id)
        
        return jsonify({
            'status': 'success',
            'data': analysis
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 6: Get All Children
# ============================================================================
@app.route('/api/children', methods=['GET'])
def get_all_children():
    """
    Ambil daftar semua anak yang terdaftar
    
    GET /api/children
    
    Response:
    {
        "status": "success",
        "total": 5,
        "data": [
            {
                "id": "CHILD001",
                "name": "Budi Santoso",
                "gender": "laki-laki",
                "birth_date": "2024-01-15",
                "total_measurements": 3,
                "last_measurement_date": "2024-12-15",
                "latest_risk_level": "NONE"
            },
            ...
        ]
    }
    """
    try:
        children = tracker.get_all_children()
        
        # Convert date objects to string
        for child in children:
            if 'birth_date' in child and child['birth_date']:
                child['birth_date'] = str(child['birth_date'])
            if 'last_measurement_date' in child and child['last_measurement_date']:
                child['last_measurement_date'] = str(child['last_measurement_date'])
        
        return jsonify({
            'status': 'success',
            'total': len(children),
            'data': children
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 7: Get WHO Thresholds
# ============================================================================
@app.route('/api/who/thresholds', methods=['GET'])
def get_who_thresholds():
    """
    Ambil threshold WHO untuk umur dan gender tertentu
    
    GET /api/who/thresholds?gender=laki-laki&age_months=12
    
    Response:
    {
        "status": "success",
        "data": {
            "gender": "laki-laki",
            "age_months": 12,
            "height_params": {
                "L": 1.0,
                "M": 75.7,
                "S": 0.037,
                "SD1neg": 73.4,
                "SD2neg": 71.0
            },
            "weight_params": {
                "L": 0.25,
                "M": 9.6,
                "S": 0.13,
                "SD1neg": 8.6,
                "SD2neg": 7.7
            }
        }
    }
    """
    try:
        gender = request.args.get('gender')
        age_months = request.args.get('age_months')
        
        if not gender or not age_months:
            return jsonify({
                'status': 'error',
                'message': 'Missing required parameters: gender, age_months'
            }), 400
        
        age_months = int(age_months)
        
        # Get height params
        height_params = classifier.get_who_params(gender, age_months, measurement_type='height')
        
        # Get weight params
        weight_params = classifier.get_who_params(gender, age_months, measurement_type='weight')
        
        if height_params is None or weight_params is None:
            return jsonify({
                'status': 'error',
                'message': f'No WHO data available for gender={gender}, age={age_months} months'
            }), 404
        
        return jsonify({
            'status': 'success',
            'data': {
                'gender': gender,
                'age_months': age_months,
                'height_params': height_params,
                'weight_params': weight_params
            }
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 8: Delete Child (Bonus - untuk keperluan testing)
# ============================================================================
@app.route('/api/child/<child_id>', methods=['DELETE'])
def delete_child(child_id):
    """
    Hapus data anak dan semua measurement-nya
    
    DELETE /api/child/{child_id}
    
    Response:
    {
        "status": "success",
        "message": "Child and all measurements deleted"
    }
    """
    try:
        query = "DELETE FROM children WHERE id = %s"
        affected_rows = DatabaseConnection.execute_query(query, (child_id,))
        
        if affected_rows == 0:
            return jsonify({
                'status': 'error',
                'message': f'Child not found: {child_id}'
            }), 404
        
        return jsonify({
            'status': 'success',
            'message': f'Child {child_id} and all measurements deleted'
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# ENDPOINT 9: Generate & Serve Chart (Grafik)
# ============================================================================
@app.route('/api/child/<child_id>/chart', methods=['GET'])
def generate_chart(child_id):
    """
    Generate grafik untuk anak dan return URL
    
    GET /api/child/{child_id}/chart?type=growth
    
    Parameters:
    - type: 'growth' (tinggi/berat) atau 'zscore' (Z-score chart)
    
    Response:
    {
        "status": "success",
        "chart_url": "/static/charts/CHILD001_growth.png",
        "full_url": "http://localhost:5000/static/charts/CHILD001_growth.png"
    }
    """
    try:
        chart_type = request.args.get('type', 'growth')
        
        # Get child history
        history = tracker.get_child_history(child_id)
        
        if history.empty:
            return jsonify({
                'status': 'error',
                'message': f'No data found for child_id: {child_id}'
            }), 404
        
        # Convert Decimal to float for plotting (MySQL returns Decimal)
        numeric_cols = ['height_cm', 'weight_kg', 'zscore_height', 'zscore_weight']
        for col in numeric_cols:
            if col in history.columns:
                history[col] = pd.to_numeric(history[col], errors='coerce')
        
        # Debug: print history columns and data
        print(f"\n=== DEBUG: History for {child_id} ===")
        print(f"Columns: {history.columns.tolist()}")
        print(f"Data shape: {history.shape}")
        print(f"Z-score height values: {history['zscore_height'].tolist()}")
        print(f"Z-score weight values: {history['zscore_weight'].tolist()}")
        print("="*50)
        
        # Get child info
        children = tracker.get_all_children()
        child_info = next((c for c in children if c['id'] == child_id), None)
        child_name = child_info['name'] if child_info else child_id
        child_gender = child_info['gender'] if child_info else 'laki-laki'
        
        # Get trend analysis for prediction
        trend_data = None
        if len(history) > 1:
            try:
                trend_data = tracker.analyze_trend(child_id)
            except:
                pass
        
        # IMPORTANT: Clear all existing figures to prevent overlay
        plt.close('all')
        
        if chart_type == 'growth':
            # Generate Growth Chart with WHO Curves
            fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(14, 12))
            
            # === TINGGI BADAN ===
            # Get WHO data untuk tinggi
            age_range = range(0, 25)  # 0-24 bulan
            who_median_height = []
            who_sd2neg_height = []
            who_sd3neg_height = []
            
            for age in age_range:
                try:
                    params = classifier.get_who_parameters(child_gender, age, 'height')
                    M = params['M']
                    L = params['L']
                    S = params['S']
                    
                    # Calculate SD lines
                    median = M
                    sd2neg = M * ((1 + L * S * (-2)) ** (1/L)) if L != 0 else M * np.exp(S * (-2))
                    sd3neg = M * ((1 + L * S * (-3)) ** (1/L)) if L != 0 else M * np.exp(S * (-3))
                    
                    who_median_height.append(median)
                    who_sd2neg_height.append(sd2neg)
                    who_sd3neg_height.append(sd3neg)
                except:
                    who_median_height.append(None)
                    who_sd2neg_height.append(None)
                    who_sd3neg_height.append(None)
            
            # Plot WHO curves (tinggi)
            ax1.plot(age_range, who_median_height, '--', color='green', alpha=0.7, linewidth=2, label='WHO Median (SD0)')
            ax1.plot(age_range, who_sd2neg_height, '--', color='orange', alpha=0.7, linewidth=2, label='Threshold -2 SD')
            ax1.plot(age_range, who_sd3neg_height, '--', color='red', alpha=0.7, linewidth=2, label='Threshold -3 SD')
            
            # Plot data anak
            ax1.plot(history['age_months'], history['height_cm'], 
                     marker='o', linewidth=3, markersize=10, color='#1f77b4', label=child_name, zorder=5)
            
            # Annotate points
            for idx, row in history.iterrows():
                ax1.annotate(f"{row['height_cm']:.1f}", 
                            (row['age_months'], row['height_cm']),
                            textcoords="offset points", xytext=(0,10), ha='center', fontsize=9)
            
            # Add prediction
            if trend_data and 'prediction_next_month' in trend_data:
                pred = trend_data['prediction_next_month']
                ax1.plot(pred['next_month_age'], pred['predicted_height_cm'], 
                        marker='o', markersize=10, color='orange', label='Prediksi Bulan Depan', zorder=5)
                ax1.annotate(f"{pred['predicted_height_cm']:.1f}", 
                            (pred['next_month_age'], pred['predicted_height_cm']),
                            textcoords="offset points", xytext=(0,10), ha='center', fontsize=9, color='orange')
            
            ax1.set_xlabel('Umur (bulan)', fontsize=12, fontweight='bold')
            ax1.set_ylabel('Tinggi Badan (cm)', fontsize=12, fontweight='bold')
            ax1.set_title(f'Pertumbuhan Tinggi Badan (Height-for-Age)', fontsize=14, fontweight='bold')
            ax1.legend(loc='upper left', fontsize=10)
            ax1.grid(True, alpha=0.3)
            ax1.set_xlim(-0.5, 20)
            
            # === BERAT BADAN ===
            # Get WHO data untuk berat
            who_median_weight = []
            who_sd2neg_weight = []
            who_sd3neg_weight = []
            
            for age in age_range:
                try:
                    params = classifier.get_who_parameters(child_gender, age, 'weight')
                    M = params['M']
                    L = params['L']
                    S = params['S']
                    
                    median = M
                    sd2neg = M * ((1 + L * S * (-2)) ** (1/L)) if L != 0 else M * np.exp(S * (-2))
                    sd3neg = M * ((1 + L * S * (-3)) ** (1/L)) if L != 0 else M * np.exp(S * (-3))
                    
                    who_median_weight.append(median)
                    who_sd2neg_weight.append(sd2neg)
                    who_sd3neg_weight.append(sd3neg)
                except:
                    who_median_weight.append(None)
                    who_sd2neg_weight.append(None)
                    who_sd3neg_weight.append(None)
            
            # Plot WHO curves (berat)
            ax2.plot(age_range, who_median_weight, '--', color='green', alpha=0.7, linewidth=2, label='WHO Median (SD0)')
            ax2.plot(age_range, who_sd2neg_weight, '--', color='orange', alpha=0.7, linewidth=2, label='Threshold -2 SD')
            ax2.plot(age_range, who_sd3neg_weight, '--', color='red', alpha=0.7, linewidth=2, label='Threshold -3 SD')
            
            # Plot data anak
            ax2.plot(history['age_months'], history['weight_kg'], 
                     marker='o', linewidth=3, markersize=10, color='#d62728', label=child_name, zorder=5)
            
            # Annotate points
            for idx, row in history.iterrows():
                ax2.annotate(f"{row['weight_kg']:.1f}", 
                            (row['age_months'], row['weight_kg']),
                            textcoords="offset points", xytext=(0,10), ha='center', fontsize=9)
            
            # Add prediction
            if trend_data and 'prediction_next_month' in trend_data:
                pred = trend_data['prediction_next_month']
                ax2.plot(pred['next_month_age'], pred['predicted_weight_kg'], 
                        marker='o', markersize=10, color='orange', label='Prediksi Bulan Depan', zorder=5)
                ax2.annotate(f"{pred['predicted_weight_kg']:.1f}", 
                            (pred['next_month_age'], pred['predicted_weight_kg']),
                            textcoords="offset points", xytext=(0,10), ha='center', fontsize=9, color='orange')
            
            ax2.set_xlabel('Umur (bulan)', fontsize=12, fontweight='bold')
            ax2.set_ylabel('Berat Badan (kg)', fontsize=12, fontweight='bold')
            ax2.set_title(f'Pertumbuhan Berat Badan (Weight-for-Age)', fontsize=14, fontweight='bold')
            ax2.legend(loc='upper left', fontsize=10)
            ax2.grid(True, alpha=0.3)
            ax2.set_xlim(-0.5, 20)
            
            plt.suptitle(f'Grafik Perkembangan: {child_name} ({child_gender.title()})', 
                        fontsize=16, fontweight='bold', y=0.995)
            plt.tight_layout()
            filename = f'{child_id}_growth.png'
            
        else:  # zscore
            # Generate Z-Score Chart with Background Zones
            fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(14, 12))
            
            # === Z-SCORE TINGGI ===
            # Background zones
            ax1.axhspan(2, 4, facecolor='lightblue', alpha=0.3, label='Overweight')
            ax1.axhspan(-1, 2, facecolor='lightgreen', alpha=0.4, label='Normal')
            ax1.axhspan(-2, -1, facecolor='yellow', alpha=0.3, label='Stunted')
            ax1.axhspan(-3, -2, facecolor='lightsalmon', alpha=0.4, label='Severely Stunted')
            ax1.axhspan(-4, -3, facecolor='red', alpha=0.2)
            
            # Threshold lines
            ax1.axhline(y=0, color='green', linestyle='-', linewidth=2, label='Median (0)', alpha=0.8)
            ax1.axhline(y=-1, color='yellow', linestyle='--', linewidth=2, label='Threshold -1 SD', alpha=0.7)
            ax1.axhline(y=-2, color='orange', linestyle='--', linewidth=2, label='Threshold -2 SD', alpha=0.7)
            ax1.axhline(y=-3, color='red', linestyle='--', linewidth=2, label='Threshold -3 SD', alpha=0.7)
            ax1.axhline(y=2, color='purple', linestyle='--', linewidth=1, alpha=0.5, label='Threshold +2 SD')
            
            # Filter valid Z-score data (not None/NaN)
            valid_height = history[history['zscore_height'].notna()].copy()
            
            # Debug print
            print(f"\n=== Z-SCORE HEIGHT DEBUG ===")
            print(f"Total rows: {len(history)}")
            print(f"Valid rows: {len(valid_height)}")
            print(f"Ages: {valid_height['age_months'].tolist()}")
            print(f"Z-scores: {valid_height['zscore_height'].tolist()}")
            print(f"Data types: {valid_height['zscore_height'].dtype}")
            print("="*50)
            
            # Plot data only if we have valid z-scores
            if not valid_height.empty:
                print(f">>> Plotting {len(valid_height)} points for HEIGHT")
                ax1.plot(valid_height['age_months'], valid_height['zscore_height'], 
                         marker='o', linewidth=3, markersize=10, color='#1f77b4', label='Z-Score TB', zorder=5)
                
                # Annotate points
                for idx, row in valid_height.iterrows():
                    ax1.annotate(f"Z={row['zscore_height']:.2f}", 
                                (row['age_months'], row['zscore_height']),
                                textcoords="offset points", xytext=(0,10), ha='center', fontsize=9)
            else:
                print(">>> NO VALID HEIGHT DATA - showing warning text")
                # Add text if no data
                ax1.text(6.5, 0, 'Belum ada data Z-score', 
                        ha='center', va='center', fontsize=14, color='red', alpha=0.5)
            
            ax1.set_xlabel('Umur (bulan)', fontsize=12, fontweight='bold')
            ax1.set_ylabel('Z-Score Tinggi Badan', fontsize=12, fontweight='bold')
            ax1.set_title('Z-Score Tinggi Badan (Height-for-Age)', fontsize=14, fontweight='bold')
            ax1.legend(loc='lower left', fontsize=8, ncol=3, framealpha=0.9)
            ax1.grid(True, alpha=0.3, zorder=0)
            # Dynamic xlim and ylim based on actual data
            max_age = history['age_months'].max() if not history.empty else 24
            ax1.set_xlim(-0.5, max(max_age + 2, 13))
            
            # Dynamic ylim untuk accommodate semua data
            if not valid_height.empty:
                min_z = min(valid_height['zscore_height'].min(), -4)
                max_z = max(valid_height['zscore_height'].max(), 4)
                ax1.set_ylim(min_z - 0.5, max_z + 0.5)
            else:
                ax1.set_ylim(-4, 4)
            
            # === Z-SCORE BERAT ===
            # Background zones
            ax2.axhspan(2, 4, facecolor='yellow', alpha=0.3, label='Risk of Overweight')
            ax2.axhspan(1, 2, facecolor='lightyellow', alpha=0.3, label='Overweight')
            ax2.axhspan(-1, 1, facecolor='lightgreen', alpha=0.4, label='Normal')
            ax2.axhspan(-2, -1, facecolor='lightsalmon', alpha=0.3, label='Underweight')
            ax2.axhspan(-3, -2, facecolor='red', alpha=0.3, label='Severely Underweight')
            ax2.axhspan(-4, -3, facecolor='darkred', alpha=0.2)
            
            # Threshold lines
            ax2.axhline(y=0, color='green', linestyle='-', linewidth=2, label='Median (0)', alpha=0.8)
            ax2.axhline(y=-1, color='yellow', linestyle='--', linewidth=2, label='Threshold -1 SD', alpha=0.7)
            ax2.axhline(y=-2, color='orange', linestyle='--', linewidth=2, label='Threshold -2 SD', alpha=0.7)
            ax2.axhline(y=-3, color='red', linestyle='--', linewidth=2, label='Threshold -3 SD', alpha=0.7)
            ax2.axhline(y=1, color='goldenrod', linestyle='--', linewidth=1, alpha=0.5, label='Threshold +1 SD')
            ax2.axhline(y=2, color='purple', linestyle='--', linewidth=1, alpha=0.5, label='Threshold +2 SD')
            
            # Filter valid Z-score data (not None/NaN)
            valid_weight = history[history['zscore_weight'].notna()].copy()
            
            # Debug print
            print(f"\n=== Z-SCORE WEIGHT DEBUG ===")
            print(f"Total rows: {len(history)}")
            print(f"Valid rows: {len(valid_weight)}")
            print(f"Ages: {valid_weight['age_months'].tolist()}")
            print(f"Z-scores: {valid_weight['zscore_weight'].tolist()}")
            print(f"Data types: {valid_weight['zscore_weight'].dtype}")
            print("="*50)
            
            # Plot data only if we have valid z-scores
            if not valid_weight.empty:
                print(f">>> Plotting {len(valid_weight)} points for WEIGHT")
                ax2.plot(valid_weight['age_months'], valid_weight['zscore_weight'], 
                         marker='o', linewidth=3, markersize=10, color='#d62728', label='Z-Score BB', zorder=5)
                
                # Annotate points
                for idx, row in valid_weight.iterrows():
                    ax2.annotate(f"Z={row['zscore_weight']:.2f}", 
                                (row['age_months'], row['zscore_weight']),
                                textcoords="offset points", xytext=(0,10), ha='center', fontsize=9)
            else:
                print(">>> NO VALID WEIGHT DATA - showing warning text")
                # Add text if no data
                ax2.text(6.5, 0, 'Belum ada data Z-score', 
                        ha='center', va='center', fontsize=14, color='red', alpha=0.5)
            
            ax2.set_xlabel('Umur (bulan)', fontsize=12, fontweight='bold')
            ax2.set_ylabel('Z-Score Berat Badan', fontsize=12, fontweight='bold')
            ax2.set_title('Z-Score Berat Badan (Weight-for-Age)', fontsize=14, fontweight='bold')
            ax2.legend(loc='lower left', fontsize=8, ncol=3, framealpha=0.9)
            ax2.grid(True, alpha=0.3, zorder=0)
            # Dynamic xlim and ylim based on actual data
            ax2.set_xlim(-0.5, max(max_age + 2, 13))
            
            # Dynamic ylim untuk accommodate semua data
            if not valid_weight.empty:
                min_z = min(valid_weight['zscore_weight'].min(), -4)
                max_z = max(valid_weight['zscore_weight'].max(), 4)
                ax2.set_ylim(min_z - 0.5, max_z + 0.5)
            else:
                ax2.set_ylim(-4, 4)
            
            plt.tight_layout()
            filename = f'{child_id}_zscore.png'
        
        # Save chart with absolute path
        filepath = os.path.join(CHARTS_DIR, filename)
        plt.savefig(filepath, dpi=150, bbox_inches='tight')
        plt.close()
        
        # Verify file was created
        if not os.path.exists(filepath):
            return jsonify({
                'status': 'error',
                'message': f'Failed to save chart: {filepath}'
            }), 500
        
        # Build URLs
        chart_url = f'/static/charts/{filename}'
        full_url = f"{request.host_url.rstrip('/')}{chart_url}"
        
        return jsonify({
            'status': 'success',
            'chart_url': chart_url,
            'full_url': full_url,
            'filename': filename,
            'file_path': filepath  # Debug info
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


# ============================================================================
# Serve Static Files (Grafik PNG)
# ============================================================================
@app.route('/static/charts/<path:filename>')
def serve_chart(filename):
    """
    Serve grafik PNG dari folder static/charts
    
    GET /static/charts/CHILD001_growth.png
    """
    return send_from_directory(CHARTS_DIR, filename)


# ============================================================================
# Run Server
# ============================================================================
if __name__ == '__main__':
    # Development mode
    app.run(
        host='0.0.0.0',  # Accessible dari network (mobile bisa akses)
        port=5000,
        debug=True
    )
