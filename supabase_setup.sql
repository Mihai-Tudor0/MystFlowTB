-- ============================================================
--  MystFlowTB — Supabase Database Setup (Updated for OTP Flow)
--  Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- ⚠️  If you already have an old `profiles` table with `name`/`surname` columns,
--     uncomment the line below to drop it first, then re-run this script.
-- DROP TABLE IF EXISTS public.profiles;

-- 1. Create the profiles table
CREATE TABLE public.profiles (
    -- Link the profile ID to the Supabase Auth user ID
    id           UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    first_name   TEXT NOT NULL,
    last_name    TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT now()
);

-- 2. Enable Row Level Security (required by Supabase)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 3. Allow users to insert their own profile during Sign Up
CREATE POLICY "Users can insert their own profile"
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = id);

-- 4. Allow users to read their own profile
CREATE POLICY "Users can read own profile"
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (auth.uid() = id);

-- 5. Allow users to update their own profile
CREATE POLICY "Users can update own profile"
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- ============================================================
-- IMPORTANT SETUP INSTRUCTIONS FOR DEMO:
-- ============================================================
-- 1. Go to Authentication -> Providers
-- 2. Enable "Phone"
-- 3. To avoid SMS costs during development, add Test Phone Numbers:
--    - Test Phone: +40700000001
--    - Test OTP: 123456
--    - Test Phone: +40700000002
--    - Test OTP: 123456
-- 
-- With these test numbers, Supabase will NOT send an SMS.
-- Simply type "+40700000001" and "123456" in the app to log in.
-- ============================================================
