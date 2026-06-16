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
        "Instrucțiune de sistem: Ești Agentul Analist de Securitate al aplicației financiare MystFlow. "
        "Aplicația folosește un sistem de securitate avansat: autentificare prin Număr de Telefon (SMS OTP) "
        "și protecție locală cu PIN și Amprentă (Biometrie). "
        f"Oferă-i utilizatorului ({user_data.get('username', 'Utilizator')}) un singur sfat scurt și util de securitate (max 2 propoziții) în limba română. "
        "Exemple de idei: să nu divulge codul OTP primit prin SMS nimănui (nici măcar angajaților MystFlow), să mențină amprenta activă pentru o deblocare rapidă și sigură, sau să fie atent la atacurile de tip phishing (mesaje false). "
        "Fii direct, profestionist și nu folosi formule de salut la început, scrie direct sfatul."
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
            model = genai.GenerativeModel(model_name="gemini-1.5-flash")

            # Îi dăm un context clar despre aplicația MystFlow ca să știe cum să ajute utilizatorul
            prompt = (
                "Instrucțiune de sistem: Ești MystBot, asistentul digital integrat în aplicația financiară MystFlow. "
                "Rolul tău este să ajuți utilizatorul cu întrebări despre aplicație, securitate și operațiuni. "
                "Aplicația MystFlow are următoarele funcționalități:\n"
                "1. Autentificare securizată prin Număr de Telefon (SMS OTP) la creare/logare, plus protecție cu PIN sau Amprentă la deschiderea pe telefon.\n"
                "2. Un ecran principal cu Balanța disponibilă și un card virtual 'MystFlow Premium' cu număr de card ce poate fi copiat.\n"
                "3. Butonul 'Alimentează' - folosit pentru a adăuga bani în cont.\n"
                "4. Butonul 'Transferă' - folosit pentru a trimite bani altor persoane din MystFlow.\n"
                "5. Secțiunea 'Activitate recentă' în partea de jos, unde se văd istoricul și notificările tranzacțiilor.\n"
                f"Numele utilizatorului cu care vorbești este: {user_data.get('username', 'Utilizator')}.\n"
                "REGULI STRICTE PENTRU TINE: \n"
                "- Fii prietenos, dar răspunde DIRECT și scurt (maximum 2-3 propoziții).\n"
                "- NU saluta utilizatorul la începutul mesajului și nu te mai prezenta, trece direct la subiect.\n"
                "- Dacă te întreabă cum să bage bani, spune-i să apese pe butonul 'Alimentează'.\n"
                "- Dacă te întreabă cum să trimită bani, spune-i să folosească butonul 'Transferă'.\n\n"
                f"Mesajul utilizatorului: {user_message}\n"
                "Răspunsul tău:"
            )

            response = model.generate_content(prompt)
            return response.text
        except Exception as e:
            print(f"EROARE CHAT GEMINI: {e}")
            return f"Sunt puțin obosit acum, {user_data['username']}. Poți repeta întrebarea?"