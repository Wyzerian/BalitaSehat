# Database Configuration Template
# Copy file ini dan rename ke: db_config.py
# JANGAN commit db_config.py ke GitHub (sudah ada di .gitignore)

DB_CONFIG = {
    'host': 'localhost',        # Ganti dengan host MySQL (localhost atau IP server)
    'user': 'root',             # Ganti dengan username MySQL
    'password': '',             # Ganti dengan password MySQL
    'database': 'balita_sehat', # Nama database yang sudah dibuat tim
    'port': 3306,               # Port MySQL (default: 3306)
    'charset': 'utf8mb4',
    'autocommit': True
}

# CONTOH KONFIGURASI:
# ===================

# Contoh 1: XAMPP di Windows (default tanpa password)
# DB_CONFIG = {
#     'host': 'localhost',
#     'user': 'root',
#     'password': '',  # Kosong untuk XAMPP default
#     'database': 'balita_sehat',
#     'port': 3306
# }

# Contoh 2: MySQL dengan password
# DB_CONFIG = {
#     'host': 'localhost',
#     'user': 'root',
#     'password': 'admin123',  # <-- Password MySQL kamu
#     'database': 'balita_sehat',
#     'port': 3306
# }

# Contoh 3: MySQL di server cloud/VPS
# DB_CONFIG = {
#     'host': 'db.yourserver.com',  # Domain atau IP server
#     'user': 'balitasehat_user',
#     'password': 'strongpassword123',
#     'database': 'balita_sehat',
#     'port': 3306
# }

# Contoh 4: Railway/Heroku MySQL
# DB_CONFIG = {
#     'host': 'containers-us-west-123.railway.app',
#     'user': 'root',
#     'password': 'railway_generated_password',
#     'database': 'railway',
#     'port': 5432  # Railway kadang pakai port berbeda
# }

# SECURITY TIPS:
# ==============
# 1. JANGAN commit file db_config.py ke GitHub!
# 2. Gunakan environment variables untuk production:
#    import os
#    DB_CONFIG = {
#        'host': os.getenv('DB_HOST', 'localhost'),
#        'user': os.getenv('DB_USER', 'root'),
#        'password': os.getenv('DB_PASSWORD', ''),
#        'database': os.getenv('DB_NAME', 'balita_sehat'),
#        'port': int(os.getenv('DB_PORT', 3306))
#    }
# 3. Gunakan user MySQL khusus (bukan root) untuk production
# 4. Set strong password untuk MySQL user
