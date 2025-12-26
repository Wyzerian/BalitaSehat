# API Posyandu Workflow

## Overview

API untuk workflow petugas posyandu:

1. Check NIK anak
2. Jika belum terdaftar → Register
3. Input measurement (tinggi/berat)
4. Lihat grafik pertumbuhan

---

## Endpoint 1: Check NIK

**Purpose:** Cek apakah anak sudah terdaftar

**Request:**

```http
GET http://localhost:5000/api/child/check?nik=3301150120240001
```

**Response - Jika Ditemukan:**

```json
{
  "status": "found",
  "data": {
    "child_id": "BUDI001",
    "nik_anak": "3301150120240001",
    "name": "Budi Santoso",
    "gender": "laki-laki",
    "birth_date": "2024-01-15",
    "created_at": "2025-12-23 15:11:02"
  }
}
```

**Response - Jika Tidak Ditemukan:**

```json
{
  "status": "not_found",
  "message": "NIK belum terdaftar. Silakan daftar anak terlebih dahulu."
}
```

---

## Endpoint 2: Register Anak Baru

**Purpose:** Daftarkan anak baru ke sistem

**Request:**

```http
POST http://localhost:5000/api/child/register
Content-Type: application/json

{
  "nik_anak": "3302230720240002",
  "name": "Siti Aisyah",
  "gender": "perempuan",
  "birth_date": "2024-07-23"
}
```

**Response - Success:**

```json
{
  "status": "success",
  "message": "Anak berhasil didaftarkan",
  "data": {
    "child_id": "SITI453",
    "nik_anak": "3302230720240002",
    "name": "Siti Aisyah",
    "gender": "perempuan",
    "birth_date": "2024-07-23"
  }
}
```

**Response - NIK Sudah Ada:**

```json
{
  "status": "error",
  "message": "NIK sudah terdaftar atas nama Siti Aisyah",
  "data": {
    "child_id": "SITI001",
    ...
  }
}
```

---

## Endpoint 3: Input Measurement (Simplified - Recommended)

**Purpose:** Input data pengukuran baru - SIMPLE untuk petugas posyandu

**Request:**

```http
POST http://localhost:5000/api/measurement/add
Content-Type: application/json

{
  "nik_anak": "3301150120240001",
  "height_cm": 85.0,
  "weight_kg": 13.0,
  "measurement_date": "2024-12-23"
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Data pengukuran berhasil disimpan",
  "data": {
    "measurement_id": 5,
    "child": {
      "child_id": "BUDI001",
      "nik_anak": "3301150120240001",
      "name": "Budi Santoso",
      "gender": "laki-laki",
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
```

**Keuntungan endpoint ini:**

- ✅ Petugas cukup input NIK + tinggi + berat
- ✅ Auto-lookup data anak dari database
- ✅ Auto-calculate umur dari tanggal lahir
- ✅ Langsung dapat chart URLs

---

## Endpoint 3B: Input Measurement (Full - Legacy)

**Purpose:** Tambah data pengukuran baru

**Request:**

```http
POST http://localhost:5000/api/child/measurement
Content-Type: application/json

{
  "child_id": "BUDI001",
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 11,
  "height_cm": 73.5,
  "weight_kg": 9.2,
  "measurement_date": "2024-12-23"
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "measurement_id": 5,
    "child_id": "BUDI001",
    "classification": {
      "height_zscore": 0.15,
      "weight_zscore": -0.32,
      "classification_height": "Normal",
      "classification_weight": "Normal weight",
      "risk_level": "NONE"
    }
  }
}
```

---

## Endpoint 4: Get Chart

**Purpose:** Generate dan download grafik pertumbuhan

**Request - Growth Chart:**

```http
GET http://localhost:5000/api/child/BUDI001/chart?type=growth
```

**Request - Z-Score Chart:**

```http
GET http://localhost:5000/api/child/BUDI001/chart?type=zscore
```

**Response:**

