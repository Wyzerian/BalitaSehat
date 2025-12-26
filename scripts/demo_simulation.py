"""
Demo Simulation - BalitaSehat
Simulasi lengkap tanpa mobile app untuk testing

Features:
- Input data anak
- Generate klasifikasi WHO
- Generate grafik (PNG)
- Simpan ke database MySQL
- Display hasil
"""

import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from app.who_classifier import WHOClassifier
from app.growth_tracker_mysql import GrowthTrackerMySQL
from app.database import DatabaseConnection
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use('Agg')  # Backend untuk server (no display)
from datetime import datetime, timedelta
import pandas as pd

# Inisialisasi
print("="*60)
print("🚀 BALITASEHAT DEMO SIMULATION")
print("="*60)

# Test database connection
print("\n1. Testing database connection...")
if DatabaseConnection.test_connection():
    print("   ✅ Database connected!")
else:
    print("   ❌ Database connection failed!")
    print("   Check config/db_config.py")
    sys.exit(1)

# Load WHO Classifier
print("\n2. Loading WHO Classifier...")
classifier = WHOClassifier(
    who_boys_height_path='data/WHO Indicators Boys 2 years_Tinggi.csv',
    who_girls_height_path='data/WHO Indicators Girls 2 years_Tinggi.csv',
    who_boys_weight_path='data/WHO Indicators Boys 2 years_Berat.csv',
    who_girls_weight_path='data/WHO Indicators Girls 2 years_Berat.csv'
)
print("   ✅ WHO Classifier loaded!")

# Inisialisasi Growth Tracker
tracker = GrowthTrackerMySQL(classifier)

print("\n" + "="*60)
print("📊 SIMULASI: Data Budi (3 Bulan Berturut-turut)")
print("="*60)

# Data simulasi untuk 3 bulan
measurements = [
    {
        'month': 1,
        'child_id': 'DEMO_BUDI',
        'name': 'Budi Santoso',
        'gender': 'laki-laki',
        'birth_date': '2024-01-15',
        'age_months': 16,
        'height_cm': 85.0,
        'weight_kg': 13.0,
        'date': '2024-12-15'
    },
    {
        'month': 2,
        'child_id': 'DEMO_BUDI',
        'name': 'Budi Santoso',
        'gender': 'laki-laki',
        'birth_date': '2024-01-15',
        'age_months': 17,
        'height_cm': 86.5,
        'weight_kg': 13.5,
        'date': '2025-01-15'
    },
    {
        'month': 3,
        'child_id': 'DEMO_BUDI',
        'name': 'Budi Santoso',
        'gender': 'laki-laki',
        'birth_date': '2024-01-15',
        'age_months': 18,
        'height_cm': 88.0,
        'weight_kg': 14.0,
        'date': '2025-02-15'
    }
]

