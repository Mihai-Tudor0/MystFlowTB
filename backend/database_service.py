import os
from supabase import create_client, Client
from dotenv import load_dotenv

load_dotenv()


url: str = os.getenv("SUPABASE_URL")
key: str = os.getenv("SUPABASE_KEY")


if url and key:
    supabase: Client = create_client(url, key)
else:
    supabase = None
    print("AVERTISMENT: SUPABASE_URL sau SUPABASE_KEY lipsesc!")


def get_user_profile_from_db(user_id: str):
    if not supabase:
        return None

    try:
        response = supabase.table("profiles").select("*").eq("id", user_id).execute()
        data = response.data

        if data and len(data) > 0:
            user = data[0]
            # Îmbinăm first_name și last_name pentru aiService
            nume_complet = f"{user.get('first_name', '')} {user.get('last_name', '')}".strip()

            return {
                "username": nume_complet,
                "phone_number": user.get('phone_number', ''),
                "has_2fa": True,  # Login-ul OTP prin telefon acționează ca 2FA
                "days_active": 4
            }
        return None
    except Exception as e:
        print(f"Eroare conexiune Supabase: {e}")
        return None