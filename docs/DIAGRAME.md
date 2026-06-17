# Diagrame UML și Workflow-uri

Acest document prezintă vizual modul de funcționare și arhitectura aplicației **MystFlow**, folosind standardul Mermaid.js (randat direct de GitHub).

## 1. Arhitectura Componentelor (Component Diagram)
Aplicația folosește modelul MVVM (Model-View-ViewModel) împreună cu un backend BaaS (Supabase).

```mermaid
graph TD
    A[UI / Jetpack Compose] -->|Observe State| B[ViewModels]
    A -->|User Actions| B
    B -->|Fetch/Mutate| C[Repositories]
    C -->|Network Calls| D[Supabase SDK / Retrofit]
    C -->|Local State| E[Room DB / DataStore]
    D -->|PostgreSQL RPC| F[(Supabase Database)]
    D -->|Auth Tokens| G[Supabase Auth]
    D -->|API Calls| H[LLM AI Service]
```

## 2. Diagrama de Stări: Tranzacție / Transfer (State Diagram)
Arată pașii prin care trece un utilizator când efectuează un transfer bancar.

```mermaid
stateDiagram-v2
    [*] --> EcranTransfer
    EcranTransfer --> ValidareSuma : Introducere sumă
    ValidareSuma --> EroareFonduri : Sumă > Balanță
    EroareFonduri --> EcranTransfer
    ValidareSuma --> ProcesareSupabase : Fonduri OK
    ProcesareSupabase --> Succes : RPC 'perform_transfer' reușit
    ProcesareSupabase --> EroareBackend : Eroare la server
    EroareBackend --> EcranTransfer
    Succes --> [*] : Update UI
```

## 3. Diagrama de Secvență: Autentificare (Sequence Diagram)
Descrie flow-ul de comunicare între client și server în timpul procesului de Login.

```mermaid
sequenceDiagram
    actor U as Utilizator
    participant UI as LoginScreen
    participant VM as AuthViewModel
    participant Repo as AuthRepository
    participant DB as Supabase Auth

    U->>UI: Introduce Nr. telefon
    U->>UI: Click Login
    UI->>VM: login(phone/PIN)
    VM->>Repo: signIn(phone/PIN)
    Repo->>DB: cerere de autentificare
    alt Credențiale Invalide
        DB-->>Repo: Eroare Auth
        Repo-->>VM: AuthResult.Failure
        VM-->>UI: Afișare Toast Eroare
    else Credențiale Valide
        DB-->>Repo: Returnează Session Token
        Repo-->>VM: AuthResult.Success
        VM-->>UI: Navigare către HomeScreen
    end
```
