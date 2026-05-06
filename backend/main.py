from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import sqlite3

app = FastAPI()

# config baza de date
def init_db():
    conn = sqlite3.connect("mystflow.db")
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT,
            email TEXT UNIQUE,
            password TEXT
        )
    """)
    conn.commit()
    conn.close()

init_db()

class LoginRequest(BaseModel):
    email: str
    password: str

class RegisterRequest(BaseModel):
    username: str
    email: str
    password: str

# --- RUTE API ---

@app.post("/register")
def register(user: RegisterRequest):
    conn = sqlite3.connect("mystflow.db")
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO users (username, email, password) VALUES (?, ?, ?)",
            (user.username, user.email, user.password)
        )
        conn.commit()
        return {"status": "success", "message": "Cont creat cu succes!"}
    except sqlite3.IntegrityError:
        return {"status": "error", "message": "Acest email este deja folosit."}
    finally:
        conn.close()

@app.post("/login")
def login(user: LoginRequest):
    conn = sqlite3.connect("mystflow.db")
    cursor = conn.cursor()
    # Căutăm utilizatorul în baza de date
    cursor.execute(
        "SELECT * FROM users WHERE email = ? AND password = ?",
        (user.email, user.password)
    )
    result = cursor.fetchone()
    conn.close()

    if result:
        return {
            "status": "success",
            "message": f"Bine ai venit, {result[1]}!",
            "user_id": result[0]
        }
    else:
        return {"status": "error", "message": "Email sau parolă incorectă."}