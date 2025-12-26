"""
WHO-based Growth Classification System
Klasifikasi Status Gizi Anak berdasarkan Standar WHO
"""

import pandas as pd
import numpy as np


class WHOClassifier:
    """
    Klasifikasi status gizi anak menggunakan standar WHO
    berdasarkan Z-score untuk Tinggi Badan dan Berat Badan
    """
    
    def __init__(self, who_boys_height_path, who_girls_height_path, 
                 who_boys_weight_path, who_girls_weight_path):
        """
        Inisialisasi dengan data WHO untuk laki-laki dan perempuan
        
        Parameters:
        -----------
        who_boys_height_path : str
            Path ke file WHO standar tinggi badan laki-laki
        who_girls_height_path : str
            Path ke file WHO standar tinggi badan perempuan
        who_boys_weight_path : str
            Path ke file WHO standar berat badan laki-laki
        who_girls_weight_path : str
            Path ke file WHO standar berat badan perempuan
        """
        # Load data WHO untuk Tinggi Badan (Height-for-Age)
        self.who_boys_height = pd.read_csv(who_boys_height_path)
        self.who_girls_height = pd.read_csv(who_girls_height_path)
        
        # Load data WHO untuk Berat Badan (Weight-for-Age)
        self.who_boys_weight = pd.read_csv(who_boys_weight_path)
        self.who_girls_weight = pd.read_csv(who_girls_weight_path)
        
        print("✓ Data WHO berhasil dimuat")
        print(f"  - Laki-laki TB: {len(self.who_boys_height)} bulan data")
        print(f"  - Perempuan TB: {len(self.who_girls_height)} bulan data")
        print(f"  - Laki-laki BB: {len(self.who_boys_weight)} bulan data")
        print(f"  - Perempuan BB: {len(self.who_girls_weight)} bulan data")
    
    
    def calculate_zscore(self, value, L, M, S):
        """
        Hitung Z-score menggunakan metode LMS (Lambda-Mu-Sigma)
        
        Formula WHO:
        Z = [(value/M)^L - 1] / (L * S)
        
        Parameters:
        -----------
        value : float
            Nilai yang ingin dihitung (TB atau BB)
        L : float
            Parameter L dari WHO (Box-Cox transformation)
        M : float
            Parameter M dari WHO (Median)
        S : float
            Parameter S dari WHO (Coefficient of variation)
            
        Returns:
        --------
        float : Z-score
        """
        if L == 0:
            # Special case: L = 0
            zscore = np.log(value / M) / S
        else:
            # Standard LMS formula
            zscore = ((value / M) ** L - 1) / (L * S)
        
        return zscore
    
    
    def get_who_parameters(self, gender, age_months, parameter_type='height'):
        """
        Ambil parameter L, M, S dari tabel WHO
        
        Parameters:
        -----------
        gender : str
            'Laki-laki' atau 'Perempuan'
        age_months : int
            Umur dalam bulan (0-24)
        parameter_type : str
            'height' untuk tinggi badan, 'weight' untuk berat badan
            
        Returns:
        --------
        dict : {'L': float, 'M': float, 'S': float}
        """
        # Pilih dataset WHO sesuai gender dan parameter type
        is_male = gender.lower() in ['laki-laki', 'laki', 'male', 'l', 'boy', 'boys']
        
        if parameter_type == 'height':
            who_data = self.who_boys_height if is_male else self.who_girls_height
        else:  # weight
            who_data = self.who_boys_weight if is_male else self.who_girls_weight
        
        # Filter berdasarkan umur
        month_col = 'Month'  # Nama kolom untuk bulan
        
        row = who_data[who_data[month_col] == age_months]
        
        if row.empty:
            raise ValueError(f"Data WHO tidak tersedia untuk umur {age_months} bulan")
        
        # Ambil parameter L, M, S
        params = {
            'L': row['L'].values[0],
            'M': row['M'].values[0],
            'S': row['S'].values[0]
        }
        
        return params
    
    
    def classify_stunting(self, zscore_height):
        """
        Klasifikasi status stunting berdasarkan Z-score tinggi badan
        
        Kategori WHO + Praktik Lapangan:
        - Severely Stunted: Z-score < -3 (< SD3neg)
        - Stunted: -3 <= Z-score < -2 (< SD2neg)
        - At Risk (Early Warning): -2 <= Z-score < -1 (< SD1neg)
        - Normal: -1 <= Z-score <= 2
        - Tall: Z-score > 2
        
        Parameters:
        -----------
        zscore_height : float
            Z-score untuk tinggi badan
            
        Returns:
        --------
        str : Kategori stunting
        """
        if zscore_height < -3:
            return "Severely Stunted"
        elif zscore_height < -2:
            return "Stunted"
        elif zscore_height < -1:
            return "At Risk (Early Warning)"
        elif zscore_height <= 2:
            return "Normal"
        else:
            return "Tall"
    
    
    def classify_wasting(self, zscore_weight):
        """
        Klasifikasi status wasting berdasarkan Z-score berat badan
        
        Kategori WHO + Praktik Lapangan:
        - Severely Underweight: Z-score < -3 (< SD3neg)
        - Underweight: -3 <= Z-score < -2 (< SD2neg)
        - At Risk (Early Warning): -2 <= Z-score < -1 (< SD1neg)
        - Normal weight: -1 <= Z-score <= 1
        - Risk of Overweight: 1 < Z-score <= 2
        - Overweight: Z-score > 2
        
        Parameters:
        -----------
        zscore_weight : float
            Z-score untuk berat badan
            
        Returns:
        --------
        str : Kategori wasting
        """
        if zscore_weight < -3:
            return "Severely Underweight"
        elif zscore_weight < -2:
            return "Underweight"
        elif zscore_weight < -1:
            return "At Risk (Early Warning)"
        elif zscore_weight <= 1:
            return "Normal weight"
        elif zscore_weight <= 2:
            return "Risk of Overweight"
        else:
            return "Overweight"
    
    
    def classify(self, gender, age_months, height_cm, weight_kg):
        """
        FUNGSI UTAMA: Klasifikasi lengkap status gizi anak
        
        Parameters:
        -----------
        gender : str
            Jenis kelamin ('Laki-laki' atau 'Perempuan')
        age_months : int
            Umur dalam bulan (0-24)
        height_cm : float
            Tinggi badan dalam cm
        weight_kg : float
            Berat badan dalam kg
            
        Returns:
        --------
        dict : Hasil klasifikasi lengkap
        """
        try:
            # 1. Ambil parameter WHO untuk tinggi badan
            params_height = self.get_who_parameters(gender, age_months, 'height')
            
            # 2. Hitung Z-score tinggi badan
            zscore_height = self.calculate_zscore(
                height_cm, 
                params_height['L'], 
                params_height['M'], 
                params_height['S']
            )
            
            # 3. Klasifikasi stunting
            stunting_status = self.classify_stunting(zscore_height)
            
            # 4. Ambil parameter WHO untuk berat badan (Weight-for-Age)
            params_weight = self.get_who_parameters(gender, age_months, 'weight')
            
            # 5. Hitung Z-score berat badan
            zscore_weight = self.calculate_zscore(
                weight_kg,
                params_weight['L'],
                params_weight['M'],
                params_weight['S']
            )
            
            # 6. Klasifikasi wasting
            wasting_status = self.classify_wasting(zscore_weight)
            
            # 7. Deteksi risiko (jika mendekati threshold)
            risk_alert = self._detect_risk(zscore_height, zscore_weight)
            
            # Return hasil lengkap
            return {
                'gender': gender,
                'age_months': age_months,
                'height_cm': height_cm,
                'weight_kg': weight_kg,
                'zscore_height': round(zscore_height, 2),
                'zscore_weight': round(zscore_weight, 2),
                'stunting_status': stunting_status,
                'wasting_status': wasting_status,
                'risk_alert': risk_alert,
                'status': 'success'
            }
            
        except Exception as e:
            return {
                'status': 'error',
                'message': str(e)
            }
    
    
    def _detect_risk(self, zscore_height, zscore_weight):
        """
        Deteksi risiko berdasarkan Z-score yang mendekati threshold
        
        Berdasarkan praktik lapangan:
        - SD1neg (Z-score -1) = Early Warning
        - SD2neg (Z-score -2) = Sudah Stunting/Underweight
        - SD3neg (Z-score -3) = Severely Stunted/Underweight
        
        Parameters:
        -----------
        zscore_height : float
        zscore_weight : float
        
        Returns:
        --------
        dict : Informasi risiko
        """
        risks = []
        
        # Risiko stunting (berdasarkan SD threshold)
        if zscore_height <= -3:
            risks.append("🚨 DARURAT: Severely stunted (< SD3neg) - butuh intervensi segera")
        elif zscore_height < -2:
            risks.append("⚠️ STUNTING TERDETEKSI (< SD2neg) - konsultasi ahli gizi")
        elif zscore_height < -1:
            risks.append("⚠️ EARLY WARNING: At risk stunting (< SD1neg) - monitoring ketat diperlukan")
        elif -1.2 < zscore_height < -1:
            risks.append("⚡ Mendekati threshold early warning - perhatikan asupan nutrisi")
        
        # Risiko wasting/underweight
        if zscore_weight <= -3:
            risks.append("🚨 DARURAT: Severely underweight (< SD3neg) - butuh intervensi segera")
        elif zscore_weight < -2:
            risks.append("⚠️ UNDERWEIGHT TERDETEKSI (< SD2neg) - konsultasi ahli gizi")
        elif zscore_weight < -1:
            risks.append("⚠️ EARLY WARNING: At risk underweight (< SD1neg) - monitoring ketat diperlukan")
        elif -1.2 < zscore_weight < -1:
            risks.append("⚡ Mendekati threshold early warning - perhatikan asupan nutrisi")
        
        # Risiko overweight
        if zscore_weight > 2:
            risks.append("⚠️ OVERWEIGHT (> SD2) - konsultasi nutrisionis")
        elif 1.5 < zscore_weight <= 2:
            risks.append("⚡ Berisiko overweight (> SD1.5) - perlu pengaturan pola makan")
        
        # Tentukan risk level
        if any('DARURAT' in r for r in risks):
            risk_level = 'high'
        elif any('TERDETEKSI' in r or 'EARLY WARNING' in r for r in risks):
            risk_level = 'medium'
        elif len(risks) > 0:
            risk_level = 'low'
        else:
            risk_level = 'none'
        
        return {
            'has_risk': len(risks) > 0,
            'risk_level': risk_level,
            'risk_messages': risks
        }


