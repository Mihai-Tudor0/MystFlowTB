import os
import google.generativeai as genai
import requests
from dotenv import load_dotenv

load_dotenv()

api_key = os.getenv("GEMINI_API_KEY")
if api_key:
    genai.configure(api_key=api_key.strip())

# suggest-bot_openrouter
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
OPENROUTER_MODEL = "openrouter/auto:free"


def generate_security_insights(user_data: dict) -> str:
    if not OPENROUTER_API_KEY:
        return f"Salut {user_data['username']}, pentru o siguranță maximă a contului MystFlow, îți recomandăm să folosești o parolă unică și să activezi autentificarea în doi pași (2FA)."

    prompt = (
        "Instrucțiune de sistem: Ești Agentul Analist de Securitate al aplicației MystFlow. "
        "Oferă-i acestui utilizator un sfat scurt de securitate în limba română (maximum 3 propoziții), "
        f"știind că nu are activată autentificarea în doi pași. Date utilizator: {user_data}\n"
        "Sfatul tău de securitate:"
    )

    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY.strip()}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": OPENROUTER_MODEL,
        "messages": [
            {"role": "user", "content": prompt}
        ]
    }

    try:
        response = requests.post(OPENROUTER_URL, json=payload, headers=headers, timeout=15)

        if response.status_code == 200:
            return response.json()["choices"][0]["message"]["content"].strip()
        else:
            return f"Salut {user_data['username']}, sistemul recomandă verificarea periodică a sesiunilor active din cont."

    except Exception as e:
        print(f"EROARE SFATURI OPENROUTER: {e}")
        return f"Salut {user_data['username']}, recomandăm activarea autentificării în doi pași (2FA)."

# chatbot_gemini
def generate_chat_response(user_message: str, user_data: dict) -> str:
        if not os.getenv("GEMINI_API_KEY"):
            return "Asistentul de chat este offline momentan."

        try:
            model = genai.GenerativeModel(model_name="gemini-2.5-flash")

            # Îi dăm un context clar despre aplicația MystFlow ca să știe cum să ajute utilizatorul
            prompt = (
                "Instrucțiune de sistem: Ești MystBot, asistentul digital inteligent din interiorul aplicației MystFlow. "
                "Rolul tău este să ajuți utilizatorul cu întrebări despre aplicație, securitate și suport tehnic. "
                "Fii prietenos, dar răspunde DIRECT, scurt (maximum 3-4 propoziții) în limba română. "
                "REGULĂ STRICTĂ: NU saluta utilizatorul la începutul mesajului și nu te mai prezenta, trece direct la subiect! "
                f"Date utilizator: {user_data['username']}.\n\n"
                f"Mesajul utilizatorului: {user_message}\n"
                "Răspunsul tău:"
            )

            response = model.generate_content(prompt)
            return response.text
        except Exception as e:
            print(f"EROARE CHAT GEMINI: {e}")
            return f"Sunt puțin obosit acum, {user_data['username']}. Poți repeta întrebarea?"