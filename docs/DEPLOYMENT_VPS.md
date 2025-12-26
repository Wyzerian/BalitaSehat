# Deployment Guide - VPS

## Persiapan VPS

### 1. Install Dependencies

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Python 3 dan pip
sudo apt install python3 python3-pip python3-venv -y

# Install MySQL
sudo apt install mysql-server -y
```

### 2. Setup MySQL Database

```bash
# Login ke MySQL
sudo mysql -u root -p

# Buat database dan user
CREATE DATABASE balita_sehat;
CREATE USER 'balitasehat_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON balita_sehat.* TO 'balitasehat_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Import schema
mysql -u balitasehat_user -p balita_sehat < scripts/init_database.sql
```

### 3. Upload Files

```bash
# Di local PC, compress files
tar -czf balitasehat.tar.gz app/ config/ data/ static/ scripts/ tests/ docs/ run_server.py requirements.txt README.md

# Upload ke VPS
scp balitasehat.tar.gz user@your-vps-ip:/home/user/

# Di VPS, extract
cd /home/user
tar -xzf balitasehat.tar.gz
cd balitasehat
```

### 4. Setup Python Environment

```bash
# Buat virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### 5. Configure Database

```bash
# Copy example config
cp config/db_config.example.py config/db_config.py

# Edit dengan credentials VPS
nano config/db_config.py
```

Edit `config/db_config.py`:

```python
DB_CONFIG = {
    'host': 'localhost',
    'user': 'balitasehat_user',
    'password': 'your_password',
    'database': 'balita_sehat'
}
```

### 6. Test Server

```bash
# Test run
python run_server.py

# Test dari browser/postman
curl http://your-vps-ip:5000/api/health
```

### 7. Setup Firewall

```bash
# Buka port 5000
sudo ufw allow 5000/tcp
sudo ufw reload
```

## Production Setup dengan Gunicorn + Nginx

### 1. Install Gunicorn

```bash
pip install gunicorn
```

### 2. Test Gunicorn

```bash
gunicorn -w 4 -b 0.0.0.0:5000 "app.api_server_mysql:app"
```

### 3. Setup Systemd Service

```bash
sudo nano /etc/systemd/system/balitasehat.service
```

Content:

```ini
[Unit]
Description=BalitaSehat API Server
After=network.target

[Service]
User=your-username
WorkingDirectory=/home/user/balitasehat
Environment="PATH=/home/user/balitasehat/venv/bin"
ExecStart=/home/user/balitasehat/venv/bin/gunicorn -w 4 -b 0.0.0.0:5000 "app.api_server_mysql:app"
Restart=always

[Install]
WantedBy=multi-user.target
```

Enable service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable balitasehat
sudo systemctl start balitasehat
sudo systemctl status balitasehat
```

### 4. Setup Nginx (Optional)

```bash
sudo apt install nginx -y
sudo nano /etc/nginx/sites-available/balitasehat
```

Content:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /static {
        alias /home/user/balitasehat/static;
    }
}
```

Enable site:

```bash
sudo ln -s /etc/nginx/sites-available/balitasehat /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 5. Setup Auto-Cleanup Charts (Optional)

```bash
# Crontab untuk hapus chart > 7 hari
crontab -e
```

Add:

```
0 2 * * * find /home/user/balitasehat/static/charts -name "*.png" -mtime +7 -delete
```

## Monitoring & Maintenance

### View Logs

```bash
# Systemd logs
sudo journalctl -u balitasehat -f

# Nginx logs
sudo tail -f /var/log/nginx/error.log
```

### Restart Service

```bash
sudo systemctl restart balitasehat
```

### Update Code

```bash
cd /home/user/balitasehat
source venv/bin/activate
git pull  # jika pakai git
sudo systemctl restart balitasehat
```

## Troubleshooting

### Database Connection Error

- Check MySQL service: `sudo systemctl status mysql`
- Check credentials di `config/db_config.py`
- Check MySQL user permissions

### Port Already in Use

```bash
# Kill process di port 5000
sudo lsof -t -i:5000 | xargs sudo kill -9
sudo systemctl restart balitasehat
```

### Permission Error

```bash
# Fix ownership
sudo chown -R your-username:your-username /home/user/balitasehat
```

---

✅ **Server Ready!**  
API URL: `http://your-vps-ip:5000`