# Proses setiap pengukuran
results = []
for data in measurements:
    print(f"\n{'='*60}")
    print(f"📅 BULAN {data['month']}: Umur {data['age_months']} Bulan")
    print(f"{'='*60}")
    print(f"Tinggi: {data['height_cm']} cm")
    print(f"Berat: {data['weight_kg']} kg")
    print(f"Tanggal: {data['date']}")
    
    # Tambah measurement ke database
    try:
        result = tracker.add_measurement(
            child_id=data['child_id'],
            name=data['name'],
            gender=data['gender'],
            birth_date=data['birth_date'],
            age_months=data['age_months'],
            height_cm=data['height_cm'],
            weight_kg=data['weight_kg'],
            measurement_date=data['date']
        )
        
        if result.get('status') == 'success':
            print(f"\n✅ Data berhasil disimpan ke database!")
            print(f"\n📊 HASIL KLASIFIKASI:")
            print(f"   Z-score Tinggi: {result.get('zscore_height', 'N/A')}")
            print(f"   Z-score Berat: {result.get('zscore_weight', 'N/A')}")
            print(f"   Status Tinggi: {result.get('classification_height', 'N/A')}")
            print(f"   Status Berat: {result.get('classification_weight', 'N/A')}")
            print(f"   Risk Level: {result.get('risk_level', 'N/A')}")
            
            results.append(result)
        else:
            print(f"\n❌ Error: {result.get('message', 'Unknown error')}")
            
    except Exception as e:
        print(f"\n❌ Exception: {str(e)}")
        
        if result.get('warnings'):
            print(f"\n⚠️ PERINGATAN:")
            for warning in result['warnings']:
                print(f"   - {warning}")
        
        if result.get('recommendations'):
            print(f"\n💡 REKOMENDASI:")
            for rec in result['recommendations']:
                print(f"   - {rec}")
        
        # Trend analysis (jika ada)
        if 'trend_analysis' in result:
            trend = result['trend_analysis']
            print(f"\n📈 TREND ANALYSIS:")
            print(f"   Trend Tinggi: {trend.get('height_trend', 'N/A')}")
            print(f"   Trend Berat: {trend.get('weight_trend', 'N/A')}")
            print(f"   Perubahan Tinggi: {trend.get('height_change_per_month', 0):.2f} cm/bulan")
            print(f"   Perubahan Berat: {trend.get('weight_change_per_month', 0):.2f} kg/bulan")
            
            if trend.get('prediction_next_month'):
                pred = trend['prediction_next_month']
                print(f"\n🔮 PREDIKSI BULAN DEPAN ({pred['next_month_age']} bulan):")
                print(f"   Tinggi: {pred['predicted_height_cm']:.2f} cm")
                print(f"   Berat: {pred['predicted_weight_kg']:.2f} kg")
        
        results.append(result)
    else:
        print(f"❌ Error: {result.get('message')}")

# Generate Grafik
print(f"\n{'='*60}")
print(f"📊 GENERATE GRAFIK")
print(f"{'='*60}")

# Get all measurements dari database
history = tracker.get_child_history('DEMO_BUDI')

