-- ============================================================
--  MystFlowTB — Supabase Database Setup (Full Banking Schema)
--  Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- ⚠️  If you already have old tables, uncomment the lines below to drop them first.
-- DROP TABLE IF EXISTS public.transactions;
-- DROP TABLE IF EXISTS public.profiles;

-- ============================================================
-- 1. PROFILES TABLE
-- ============================================================
CREATE TABLE public.profiles (
    -- Link the profile ID to the Supabase Auth user ID
    id           UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    first_name   TEXT NOT NULL,
    last_name    TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    balance      NUMERIC(12,2) DEFAULT 0.00,
    card_number  TEXT UNIQUE,
    created_at   TIMESTAMPTZ DEFAULT now()
);

-- Enable Row Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Users can insert their own profile during Sign Up
CREATE POLICY "Users can insert their own profile"
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = id);

-- Users can read their own profile
CREATE POLICY "Users can read own profile"
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (auth.uid() = id);

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- Allow users to look up other profiles by card_number (for transfer preview)
-- This only exposes first_name, last_name, and card_number — not balance.
-- We use a security definer function for this instead (see below).

-- ============================================================
-- 2. TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE public.transactions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id     UUID REFERENCES auth.users(id),       -- NULL for top-ups
    receiver_id   UUID NOT NULL REFERENCES auth.users(id),
    amount        NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    type          TEXT NOT NULL CHECK (type IN ('top_up', 'transfer')),
    description   TEXT,
    created_at    TIMESTAMPTZ DEFAULT now()
);

-- Enable Row Level Security
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

-- Users can read their own transactions (sent or received)
CREATE POLICY "Users can read own transactions"
    ON public.transactions
    FOR SELECT
    TO authenticated
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Users can insert transactions (the perform_transfer function handles validation)
CREATE POLICY "Users can insert transactions"
    ON public.transactions
    FOR INSERT
    TO authenticated
    WITH CHECK (true);

-- ============================================================
-- 3. TOP-UP FUNCTION
-- ============================================================
-- Adds money to the caller's account and records it as a transaction.
CREATE OR REPLACE FUNCTION public.top_up(p_amount NUMERIC)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be positive';
    END IF;

    -- Add balance
    UPDATE public.profiles
    SET balance = balance + p_amount
    WHERE id = auth.uid();

    -- Record the transaction
    INSERT INTO public.transactions (sender_id, receiver_id, amount, type, description)
    VALUES (NULL, auth.uid(), p_amount, 'top_up', 'Top-up from external card');
END;
$$;

-- ============================================================
-- 4. TRANSFER FUNCTION
-- ============================================================
-- Atomically transfers money between two users by card number.
-- Validates sufficient balance and that the recipient exists.
CREATE OR REPLACE FUNCTION public.perform_transfer(
    p_recipient_card TEXT,
    p_amount NUMERIC
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_sender_id UUID;
    v_receiver_id UUID;
    v_sender_balance NUMERIC;
BEGIN
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be positive';
    END IF;

    v_sender_id := auth.uid();

    -- Look up receiver by card number
    SELECT id INTO v_receiver_id
    FROM public.profiles
    WHERE card_number = p_recipient_card;

    IF v_receiver_id IS NULL THEN
        RAISE EXCEPTION 'Recipient card number not found';
    END IF;

    IF v_sender_id = v_receiver_id THEN
        RAISE EXCEPTION 'Cannot transfer to yourself';
    END IF;

    -- Check sender balance
    SELECT balance INTO v_sender_balance
    FROM public.profiles
    WHERE id = v_sender_id
    FOR UPDATE;  -- Lock the row

    IF v_sender_balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient balance. Available: % RON', v_sender_balance;
    END IF;

    -- Deduct from sender
    UPDATE public.profiles
    SET balance = balance - p_amount
    WHERE id = v_sender_id;

    -- Add to receiver
    UPDATE public.profiles
    SET balance = balance + p_amount
    WHERE id = v_receiver_id;

    -- Record the transaction
    INSERT INTO public.transactions (sender_id, receiver_id, amount, type, description)
    VALUES (v_sender_id, v_receiver_id, p_amount, 'transfer', 'Transfer via card number');
END;
$$;

-- ============================================================
-- 5. LOOKUP FUNCTION (for transfer preview)
-- ============================================================
-- Allows looking up a profile by card number without full table access.
-- Returns only the name (not balance or other sensitive info).
CREATE OR REPLACE FUNCTION public.lookup_card(p_card_number TEXT)
RETURNS TABLE(first_name TEXT, last_name TEXT, card_number TEXT)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT p.first_name, p.last_name, p.card_number
    FROM public.profiles p
    WHERE p.card_number = p_card_number;
END;
$$;

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