def main():
    """
    Contoh penggunaan WHO Classifier
    """
    print("=" * 60)
    print("WHO GROWTH CLASSIFICATION SYSTEM")
    print("=" * 60)
    print()
    
    # Inisialisasi classifier dengan 4 file WHO
    classifier = WHOClassifier(
        who_boys_height_path='data/WHO Indicators Boys 2 years_Tinggi.csv',
        who_girls_height_path='data/WHO Indicators Girls 2 years_Tinggi.csv',
        who_boys_weight_path='data/WHO Indicators Boys 2 years_Berat.csv',
        who_girls_weight_path='data/WHO Indicators Girls 2 years_Berat.csv'
    )
    
    print("\n" + "=" * 60)
    print("CONTOH KLASIFIKASI")
    print("=" * 60)
    
    # Contoh 1: Anak laki-laki umur 12 bulan
    print("\n📊 Contoh 1: Anak Laki-laki, 12 bulan")
    result1 = classifier.classify(
        gender='Laki-laki',
        age_months=12,
        height_cm=75.5,
        weight_kg=9.2
    )
    print_result(result1)
    
    # Contoh 2: Anak perempuan umur 6 bulan
    print("\n📊 Contoh 2: Anak Perempuan, 6 bulan")
    result2 = classifier.classify(
        gender='Perempuan',
        age_months=6,
        height_cm=65.0,
        weight_kg=7.0
    )
    print_result(result2)
    
    # Contoh 3: Kasus berisiko stunting
    print("\n📊 Contoh 3: Kasus Berisiko Stunting")
    result3 = classifier.classify(
        gender='Laki-laki',
        age_months=18,
        height_cm=76.0,  # Tinggi di bawah normal
        weight_kg=9.5
    )
    print_result(result3)


def print_result(result):
    """
    Cetak hasil klasifikasi dengan format yang rapi
    """
    if result['status'] == 'error':
        print(f"❌ ERROR: {result['message']}")
        return
    
    print(f"Gender        : {result['gender']}")
    print(f"Umur          : {result['age_months']} bulan")
    print(f"Tinggi Badan  : {result['height_cm']} cm (Z-score: {result['zscore_height']})")
    print(f"Berat Badan   : {result['weight_kg']} kg (Z-score: {result['zscore_weight']})")
    print(f"Status Stunting: {result['stunting_status']}")
    print(f"Status Wasting : {result['wasting_status']}")
    
    risk = result['risk_alert']
    if risk['has_risk']:
        print(f"\n⚠️  RISK LEVEL: {risk['risk_level'].upper()}")
        for msg in risk['risk_messages']:
            print(f"   • {msg}")
    else:
        print(f"\n✅ Status: Normal, tidak ada risiko terdeteksi")


if __name__ == "__main__":
    main()
