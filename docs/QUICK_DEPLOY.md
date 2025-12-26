# 🚀 Quick Deploy - Langkah Demi Langkah

**Situasi:** Kamu sudah login VPS dan sudah di `/home/kemas/balita-sehat/` tapi folder masih kosong.

---

## STEP 1: Upload Files ke VPS

### Option A: Via Git (Tercepat - RECOMMENDED)

**Di Laptop (Windows PowerShell):**

```powershell
# Masuk ke folder project
cd D:\Dev\BalitaSehat

# Initialize git
git init
git add .
git commit -m "Initial commit"

# Push ke GitHub (buat repo dulu di https://github.com/new)
# Nama repo: balita-sehat
git remote add origin https://github.com/username/balita-sehat.git
git branch -M main
git push -u origin main
```

**Di VPS (Terminal SSH):**

```bash
# Clone dari GitHub
cd /home/kemas/balita-sehat
git clone https://github.com/username/balita-sehat.git .
# ^ Titik di akhir = clone ke current directory

# Cek files sudah ada
ls -la
```

### Option B: Via SCP (Upload Manual)

**Di Laptop (Windows PowerShell):**

```powershell
# Compress files
cd D:\Dev
Compress-Archive -Path BalitaSehat\* -DestinationPath balita-sehat.zip

# Upload via SCP (ganti IP & username)
scp balita-sehat.zip kemas@103.xx.xx.xx:/home/kemas/balita-sehat/
```

**Di VPS:**

```bash
cd /home/kemas/balita-sehat
unzip balita-sehat.zip
rm balita-sehat.zip
ls -la  # Verify files
```

---

## STEP 2: Install Python & Dependencies

```bash
# Install Python
sudo apt update
sudo apt install python3 python3-pip python3-venv -y

# Create virtual environment
cd /home/kemas/balita-sehat
python3 -m venv venv

# Activate venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Jika ada error, install manual:
pip install flask flask-cors mysql-connector-python pandas numpy matplotlib
```

---

## STEP 3: Setup MySQL Database

```bash
# Install MySQL (jika belum)
sudo apt install mysql-server -y

# Login ke MySQL
sudo mysql

# Di MySQL prompt, ketik:
```

```sql
CREATE DATABASE balita_sehat;
CREATE USER 'balita_user'@'localhost' IDENTIFIED BY 'BalitaSehat2025!';
GRANT ALL PRIVILEGES ON balita_sehat.* TO 'balita_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## STEP 4: Configure Database

```bash
# Edit db_config.py
nano config/db_config.py
```

**Ganti isinya dengan:**

```python
DB_CONFIG = {
    'host': 'localhost',
    'user': 'balita_user',
    'password': 'BalitaSehat2025!',  # Password yang tadi dibuat
    'database': 'balita_sehat',
    'port': 3306
}
```

**Save:** `Ctrl+O` lalu `Enter`, keluar: `Ctrl+X`

---

## STEP 5: Create Database Tables

```bash
# Run migration script
cd /home/kemas/balita-sehat
mysql -u balita_user -p balita_sehat < scripts/setup_database.sql
# Masukkan password: BalitaSehat2025!

# Verify tables created
mysql -u balita_user -p -e "USE balita_sehat; SHOW TABLES;"
```

**Expected output:**

```
+-------------------------+
| Tables_in_balita_sehat  |
+-------------------------+
| children                |
| classifications         |
| measurements            |
| trend_analysis          |
+-------------------------+
```

---

## STEP 6: Test Server (Development Mode)

```bash
# Make sure venv active
source venv/bin/activate

# Run server
python run_server.py
```

**Expected output:**

```
Testing database connection...
✓ Database connected successfully!
Initializing WHO Classifier...
✓ Server ready!
 * Running on http://0.0.0.0:5000
```

**Test dari browser/Postman:**

```
http://103.xx.xx.xx:5000/api/health
```

Harus muncul:

```json
{
  "status": "healthy",
  "message": "BalitaSehat API is running",
  "database": "connected"
}
```

**Stop server:** `Ctrl+C`

---

## STEP 7: Production Setup (Gunicorn + Systemd)

### 7.1 Install Gunicorn

```bash
# Activate venv
source venv/bin/activate

