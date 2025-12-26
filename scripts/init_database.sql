-- Database Schema untuk Balita Sehat
-- Jalankan script ini untuk membuat tabel-tabel yang dibutuhkan

-- Buat database (jika belum ada)
CREATE DATABASE IF NOT EXISTS stunting;
USE balita_sehat;

-- Tabel untuk menyimpan data anak
CREATE TABLE IF NOT EXISTS children (
    id VARCHAR(50) PRIMARY KEY,
    nik_anak CHAR(16) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    parent_name VARCHAR(100) DEFAULT NULL,
    address TEXT DEFAULT NULL,
    gender ENUM('laki-laki', 'perempuan') NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nik_anak (nik_anak),
    INDEX idx_gender (gender),
    INDEX idx_birth_date (birth_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabel untuk menyimpan hasil pengukuran
CREATE TABLE IF NOT EXISTS measurements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    child_id VARCHAR(50) NOT NULL,
    measurement_date DATE NOT NULL,
    age_months INT NOT NULL,
    height_cm DECIMAL(5,2) NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    INDEX idx_child_date (child_id, measurement_date),
    INDEX idx_age (age_months)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabel untuk menyimpan hasil klasifikasi
CREATE TABLE IF NOT EXISTS classifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    measurement_id INT NOT NULL,
    child_id VARCHAR(50) NOT NULL,
    height_zscore DECIMAL(5,2),
    weight_zscore DECIMAL(5,2),
    stunting_status VARCHAR(50),
    wasting_status VARCHAR(50),
    risk_level ENUM('NONE', 'LOW', 'MEDIUM', 'HIGH'),
    warnings TEXT,
    recommendations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (measurement_id) REFERENCES measurements(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    INDEX idx_child (child_id),
    INDEX idx_risk (risk_level),
    INDEX idx_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabel untuk menyimpan analisis trend (opsional)
CREATE TABLE IF NOT EXISTS trend_analysis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    child_id VARCHAR(50) NOT NULL,
    analysis_date DATE NOT NULL,
    height_trend VARCHAR(20),
    weight_trend VARCHAR(20),
    height_change_per_month DECIMAL(5,2),
    weight_change_per_month DECIMAL(5,2),
    predicted_height_next_month DECIMAL(5,2),
    predicted_weight_next_month DECIMAL(5,2),
    warnings TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    INDEX idx_child_date (child_id, analysis_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- View untuk laporan lengkap (gabungan semua data)
CREATE OR REPLACE VIEW vw_child_report AS
SELECT 
    c.id as child_id,
    c.name,
    c.gender,
    c.birth_date,
    m.id as measurement_id,
    m.measurement_date,
    m.age_months,
    m.height_cm,
    m.weight_kg,
    cl.height_zscore,
    cl.weight_zscore,
    cl.stunting_status,
    cl.wasting_status,
    cl.risk_level,
    cl.warnings,
    cl.recommendations
FROM children c
LEFT JOIN measurements m ON c.id = m.child_id
LEFT JOIN classifications cl ON m.id = cl.measurement_id
ORDER BY c.id, m.measurement_date DESC;

-- Contoh data untuk testing (OPSIONAL - hapus jika tidak perlu)
-- INSERT INTO children (id, name, gender, birth_date) 
-- VALUES ('CHILD001', 'Budi Santoso', 'laki-laki', '2024-01-15');

-- INSERT INTO measurements (child_id, measurement_date, age_months, height_cm, weight_kg)
-- VALUES ('CHILD001', '2024-12-15', 11, 73.5, 9.2);

SHOW TABLES;
