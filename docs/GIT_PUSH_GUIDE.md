# 🚀 Git Push Guide - Collaboration

Panduan push code ke repository GitHub teman (collaboration).

## 📋 Prerequisites

- Git sudah terinstall
- Sudah ditambahkan sebagai collaborator di repo teman
- Punya akses ke repo

---

## Step 1: Setup Git (First Time)

```bash
# Set identity
git config --global user.name "Nama Kamu"
git config --global user.email "email@example.com"

# Cek config
git config --list
```

---

## Step 2: Connect ke Remote Repository

```bash
# Masuk ke folder project
cd D:\Dev\BalitaSehat

# Check apakah sudah punya remote
git remote -v

# Jika belum ada remote, tambahkan:
git remote add origin https://github.com/username-teman/balita-sehat.git

# Jika sudah ada tapi salah URL:
git remote set-url origin https://github.com/username-teman/balita-sehat.git

# Verify
git remote -v
```

---

## Step 3: Create Branch "Backend"

```bash
# Fetch semua branch dari remote
git fetch origin

# Create & checkout branch Backend
git checkout -b Backend

# Atau jika branch Backend sudah ada di remote:
git checkout Backend
git pull origin Backend
```

---

## Step 4: Verify File Sensitif Tidak Ter-commit

```bash
# Check .gitignore working
git status

# Pastikan file ini TIDAK muncul di list:
# ✗ config/db_config.py
# ✗ static/charts/*.png
# ✗ *.log

# Jika muncul, tambahkan ke .gitignore dulu!
```

---

## Step 5: Stage & Commit Files

```bash
# Stage semua file (kecuali yang di .gitignore)
git add .

# Cek file yang akan di-commit
git status

# Commit dengan message yang jelas
git commit -m "feat: initial backend setup with WHO classification and MySQL"

# Atau commit dengan detail:
git commit -m "feat: backend API setup

- Add WHO classification system (height & weight)
- Add MySQL database integration
- Add 9 REST API endpoints
- Add NIK-based registration
- Add chart generation (growth & z-score)
- Add documentation for mobile integration"
```

---

## Step 6: Push ke Branch Backend

```bash
# Push ke remote branch Backend
git push origin Backend

# Jika first time push branch baru:
git push -u origin Backend
```

**Output yang diharapkan:**

```
Enumerating objects: 123, done.
Counting objects: 100% (123/123), done.
Writing objects: 100% (123/123), 45.67 KiB | 1.52 MiB/s, done.
To https://github.com/username-teman/balita-sehat.git
 * [new branch]      Backend -> Backend
```

---

## Step 7: Verify di GitHub

1. Buka browser: `https://github.com/username-teman/balita-sehat`
2. Klik dropdown branch (biasanya tulisan "main")
3. Pilih branch "Backend"
4. Cek file-file sudah ter-upload

---

## 🔄 Workflow Kolaborasi Selanjutnya

### Setiap Kali Mulai Coding:

```bash
# Update dari remote
git checkout Backend
git pull origin Backend

# Mulai coding...
```

### Setelah Coding:

```bash
# Cek perubahan
git status

# Stage files
git add .

# Commit
git commit -m "feat: add parent_name and address to registration"

# Push
git push origin Backend
```

---

## 🔀 Merge ke Main (Setelah Testing)

**Jangan langsung push ke main!** Gunakan Pull Request:

1. Push ke branch Backend (seperti biasa)
2. Buka GitHub repository
3. Klik "Pull Request"
4. Compare: `Backend` → `main`
5. Tulis deskripsi perubahan
6. Request review dari teman
7. Setelah approved, merge

---

## 🚨 Troubleshooting

### 1. Push Rejected (conflict)

```bash
# Pull dulu untuk sync
git pull origin Backend

# Resolve conflict jika ada
# Edit file yang conflict, lalu:
git add .
git commit -m "fix: resolve merge conflict"
git push origin Backend
```

### 2. Lupa Nama Branch

```bash
# Lihat semua branch
git branch -a
```

### 3. Accidentally Committed Sensitive File

```bash
# Remove from Git (tapi file tetap di lokal)
git rm --cached config/db_config.py

# Commit
git commit -m "fix: remove sensitive file"

# Push
git push origin Backend
```

### 4. Authentication Failed

Jika pakai HTTPS dan error "Authentication failed":

**Option A: Use Personal Access Token**

1. GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Scope: pilih `repo`
4. Copy token
5. Saat push, username = GitHub username, password = TOKEN (bukan password GitHub!)

**Option B: Use SSH**

```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "email@example.com"

# Copy public key
cat ~/.ssh/id_ed25519.pub

# Add ke GitHub (Settings → SSH and GPG keys)

# Change remote URL to SSH
git remote set-url origin git@github.com:username-teman/balita-sehat.git
```

---

## ✅ Checklist Before Push

- [ ] `config/db_config.py` ada di `.gitignore`
- [ ] File password/credentials tidak ter-commit
- [ ] Code sudah di-test lokal
- [ ] Commit message jelas dan deskriptif
- [ ] Push ke branch `Backend`, BUKAN `main`

---

## 📝 Quick Reference

```bash
# Daily workflow
git checkout Backend
git pull origin Backend
# ... coding ...
git add .
git commit -m "feat: description"
git push origin Backend

# Check status
git status

# View commit history
git log --oneline

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard local changes
git checkout -- .
```

---

**Tips**: Gunakan VS Code Git integration untuk lebih mudah! 😉
