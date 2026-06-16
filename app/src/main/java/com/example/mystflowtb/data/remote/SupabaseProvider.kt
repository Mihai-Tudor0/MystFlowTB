package com.example.mystflowtb.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton Supabase client — configured with Auth (Phone OTP) and Postgrest.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  SUPABASE SETUP REQUIRED BEFORE RUNNING THE APP:            │
 * │                                                              │
 * │  1. Paste your Project URL and Anon Key below.               │
 * │  2. In Supabase Dashboard → Authentication → Providers:      │
 * │     • Enable "Phone" provider.                               │
 * │  3. For testing WITHOUT real SMS costs, add Test Phone        │
 * │     Numbers in Authentication → Phone:                       │
 * │     • Phone: +40700000001   OTP: 123456                      │
 * │     • Phone: +40700000002   OTP: 123456                      │
 * │     These numbers will NOT send real SMS — Supabase           │
 * │     accepts the fixed OTP code directly.                     │
 * └──────────────────────────────────────────────────────────────┘
 */
object SupabaseProvider {

    // ⚠️  PASTE YOUR OWN CREDENTIALS BELOW
    private const val SUPABASE_URL = "https://hnrvvndgnlobbcgfvcia.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_nOEVjDqbiSMGvEbe9Av6ZA_wd8Dgosu"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            // Auth module — handles Phone OTP sign-in/sign-up + session management
            install(Auth)

            // Postgrest module — for the 'profiles' table (first_name, last_name, phone)
            install(Postgrest)
        }
    }
}
