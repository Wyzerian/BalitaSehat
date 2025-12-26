# Database Connection Manager
import mysql.connector
from mysql.connector import Error
from contextlib import contextmanager
from config.db_config import DB_CONFIG

class DatabaseConnection:
    """Class untuk manage koneksi ke MySQL database"""
    
    @staticmethod
    @contextmanager
    def get_connection():
        """
        Context manager untuk koneksi database
        Otomatis close connection setelah selesai
        
        Usage:
            with DatabaseConnection.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("SELECT * FROM children")
        """
        connection = None
        try:
            connection = mysql.connector.connect(**DB_CONFIG)
            if connection.is_connected():
                yield connection
        except Error as e:
            print(f"Error connecting to MySQL: {e}")
            raise
        finally:
            if connection and connection.is_connected():
                connection.close()
    
    @staticmethod
    def test_connection():
        """Test koneksi ke database"""
        try:
            with DatabaseConnection.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("SELECT DATABASE()")
                db_name = cursor.fetchone()
                cursor.close()
                print(f"✓ Berhasil terkoneksi ke database: {db_name[0]}")
                return True
        except Error as e:
            print(f"✗ Gagal terkoneksi ke database: {e}")
            return False
    
    @staticmethod
    def execute_query(query, params=None):
        """
        Execute query INSERT, UPDATE, DELETE
        Returns: lastrowid atau affected rows
        """
        try:
            with DatabaseConnection.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute(query, params or ())
                conn.commit()
                result = cursor.lastrowid or cursor.rowcount
                cursor.close()
                return result
        except Error as e:
            print(f"Error executing query: {e}")
            raise
    
    @staticmethod
    def fetch_one(query, params=None):
        """
        Execute SELECT query dan return 1 row
        Returns: dict atau None
        """
        try:
            with DatabaseConnection.get_connection() as conn:
                cursor = conn.cursor(dictionary=True)
                cursor.execute(query, params or ())
                result = cursor.fetchone()
                cursor.close()
                return result
        except Error as e:
            print(f"Error fetching data: {e}")
            raise
    
    @staticmethod
    def fetch_all(query, params=None):
        """
        Execute SELECT query dan return semua rows
        Returns: list of dict
        """
        try:
            with DatabaseConnection.get_connection() as conn:
                cursor = conn.cursor(dictionary=True)
                cursor.execute(query, params or ())
                results = cursor.fetchall()
                cursor.close()
                return results
        except Error as e:
            print(f"Error fetching data: {e}")
            raise

# Test koneksi saat import
if __name__ == "__main__":
    print("Testing database connection...")
    DatabaseConnection.test_connection()
