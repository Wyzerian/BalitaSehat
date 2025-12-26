# Database Configuration Template
# COPY file ini menjadi db_config.py dan isi dengan kredensial database kamu

DB_CONFIG = {
    'host': 'localhost',           # Host database
    'user': 'your_mysql_user',     # Ganti dengan user MySQL kamu
    'password': 'your_password',   # Ganti dengan password MySQL kamu
    'database': 'balita_sehat',    # Nama database
    'port': 3306                   # Port MySQL (default: 3306)
}

# CARA SETUP:
# 1. Copy file ini: cp config/db_config.example.py config/db_config.py
# 2. Edit db_config.py dengan kredensial database kamu
# 3. JANGAN commit db_config.py ke Git!

# Contoh untuk Development:
# DB_CONFIG = {
#     'host': 'localhost',
#     'user': 'root',
#     'password': 'password123',
#     'database': 'balita_sehat',
#     'port': 3306
# }

# Contoh untuk Production (VPS):
# DB_CONFIG = {
#     'host': 'localhost',
#     'user': 'balita_user',
#     'password': 'strong_password_here',
#     'database': 'balita_sehat',
#     'port': 3306
# }