if not history.empty:
    # Buat folder charts jika belum ada
    os.makedirs('static/charts', exist_ok=True)
    
    # 1. Grafik Pertumbuhan (Tinggi & Berat)
    print("\n1. Generating Growth Chart...")
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
    
    # Plot Tinggi
    ax1.plot(history['age_months'], history['height_cm'], 
             marker='o', linewidth=2, markersize=8, color='blue')
    ax1.set_xlabel('Umur (bulan)', fontsize=12)
    ax1.set_ylabel('Tinggi Badan (cm)', fontsize=12)
    ax1.set_title('Grafik Pertumbuhan Tinggi Badan - Budi', fontsize=14, fontweight='bold')
    ax1.grid(True, alpha=0.3)
    
    # Annotate points
    for idx, row in history.iterrows():
        ax1.annotate(f"{row['height_cm']:.1f} cm", 
                    (row['age_months'], row['height_cm']),
                    textcoords="offset points", xytext=(0,10), ha='center')
    
    # Plot Berat
    ax2.plot(history['age_months'], history['weight_kg'], 
             marker='o', linewidth=2, markersize=8, color='red')
    ax2.set_xlabel('Umur (bulan)', fontsize=12)
    ax2.set_ylabel('Berat Badan (kg)', fontsize=12)
    ax2.set_title('Grafik Pertumbuhan Berat Badan - Budi', fontsize=14, fontweight='bold')
    ax2.grid(True, alpha=0.3)
    
    # Annotate points
    for idx, row in history.iterrows():
        ax2.annotate(f"{row['weight_kg']:.1f} kg", 
                    (row['age_months'], row['weight_kg']),
                    textcoords="offset points", xytext=(0,10), ha='center')
    
    plt.tight_layout()
    growth_chart_path = 'static/charts/DEMO_BUDI_growth.png'
    plt.savefig(growth_chart_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"   ✅ Saved: {growth_chart_path}")
    
    # 2. Grafik Z-Score
    print("\n2. Generating Z-Score Chart...")
    
    # Check if zscore data exists
    if 'zscore_height' not in history.columns or history['zscore_height'].isna().all():
        print("   ⚠️ Z-Score data not available, skipping Z-Score chart")
    else:
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
        
        # Plot Z-Score Tinggi
        ax1.plot(history['age_months'], history['zscore_height'], 
                 marker='o', linewidth=2, markersize=8, color='blue')
        ax1.axhline(y=0, color='green', linestyle='--', label='Median (0)', alpha=0.7)
        ax1.axhline(y=-1, color='yellow', linestyle='--', label='Early Warning (-1 SD)', alpha=0.7)
        ax1.axhline(y=-2, color='orange', linestyle='--', label='Stunted (-2 SD)', alpha=0.7)
        ax1.axhline(y=-3, color='red', linestyle='--', label='Severely Stunted (-3 SD)', alpha=0.7)
        ax1.set_xlabel('Umur (bulan)', fontsize=12)
        ax1.set_ylabel('Z-Score Tinggi Badan', fontsize=12)
        ax1.set_title('Grafik Z-Score Tinggi Badan - Budi', fontsize=14, fontweight='bold')
        ax1.legend(loc='best', fontsize=9)
        ax1.grid(True, alpha=0.3)
        
        # Annotate points
        for idx, row in history.iterrows():
            if pd.notna(row['zscore_height']):
                ax1.annotate(f"Z={row['zscore_height']:.2f}", 
                            (row['age_months'], row['zscore_height']),
                            textcoords="offset points", xytext=(0,10), ha='center')
        
        # Plot Z-Score Berat
        ax2.plot(history['age_months'], history['zscore_weight'], 
                 marker='o', linewidth=2, markersize=8, color='red')
        ax2.axhline(y=0, color='green', linestyle='--', label='Median (0)', alpha=0.7)
        ax2.axhline(y=-1, color='yellow', linestyle='--', label='Early Warning (-1 SD)', alpha=0.7)
        ax2.axhline(y=-2, color='orange', linestyle='--', label='Underweight (-2 SD)', alpha=0.7)
        ax2.axhline(y=-3, color='red', linestyle='--', label='Severely Underweight (-3 SD)', alpha=0.7)
        ax2.set_xlabel('Umur (bulan)', fontsize=12)
        ax2.set_ylabel('Z-Score Berat Badan', fontsize=12)
        ax2.set_title('Grafik Z-Score Berat Badan - Budi', fontsize=14, fontweight='bold')
        ax2.legend(loc='best', fontsize=9)
        ax2.grid(True, alpha=0.3)
        
        # Annotate points
        for idx, row in history.iterrows():
            if pd.notna(row['zscore_weight']):
                ax2.annotate(f"Z={row['zscore_weight']:.2f}", 
                            (row['age_months'], row['zscore_weight']),
                            textcoords="offset points", xytext=(0,10), ha='center')
        
        plt.tight_layout()
        zscore_chart_path = 'static/charts/DEMO_BUDI_zscore.png'
        plt.savefig(zscore_chart_path, dpi=150, bbox_inches='tight')
        plt.close()
        print(f"   ✅ Saved: {zscore_chart_path}")
    
    print(f"\n📁 Grafik tersimpan di folder: static/charts/")
    print(f"   - {growth_chart_path}")
    print(f"   - {zscore_chart_path}")

# Summary
print(f"\n{'='*60}")
print(f"✅ SIMULASI SELESAI!")
print(f"{'='*60}")
print(f"\n📊 SUMMARY:")
print(f"   Total measurements: {len(results)}")
print(f"   Child ID: DEMO_BUDI")
print(f"   Grafik tersimpan: static/charts/")
print(f"\n🗄️ DATA TERSIMPAN DI DATABASE:")
print(f"   - Table: children")
print(f"   - Table: measurements")
print(f"   - Table: classifications")
print(f"   - Table: trend_analysis")

print(f"\n🔍 Cek data di database:")
print(f"   mysql> SELECT * FROM children WHERE id='DEMO_BUDI';")
print(f"   mysql> SELECT * FROM measurements WHERE child_id='DEMO_BUDI';")

print(f"\n🌐 Untuk lihat via API:")
print(f"   GET http://localhost:5000/api/child/DEMO_BUDI/history")
print(f"   GET http://localhost:5000/api/child/DEMO_BUDI/analysis")

print(f"\n📸 Untuk lihat grafik:")
print(f"   http://localhost:5000/static/charts/DEMO_BUDI_growth.png")
print(f"   http://localhost:5000/static/charts/DEMO_BUDI_zscore.png")

print(f"\n{'='*60}")
print(f"🎉 Demo simulation completed successfully!")
print(f"{'='*60}\n")
