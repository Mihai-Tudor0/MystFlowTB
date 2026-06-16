import sqlite3


def get_user_profile_from_db(user_id: int):
    try:
        conn = sqlite3.connect("mystflow.db")
        cursor = conn.cursor()
        cursor.execute("SELECT username, email FROM users WHERE id = ?", (user_id,))
        user = cursor.fetchone()
        conn.close()

        if user:
            return {
                "username": user[0],
                "email": user[1],
                "has_2fa": False,  # Schimbă în True
                "days_active": 4  # Valoare simulata pt analiza
            }
        return None
    except Exception as e:
        print(f"Eroare DB: {e}")
        return None