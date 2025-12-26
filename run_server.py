"""
BalitaSehat API Server Runner
Jalankan server dengan: python run_server.py
"""

import sys
import os

# Add project root to Python path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Import and run server
from app.api_server_mysql import app

if __name__ == '__main__':
    print("="*60)
    print("🚀 BalitaSehat API Server")
    print("="*60)
    print("\n📍 Server URL: http://localhost:5000")
    print("📍 API Docs: http://localhost:5000/api/health")
    print("\n⌨️  Tekan CTRL+C untuk stop server\n")
    
    # Run Flask app
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=True
    )