# Install
pip install gunicorn

# Test
gunicorn --bind 0.0.0.0:5000 run_server:app
# Jika jalan, stop dengan Ctrl+C
```

### 7.2 Create Service

```bash
sudo nano /etc/systemd/system/balita-sehat.service
```

**Paste konfigurasi ini (GANTI `kemas` dengan username VPS kamu):**

```ini
[Unit]
Description=BalitaSehat API
After=network.target

[Service]
Type=notify
User=kemas
WorkingDirectory=/home/kemas/balita-sehat
Environment="PATH=/home/kemas/balita-sehat/venv/bin"
ExecStart=/home/kemas/balita-sehat/venv/bin/gunicorn --workers 4 --bind 0.0.0.0:5000 run_server:app
Restart=always

[Install]
WantedBy=multi-user.target
```

**Save:** `Ctrl+O` → `Enter` → `Ctrl+X`

### 7.3 Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Start service
sudo systemctl start balita-sehat

# Check status
sudo systemctl status balita-sehat
# Tekan 'q' untuk keluar

# Enable auto-start
sudo systemctl enable balita-sehat
```

---

## STEP 8: Test API

```bash
# Test dari VPS
curl http://localhost:5000/api/health

# Test dari laptop/HP
# Buka browser: http://103.xx.xx.xx:5000/api/health
```

**Test semua endpoints:**

```bash
# Health check
curl http://localhost:5000/api/health

# Check NIK (not found)
curl "http://localhost:5000/api/child/check?nik=3301234567890123"

# Register child
curl -X POST http://localhost:5000/api/child/register \
  -H "Content-Type: application/json" \
  -d '{
    "nik_anak": "3301234567890123",
    "name": "Test Anak",
    "gender": "laki-laki",
    "birth_date": "2024-01-15"
  }'
```

---

## 🎯 Update Mobile App

**Edit `RetrofitClient.kt`:**

```kotlin
// Ganti IP dengan IP VPS kamu
private const val BASE_URL_PROD = "http://103.xx.xx.xx:5000/"

// Set production
private const val IS_PRODUCTION = true
```

**Test dari mobile:**

- Buka: `http://103.xx.xx.xx:5000/api/health`
- Harus muncul JSON response

---

## 🔧 Useful Commands

```bash
# Restart service
sudo systemctl restart balita-sehat

# Stop service
sudo systemctl stop balita-sehat

# View logs
sudo journalctl -u balita-sehat -f

# Check status
sudo systemctl status balita-sehat

# Update code (jika pakai Git)
cd /home/kemas/balita-sehat
git pull
sudo systemctl restart balita-sehat
```

---

## 🐛 Troubleshooting

### Service tidak start

```bash
# Lihat error
sudo journalctl -u balita-sehat -n 50

# Check file permissions
sudo chown -R kemas:kemas /home/kemas/balita-sehat

# Check database connection
mysql -u balita_user -p -e "USE balita_sehat; SHOW TABLES;"
```

### Port 5000 sudah dipakai

```bash
# Kill process
sudo lsof -ti:5000 | xargs sudo kill -9

# Restart service
sudo systemctl restart balita-sehat
```

### Database error

```bash
# Check MySQL running
sudo systemctl status mysql

# Restart MySQL
sudo systemctl restart mysql

# Check config
cat config/db_config.py
```

---

## ✅ Checklist

- [ ] Files uploaded ke VPS
- [ ] Virtual environment created
- [ ] Dependencies installed
- [ ] MySQL database created
- [ ] Tables created (4 tables)
- [ ] Server running (port 5000)
- [ ] API health check berhasil
- [ ] Mobile app bisa connect

**Selamat! API kamu sekarang LIVE! 🚀**

**URL API Production:** `http://103.xx.xx.xx:5000`

(Ganti `103.xx.xx.xx` dengan IP VPS kamu)