```json
{
  "status": "success",
  "chart_url": "/static/charts/BUDI001_growth.png",
  "full_url": "http://localhost:5000/static/charts/BUDI001_growth.png"
}
```

---

## Complete Workflow Example

### Scenario: Ibu membawa anak pertama kali ke posyandu

**Step 1: Petugas input NIK**

```http
GET /api/child/check?nik=3303120420240003
```

Response: `not_found` (anak belum terdaftar)

**Step 2: Register anak**

```http
POST /api/child/register
{
  "nik_anak": "3303120420240003",
  "name": "Ahmad Rizki",
  "gender": "laki-laki",
  "birth_date": "2024-04-12"
}
```

Response: `child_id = AHMA234`

**Step 3: Input measurement bulan ke-0**

```http
POST /api/child/measurement
{
  "child_id": "AHMA234",
  "name": "Ahmad Rizki",
  "gender": "laki-laki",
  "birth_date": "2024-04-12",
  "age_months": 8,
  "height_cm": 70.5,
  "weight_kg": 8.5
}
```

**Step 4: Generate grafik**

```http
GET /api/child/AHMA234/chart?type=growth
GET /api/child/AHMA234/chart?type=zscore
```

---

## NIK Format Validation

- **Length:** 16 digit
- **Type:** Numeric only
- **Unique:** Tidak boleh duplikat
- **Example:** `3301150120240001`

**Format NIK:**

```
33        - Kode provinsi (Jawa Tengah)
01        - Kode kabupaten
15        - Tanggal lahir (15)
01        - Bulan lahir (Januari)
2024      - Tahun lahir
0001      - Urutan registrasi
```

---

## Error Codes

| HTTP Code | Status    | Description                  |
| --------- | --------- | ---------------------------- |
| 200       | success   | Request berhasil             |
| 201       | success   | Resource created             |
| 400       | error     | Bad request (validasi gagal) |
| 404       | not_found | Data tidak ditemukan         |
| 409       | error     | Conflict (NIK duplikat)      |
| 500       | error     | Server error                 |

---

## Testing dengan Postman

### Import Collection

1. Buat New Collection: "BalitaSehat Posyandu"
2. Add requests sesuai dokumentasi di atas
3. Set base URL: `http://localhost:5000`

### Test Sequence

1. Health Check → `GET /api/health`
2. Check NIK baru → `GET /api/child/check?nik=...` (expect not_found)
3. Register → `POST /api/child/register`
4. Check NIK lagi → (expect found)
5. Add measurement → `POST /api/child/measurement`
6. Get chart → `GET /api/child/{child_id}/chart?type=growth`

---

## Mobile Integration Notes

### Workflow di Mobile:

```
1. Petugas buka app
2. Input NIK di form
3. App hit: GET /api/child/check?nik={nik}
4. Jika not_found:
   - Show form: Nama, Gender, Tanggal Lahir
   - Submit → POST /api/child/register
5. Jika found:
   - Auto-fill nama anak
   - Show form: Tinggi, Berat
6. Calculate age_months dari birth_date
7. Submit → POST /api/child/measurement
8. Show hasil klasifikasi
9. Load chart images:
   - Glide.load("http://server/static/charts/{child_id}_growth.png")
   - Glide.load("http://server/static/charts/{child_id}_zscore.png")
```

### Auto-Calculate Age

```kotlin
fun calculateAgeMonths(birthDate: String, measurementDate: String = today()): Int {
    val birth = LocalDate.parse(birthDate)
    val measure = LocalDate.parse(measurementDate)
    return ChronoUnit.MONTHS.between(birth, measure).toInt()
}
```

---

## Database Schema (After Migration)

```sql
children (
  id VARCHAR(50) PK,
  nik_anak CHAR(16) UNIQUE NOT NULL,
  name VARCHAR(100),
  gender ENUM('laki-laki', 'perempuan'),
  birth_date DATE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
```

**Changes from previous version:**

- ✅ Added: `nik_anak` (unique, not null)
- ❌ Removed: `parent_name` (tidak diperlukan)
