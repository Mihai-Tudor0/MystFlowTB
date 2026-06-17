# Raport: Utilizarea Instrumentelor AI în Dezvoltare

În dezvoltarea proiectului **MystFlow**, instrumentele bazate pe Inteligență Artificială (LLM) au avut un rol central atât în arhitectura aplicației (integrarea AI-ului ca funcționalitate), cât și în asistența de scriere a codului.

## 1. Asistență la Scriere Cod (Copilot / LLM Agents)
Am folosit agenți AI precum **ChatGPT** și **Google Gemini** (modulul Antigravity) ca pair-programmers pe tot parcursul dezvoltării:

* **Generarea Interfeței Grafice (UI):** Pentru a crea aspectul "premium" al aplicației (ex: animația 3D de tip FlipCard, gradientul întunecat și Carousel-ul multi-card). Aceste structuri complexe în Jetpack Compose ar fi necesitat mult timp de iterație manuală. AI-ul a propus direct structuri cu `graphicsLayer { rotationY }` și `HorizontalPager`.
* **Refactorizarea Bazei de Date (Supabase SQL):** Când s-a decis trecerea de la un model simplu (o balanță per utilizator) la un model multi-card, am folosit AI pentru a scrie direct scriptul SQL `supabase_setup.sql`. AI-ul s-a ocupat de conceperea corectă a Foreign Key-urilor și de scrierea funcțiilor RPC (Remote Procedure Calls) precum `top_up` și `perform_transfer` în limbajul PL/pgSQL.
* **Rezolvarea Conflictelor (Git):** În timpul unui merge conflict cauzat de integrarea ramurii de `ai_features` creată de colegul meu cu ramura de `ui_fixes`, AI-ul a analizat cele două coduri din `.kt` și a dedus faptul că s-a schimbat tipul variabilei din `Int` în `String` (UUID), ajutându-ne să corectăm apelurile metodei `fetchInsight`.

## 2. Agenții AI integrați în aplicație
Pentru a satisface punctajul de evaluare (minim 2 agenți), proiectul integrează funcționalități AI sub forma unor apeluri API:
1. **Agentul de Chat (MystBot):** Răspunde interogărilor din interfață și oferă sfaturi de asistență generală. 
2. **Agentul de Clasificare / Securitate (Security Insights & Transactions):** Un agent care analizează tranzacțiile sau comportamentul contului pentru a sugera insights proactive, funcționând în fundal.

## 3. Productivitate
Impactul principal al acestor instrumente a fost o reducere semnificativă a timpului de dezvoltare pe partea de scriere de SQL și a design-ului de interfață, precum și detectarea rapidă a warning-urilor și deprecation-urilor din versiunile noi de biblioteci Android (ex: trecerea de la icoanele default la cele cu prefix `AutoMirrored`).
