import mysql.connector
import json

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="cdac",
        database="lifesamadhan_db_v2"
    )
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users LIMIT 1")
    row = cursor.fetchone()
    print(json.dumps(row, indent=2, default=str))
    
    cursor.execute("DESCRIBE users")
    columns = cursor.fetchall()
    print("\nColumns in users table:")
    print(json.dumps(columns, indent=2))
    
    conn.close()
except Exception as e:
    print(f"Error: {e}")
