# Integrasi MySQL - Ringkasan untuk Tim

## ✅ Yang Sudah Dikerjakan

### 1. File-file Baru untuk MySQL Integration:

- ✅ `db_config.py` - Konfigurasi koneksi MySQL (host, user, password, database)
- ✅ `database.py` - Class untuk manage koneksi dan query MySQL
- ✅ `init_database.sql` - Script SQL untuk buat tabel-tabel
- ✅ `growth_tracker_mysql.py` - Growth tracker versi MySQL (pengganti versi in-memory)
- ✅ `api_server_mysql.py` - API server versi MySQL (pengganti versi in-memory)
- ✅ `test_api_mysql.py` - Script testing untuk MySQL version
- ✅ `SETUP_MYSQL.md` - Dokumentasi lengkap setup MySQL

### 2. Database Schema:

```sql
children         → Data anak (ID, nama, gender, tanggal lahir)
measurements     → Data pengukuran (tinggi, berat, umur, tanggal)
classifications  → Hasil klasifikasi WHO (Z-score, status, risk level)
trend_analysis   → Analisis trend pertumbuhan (opsional)
```

---

## 🚀 Cara Setup (Tim Database)

### Step 1: Install MySQL

- **Pilihan A:** XAMPP (recommended) - https://www.apachefriends.org/
- **Pilihan B:** MySQL Server - https://dev.mysql.com/downloads/mysql/

### Step 2: Buat Database

Via phpMyAdmin atau MySQL command:

```sql
CREATE DATABASE balita_sehat;
```

### Step 3: Jalankan Script SQL

Import file `init_database.sql` ke database `balita_sehat`

**Via phpMyAdmin:**

1. Buka phpMyAdmin → pilih database `balita_sehat`
2. Tab "SQL" → paste isi `init_database.sql`
3. Klik "Go"

**Via MySQL Command:**

```bash
mysql -u root -p balita_sehat < init_database.sql
```

### Step 4: Update Konfigurasi

Edit file `db_config.py`:

```python
DB_CONFIG = {
    'host': 'localhost',        # Ganti sesuai server MySQL
    'user': 'root',             # Username MySQL
    'password': '',             # Password MySQL (kosong jika XAMPP default)
    'database': 'balita_sehat',
    'port': 3306
}
```

**PENTING:** Jangan commit `db_config.py` ke GitHub kalau ada password!

### Step 5: Test Koneksi

```bash
python database.py
```

Output yang benar:

```
Testing database connection...
✓ Berhasil terkoneksi ke database: balita_sehat
```

---

## 🔧 Cara Jalankan API (Tim Backend)

### Jalankan Server MySQL Version:

```bash
python api_server_mysql.py
```

Output:

```
Testing database connection...
✓ Database connected successfully!
Initializing WHO Classifier...
✓ Server ready!
 * Running on http://127.0.0.1:5000
 * Running on http://192.168.0.100:5000
```

### Test API:

```bash
python test_api_mysql.py
```

---

## 📱 Untuk Tim Mobile

**Mobile app TIDAK PERLU koneksi langsung ke MySQL!**

Yang mobile team butuhkan:

1. ✅ URL API server (contoh: `http://192.168.0.100:5000`)
2. ✅ HTTP client (Retrofit/Volley/OkHttp)
3. ✅ Parse JSON response

### Contoh Request dari Kotlin:

```kotlin
// Tambah measurement
POST http://192.168.0.100:5000/api/child/measurement
{
  "child_id": "CHILD001",
  "name": "Budi Santoso",
  "gender": "laki-laki",
  "birth_date": "2024-01-15",
  "age_months": 11,
  "height_cm": 73.5,
  "weight_kg": 9.2
}

// Lihat history
GET http://192.168.0.100:5000/api/child/CHILD001/history
```

Lihat `INTEGRASI_MOBILE.md` untuk contoh lengkap Retrofit/Kotlin.

---

## 📊 Endpoints yang Tersedia

| Endpoint                   | Method | Deskripsi                      |
| -------------------------- | ------ | ------------------------------ |
| `/api/health`              | GET    | Cek status server & database   |
| `/api/classify`            | POST   | Klasifikasi tanpa simpan ke DB |
| `/api/child/measurement`   | POST   | Tambah measurement + simpan    |
| `/api/child/{id}/history`  | GET    | Lihat riwayat pengukuran       |
| `/api/child/{id}/analysis` | GET    | Analisis trend pertumbuhan     |
| `/api/children`            | GET    | Lihat semua anak terdaftar     |
| `/api/who/thresholds`      | GET    | Ambil WHO thresholds           |
| `/api/child/{id}`          | DELETE | Hapus data anak (testing)      |

---

## 🎯 Checklist untuk Hackathon

### Backend Team:

- [ ] Setup MySQL (XAMPP atau MySQL Server)
- [ ] Buat database `balita_sehat`
- [ ] Jalankan `init_database.sql`
- [ ] Konfigurasi `db_config.py` dengan kredensial yang benar
- [ ] Test koneksi: `python database.py`
- [ ] Jalankan API server: `python api_server_mysql.py`
- [ ] Test API: `python test_api_mysql.py`
- [ ] Cek data masuk ke database MySQL (via phpMyAdmin)

### Mobile Team:

- [ ] Dapat URL API dari backend team
- [ ] Setup Retrofit/HTTP client
- [ ] Test endpoint `/api/health`
- [ ] Implement POST `/api/child/measurement`
- [ ] Implement GET `/api/child/{id}/history`
- [ ] Test integrasi end-to-end

### Deployment (Optional):

- [ ] Deploy ke Railway/Heroku untuk akses publik
- [ ] Update URL di mobile app

---

## ❓ FAQ

**Q: Kenapa mobile tidak bisa langsung ke MySQL?**  
A: Android/Kotlin tidak bisa direct connection ke MySQL karena security. Harus lewat API (standard practice).

**Q: Database MySQL harus di cloud?**  
A: Tidak, untuk demo hackathon bisa localhost. Deploy cloud optional untuk production.

**Q: File mana yang harus di-share ke mobile team?**  
A: Cukup URL API dan dokumentasi endpoint (`INTEGRASI_MOBILE.md`). File Python/MySQL tidak perlu.

**Q: Data WHO CSV masih dipakai?**  
A: Ya, untuk calculate Z-score. WHO CSV tetap di backend, tidak perlu di mobile.

**Q: Bisa pakai SQLite saja?**  
A: Bisa, tapi tim sudah buat MySQL database jadi pakai MySQL.

---

## 📞 Koordinasi Tim

**Info yang perlu dikonfirmasi:**

- ✅ Host MySQL: **\_\_\_\_** (localhost atau IP server?)
- ✅ Username: **\_\_\_\_**
- ✅ Password: **\_\_\_\_**
- ✅ Nama Database: **\_\_\_\_** (default: balita_sehat)
- ✅ IP Server API: **\_\_\_\_** (untuk mobile team)

Update info di atas ke `db_config.py` → test koneksi → jalankan API → koordinasi dengan mobile! 🚀

---

**Last Updated:** 23 Desember 2025  
**Version:** 2.0 (MySQL Integration)
