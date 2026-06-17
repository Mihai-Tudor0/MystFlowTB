# Design Patterns (Șabloane de Proiectare)

Aplicația **MystFlow** utilizează mai multe șabloane arhitecturale și de proiectare pentru a menține un cod curat, testabil și modular.

## 1. MVVM (Model-View-ViewModel)
Acesta este șablonul arhitectural de bază recomandat de Google pentru Android.
* **Model:** Clasele de date (ex: `Card`, `Transaction`, `UserProfile`) care reprezintă structura datelor din baza de date.
* **View:** Interfețele scrise în Jetpack Compose (ex: `HomeScreen`, `TransferScreen`). Ele doar afișează date și trimit evenimentele (click-uri) către ViewModel.
* **ViewModel:** Componenta care ține starea UI-ului (ex: `BankingViewModel`, `AiViewModel`). Păstrează lista de carduri și expune fluxuri (StateFlow) care se actualizează reactiv pe măsură ce datele se schimbă.

## 2. Singleton
Acest pattern este folosit pentru a ne asigura că o singură instanță globală este creată pentru anumite utilitare greoaie de instanțiat.
* **SupabaseProvider:** Creează o singură instanță (client) către baza de date Supabase pe tot parcursul vieții aplicației.
* **ApiClient (Retrofit):** Avem un singur obiect Retrofit global folosit pentru apelurile HTTP către serverul LLM/AI, economisind memorie și conexiuni.

## 3. Repository Pattern
Utilizat pentru a ascunde logica de preluare a datelor de restul aplicației.
* **AuthRepository:** Controlează logic cum se realizează înregistrarea (înregistrează user-ul, apoi creează un profil în DB, apoi generează primul card cu CVV). `AuthViewModel` nu trebuie să știe despre toate aceste apeluri succesive, el doar apelează funcția din Repository.

## 4. Observer Pattern (prin Kotlin Flows / StateFlow)
In inima framework-ului Jetpack Compose, folosim Observabile pentru a actualiza automat interfața fără să setăm noi datele manual.
* Când `cards.value` se modifică în `BankingViewModel`, `HomeScreen` observă schimbarea folosind metoda `collectAsState()` și se randează (recompune) automat pentru a afișa noul card sau balanța actualizată.
