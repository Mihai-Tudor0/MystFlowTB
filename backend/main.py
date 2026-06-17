from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from database_service import get_user_profile_from_db
from aiservice import generate_security_insights, generate_chat_response

app = FastAPI()


class ChatMessageRequest(BaseModel):
    message: str
    user_id: str  # Acum e String (UUID de la Supabase)


@app.get("/ai/insights/{user_id}")
def get_user_insights(user_id: str):
    user_data = get_user_profile_from_db(user_id)
    if not user_data:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit în Supabase")

    ai_recommendation = generate_security_insights(user_data)

    return {
        "status": "success",
        "user": user_data["username"],
        "insight": ai_recommendation
    }


@app.post("/ai/chat")
def chat_with_agent(payload: ChatMessageRequest):
    user_data = get_user_profile_from_db(payload.user_id)

    if not user_data:
        user_data = {"username": "Utilizator"}

    ai_reply = generate_chat_response(user_message=payload.message, user_data=user_data)

    return {
        "status": "success",
        "insight": ai_reply
    }