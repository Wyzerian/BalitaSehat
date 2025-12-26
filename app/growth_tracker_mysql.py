# Growth Tracker with MySQL Integration
# Updated version untuk konek ke database MySQL

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from app.who_classifier import WHOClassifier
from app.database import DatabaseConnection

class GrowthTrackerMySQL:
    """
    Sistem tracking pertumbuhan anak dengan MySQL database
    """
    
    def __init__(self, classifier):
        """
        Parameters:
        -----------
        classifier : WHOClassifier
            Instance dari WHO classifier
        """
        self.classifier = classifier
    
    
    def add_child(self, child_id, nik_anak, name, parent_name, address, gender, birth_date):
        """
        Tambahkan data anak baru ke database
        
        Parameters:
        -----------
        child_id : str
            ID unik anak
        nik_anak : str
            NIK anak (16 digit)
        name : str
            Nama anak
        parent_name : str
            Nama orang tua/wali
        address : str
            Alamat lengkap
        gender : str
            'laki-laki' atau 'perempuan'
        birth_date : str or date
            Tanggal lahir (format: 'YYYY-MM-DD')
        
        Returns:
        --------
        dict : Status berhasil atau error
        """
        try:
            query = """
                INSERT INTO children (id, nik_anak, name, parent_name, address, gender, birth_date) 
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE 
                    name = VALUES(name),
                    parent_name = VALUES(parent_name),
                    address = VALUES(address),
                    gender = VALUES(gender),
                    birth_date = VALUES(birth_date)
            """
            DatabaseConnection.execute_query(query, (child_id, nik_anak, name, parent_name, address, gender, birth_date))
            return {'status': 'success', 'message': 'Data anak berhasil disimpan', 'child_id': child_id}
        except Exception as e:
            return {'status': 'error', 'message': str(e)}
    
    
    def get_child_by_nik(self, nik_anak):
        """
        Cari data anak berdasarkan NIK
        
        Parameters:
        -----------
        nik_anak : str
            NIK anak (16 digit)
        
        Returns:
        --------
        dict or None : Data anak jika ditemukan
        """
        try:
            query = """
                SELECT id, nik_anak, name, parent_name, address, gender, birth_date, created_at
                FROM children 
                WHERE nik_anak = %s
            """
            print(f"[DEBUG] Searching for NIK: {nik_anak}")
            result = DatabaseConnection.fetch_one(query, (nik_anak,))
            print(f"[DEBUG] Query result: {result}")
            
            if result:
                # fetch_one returns dict, not tuple
                child_data = {
                    'id': result['id'],
                    'nik_anak': result['nik_anak'],
                    'name': result['name'],
                    'parent_name': result.get('parent_name'),
                    'address': result.get('address'),
                    'gender': result['gender'],
                    'birth_date': result['birth_date'].strftime('%Y-%m-%d') if result['birth_date'] else None,
                    'created_at': result['created_at'].strftime('%Y-%m-%d %H:%M:%S') if result['created_at'] else None
                }
                print(f"[DEBUG] Returning child: {child_data}")
                return child_data
            
            print("[DEBUG] No result found")
            return None
        except Exception as e:
            print(f"Error getting child by NIK: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    
    def add_measurement(self, child_id, name, gender, birth_date, age_months, 
                       height_cm, weight_kg, measurement_date=None):
        """
        Tambahkan data pengukuran baru untuk seorang anak
        
        Parameters:
        -----------
        child_id : str
            ID unik anak
        name : str
            Nama anak
        gender : str
            'laki-laki' atau 'perempuan'
        birth_date : str or date
            Tanggal lahir
        age_months : int
            Umur dalam bulan
        height_cm : float
            Tinggi badan
        weight_kg : float
            Berat badan
        measurement_date : str or date
            Tanggal pengukuran (default: hari ini)
        
        Returns:
        --------
        dict : Hasil klasifikasi + info tambahan
        """
        if measurement_date is None:
            measurement_date = datetime.now().date()
        
        try:
            # 1. Simpan/update data anak (get NIK from database first)
            # Check if child exists and get NIK
            query_get_nik = "SELECT nik_anak FROM children WHERE id = %s"
            existing = DatabaseConnection.fetch_one(query_get_nik, (child_id,))
            
            if existing:
                # Child exists, just update if needed (skip add_child for existing)
                pass
            else:
                # New child - this shouldn't happen in normal flow
                # as measurement should only be added after registration
                return {
                    'status': 'error',
                    'message': f'Child ID {child_id} tidak ditemukan. Daftarkan anak terlebih dahulu.'
                }
            
            # 2. Klasifikasi menggunakan WHO
            result = self.classifier.classify(gender, age_months, height_cm, weight_kg)
            
            if result['status'] == 'error':
                return result
            
            # 3. Simpan data measurement
            query_measurement = """
                INSERT INTO measurements 
                (child_id, measurement_date, age_months, height_cm, weight_kg)
                VALUES (%s, %s, %s, %s, %s)
            """
            measurement_id = DatabaseConnection.execute_query(
                query_measurement,
                (child_id, measurement_date, age_months, height_cm, weight_kg)
            )
            
            # 4. Simpan hasil klasifikasi
            # Extract warnings and recommendations from risk_alert
            risk_alert = result.get('risk_alert', {})
            warnings = "\n".join(risk_alert.get('warnings', []))
            recommendations = "\n".join(risk_alert.get('recommendations', []))
            risk_level = risk_alert.get('risk_level', 'NONE')
            
            query_classification = """
                INSERT INTO classifications 
                (measurement_id, child_id, height_zscore, weight_zscore, 
                 stunting_status, wasting_status, risk_level, warnings, recommendations)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            DatabaseConnection.execute_query(
                query_classification,
                (measurement_id, child_id, result['zscore_height'], result['zscore_weight'],
                 result['stunting_status'], result['wasting_status'],
                 risk_level, warnings, recommendations)
            )
            
            # 5. Add standardized keys for API response
            result['classification_height'] = result['stunting_status']
            result['classification_weight'] = result['wasting_status']
            result['risk_level'] = risk_level
            result['warnings'] = risk_alert.get('warnings', [])
            result['recommendations'] = risk_alert.get('recommendations', [])
            
            # 5. Add standardized keys for API response
            result['classification_height'] = result['stunting_status']
            result['classification_weight'] = result['wasting_status']
            result['risk_level'] = risk_level
            result['warnings'] = risk_alert.get('warnings', [])
            result['recommendations'] = risk_alert.get('recommendations', [])
            
            # 6. Tambahkan info measurement_id ke result
            result['measurement_id'] = measurement_id
            result['child_id'] = child_id
            result['name'] = name
            result['measurement_date'] = str(measurement_date)
            
            # 7. Analisis trend jika ada data sebelumnya
            history = self.get_child_history(child_id)
            if len(history) > 1:
                trend_analysis = self.analyze_trend(child_id)
                result['trend_analysis'] = trend_analysis
            
            return result
            
        except Exception as e:
            return {'status': 'error', 'message': str(e)}
    
    
    def get_child_history(self, child_id):
        """
        Ambil seluruh riwayat pengukuran seorang anak dari database
        
        Parameters:
        -----------
        child_id : str
            ID anak
        
        Returns:
        --------
        DataFrame : Riwayat pengukuran
        """
        try:
            query = """
                SELECT 
                    m.id as measurement_id,
                    m.measurement_date,
                    m.age_months,
                    m.height_cm,
                    m.weight_kg,
                    cl.height_zscore as zscore_height,
                    cl.weight_zscore as zscore_weight,
                    cl.stunting_status as classification_height,
                    cl.wasting_status as classification_weight,
                    cl.risk_level,
                    cl.warnings,
                    cl.recommendations
                FROM measurements m
                LEFT JOIN classifications cl ON m.id = cl.measurement_id
                WHERE m.child_id = %s
                ORDER BY m.age_months, m.measurement_date
            """
            results = DatabaseConnection.fetch_all(query, (child_id,))
            
            if not results:
                return pd.DataFrame()
            
            df = pd.DataFrame(results)
            
            # Convert warnings/recommendations dari string ke list
            if not df.empty:
                df['warnings'] = df['warnings'].apply(lambda x: x.split('\n') if x else [])
                df['recommendations'] = df['recommendations'].apply(lambda x: x.split('\n') if x else [])
            
            return df
            
        except Exception as e:
            print(f"Error getting child history: {e}")
            return pd.DataFrame()
    
    
    def get_all_children(self):
        """
        Ambil daftar semua anak yang terdaftar
        
        Returns:
        --------
        list : Daftar anak dengan info terakhir
        """
        try:
            query = """
                SELECT 
                    c.id,
                    c.name,
                    c.gender,
                    c.birth_date,
                    COUNT(m.id) as total_measurements,
                    MAX(m.measurement_date) as last_measurement_date,
                    (SELECT cl.risk_level 
                     FROM measurements m2 
                     LEFT JOIN classifications cl ON m2.id = cl.measurement_id
                     WHERE m2.child_id = c.id 
                     ORDER BY m2.measurement_date DESC 
                     LIMIT 1) as latest_risk_level
                FROM children c
                LEFT JOIN measurements m ON c.id = m.child_id
                GROUP BY c.id, c.name, c.gender, c.birth_date
                ORDER BY c.name
            """
            results = DatabaseConnection.fetch_all(query)
            return results
            
        except Exception as e:
            print(f"Error getting all children: {e}")
            return []
    
    
    def analyze_trend(self, child_id):
        """
        Analisis trend pertumbuhan anak
        
        Deteksi:
        - Apakah Z-score menurun dari waktu ke waktu?
        - Apakah mendekati threshold berbahaya?
        - Prediksi bulan depan
        
        Parameters:
        -----------
        child_id : str
            ID anak
        
        Returns:
        --------
        dict : Hasil analisis trend
        """
        df = self.get_child_history(child_id)
        
        if df.empty or len(df) < 2:
            return {
                'status': 'insufficient_data',
                'message': 'Butuh minimal 2 pengukuran untuk analisis trend'
            }
        
        # Ambil 2 pengukuran terakhir
        latest = df.iloc[-1]
        previous = df.iloc[-2]
        
        # Hitung perubahan Z-score
        height_zscore_change = latest['zscore_height'] - previous['zscore_height']
        weight_zscore_change = latest['zscore_weight'] - previous['zscore_weight']
        
        # Deteksi trend
        height_trend = self._interpret_trend(height_zscore_change, 'height')
        weight_trend = self._interpret_trend(weight_zscore_change, 'weight')
        
        # Hitung perubahan per bulan
        months_diff = latest['age_months'] - previous['age_months']
        if months_diff == 0:
            months_diff = 1
        
        height_change_per_month = (latest['height_cm'] - previous['height_cm']) / months_diff
        weight_change_per_month = (latest['weight_kg'] - previous['weight_kg']) / months_diff
        
        # Prediksi bulan depan
        prediction = self._predict_next_month(df)
        
        # Generate warnings
        warnings = []
        
        if height_zscore_change < -0.5:
            warnings.append("⚠️ Z-score tinggi menurun signifikan! Perlu perhatian khusus.")
        
        if weight_zscore_change < -0.5:
            warnings.append("⚠️ Z-score berat menurun signifikan! Periksa asupan nutrisi.")
        
        if latest['zscore_height'] < -1 and height_zscore_change < 0:
            warnings.append("🚨 Tinggi sudah di bawah normal dan masih menurun!")
        
        if latest['zscore_weight'] < -1 and weight_zscore_change < 0:
            warnings.append("🚨 Berat sudah di bawah normal dan masih menurun!")
        
        # Simpan trend analysis ke database (opsional)
        self._save_trend_analysis(child_id, latest, height_trend, weight_trend, 
                                  height_change_per_month, weight_change_per_month,
                                  prediction, warnings)
        
        return {
            'status': 'success',
            'child_id': child_id,
            'total_measurements': len(df),
            'latest_age_months': int(latest['age_months']),
            'height_trend': height_trend,
            'weight_trend': weight_trend,
            'height_change_per_month': round(height_change_per_month, 2),
            'weight_change_per_month': round(weight_change_per_month, 2),
            'current_height_zscore': round(latest['zscore_height'], 2),
            'current_weight_zscore': round(latest['zscore_weight'], 2),
            'prediction_next_month': prediction,
            'warnings': warnings
        }
    
    
    def _interpret_trend(self, zscore_change, measurement_type):
        """
        Interpretasi perubahan Z-score
        """
        if zscore_change > 0.3:
            return "Membaik"
        elif zscore_change < -0.3:
            return "Menurun"
        else:
            return "Stabil"
    
    
    def _predict_next_month(self, df):
        """
        Prediksi pengukuran bulan depan (simple linear extrapolation)
        """
        if len(df) < 2:
            return None
        
        # Ambil 3 data terakhir (atau semua jika < 3)
        recent = df.tail(3)
        
        # Simple linear regression
        ages = recent['age_months'].values
        heights = recent['height_cm'].values
        weights = recent['weight_kg'].values
        
        # Convert Decimal to float if needed
        ages = [float(x) for x in ages]
        heights = [float(x) for x in heights]
        weights = [float(x) for x in weights]
        
        # Predict next month
        next_age = ages[-1] + 1
        
        # Linear extrapolation
        if len(ages) >= 2:
            age_diff = ages[-1] - ages[0]
            
            # Avoid division by zero
            if age_diff > 0:
                height_slope = (heights[-1] - heights[0]) / age_diff
                weight_slope = (weights[-1] - weights[0]) / age_diff
                
                predicted_height = heights[-1] + height_slope
                predicted_weight = weights[-1] + weight_slope
            else:
                # Semua measurement di umur yang sama, gunakan rata-rata
                predicted_height = sum(heights) / len(heights)
                predicted_weight = sum(weights) / len(weights)
        else:
            predicted_height = heights[-1]
            predicted_weight = weights[-1]
        
        return {
            'next_month_age': int(next_age),
            'predicted_height_cm': round(predicted_height, 2),
            'predicted_weight_kg': round(predicted_weight, 2)
        }
    
    
    def _save_trend_analysis(self, child_id, latest_measurement, height_trend, weight_trend,
                            height_change, weight_change, prediction, warnings):
        """
        Simpan hasil analisis trend ke database (opsional)
        """
        try:
            warnings_text = "\n".join(warnings)
            
            query = """
                INSERT INTO trend_analysis 
                (child_id, analysis_date, height_trend, weight_trend,
                 height_change_per_month, weight_change_per_month,
                 predicted_height_next_month, predicted_weight_next_month, warnings)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            DatabaseConnection.execute_query(
                query,
                (child_id, 
                 latest_measurement['measurement_date'],
                 height_trend,
                 weight_trend,
                 height_change,
                 weight_change,
                 prediction['predicted_height_cm'] if prediction else None,
                 prediction['predicted_weight_kg'] if prediction else None,
                 warnings_text)
            )
        except Exception as e:
            # Jika gagal simpan trend, tidak masalah (optional feature)
            print(f"Note: Could not save trend analysis: {e}")
