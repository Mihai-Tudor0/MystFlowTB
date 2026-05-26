from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import sqlite3

from database_service import get_user_profile_from_db
from aiservice import generate_security_insights, generate_chat_response
from pydantic import BaseModel
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
# nou adaugat
class ChatMessageRequest(BaseModel):
    message: str

class LoginRequest(BaseModel):
    email: str
    password: str


class RegisterRequest(BaseModel):
    username: str
    email: str
    password: str


# --- RUTE API EXISTENTE ---

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


# analist_ai

@app.get("/ai/insights/{user_id}")
def get_user_insights(user_id: int):

    user_data = get_user_profile_from_db(user_id)
    if not user_data:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit")

    ai_recommendation = generate_security_insights(user_data)

    return {
        "status": "success",
        "user": user_data["username"],
        "insight": ai_recommendation
    }

# chat_ai
@app.post("/ai/chat/{user_id}")
def chat_with_agent(user_id: int, payload: ChatMessageRequest):

    user_data = get_user_profile_from_db(user_id)
    if not user_data:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit")

    ai_reply = generate_chat_response(user_message=payload.message, user_data=user_data)

    return {
        "status": "success",
        "reply": ai_reply
    }