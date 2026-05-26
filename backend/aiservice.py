import os
import google.generativeai as genai
from dotenv import load_dotenv

load_dotenv()

api_key = os.getenv("GEMINI_API_KEY")
if api_key:
    genai.configure(api_key=api_key.strip())


def generate_security_insights(user_data: dict) -> str:
    if not os.getenv("GEMINI_API_KEY"):
        return f"Salut {user_data['username']}! Configurați cheia API în fișierul .env."

    try:
        model = genai.GenerativeModel(model_name="gemini-2.5-flash")

        prompt = (
            "Instrucțiune: Ești Agentul Analist de Securitate al aplicației MystFlow. "
            "Oferă-i acestui utilizator un sfat scurt de securitate în limba română (maximum 3 propoziții), "
            f"știind că nu are activată autentificarea în doi pași: {user_data}"
        )

        response = model.generate_content(prompt)
        return response.text

    except Exception as e:
        print(f"EROARE GEMINI REALĂ: {e}")
        return f"Salut {user_data['username']}, recomandăm activarea autentificării în doi pași (2FA)."

def generate_chat_response(user_message: str, user_data: dict) -> str:
        if not os.getenv("GEMINI_API_KEY"):
            return "Asistentul de chat este offline momentan."

        try:
            model = genai.GenerativeModel(model_name="gemini-2.5-flash")

            # Îi dăm un context clar despre aplicația MystFlow ca să știe cum să ajute utilizatorul
            prompt = (
                "Instrucțiune de sistem: Ești MystBot, asistentul digital inteligent din interiorul aplicației MystFlow. "
                "Rolul tău este să ajuți utilizatorul cu întrebări despre aplicație, securitate și suport tehnic. "
                "Fii prietenos, politicos și răspunde scurt (maximum 3-4 propoziții) în limba română. "
                f"Te adresezi utilizatorului cu numele: {user_data['username']}.\n\n"
                f"Mesajul utilizatorului: {user_message}\n"
                "Răspunsul tău:"
            )

            response = model.generate_content(prompt)
            return response.text
        except Exception as e:
            print(f"EROARE CHAT GEMINI: {e}")
            return f"Sunt puțin obosit acum, {user_data['username']}. Poți repeta întrebarea?"