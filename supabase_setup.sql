-- ============================================================
--  MystFlowTB — Supabase Database Setup (Multiple Cards & History)
--  Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- ⚠️  WARNING: THIS WILL DELETE EXISTING DATA to apply the new architecture.
DROP TABLE IF EXISTS public.transactions;
DROP TABLE IF EXISTS public.cards;
DROP TABLE IF EXISTS public.profiles;

-- ============================================================
-- 1. PROFILES TABLE
-- ============================================================
CREATE TABLE public.profiles (
    id           UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    first_name   TEXT NOT NULL,
    last_name    TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert their own profile"
    ON public.profiles FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can read own profile"
    ON public.profiles FOR SELECT TO authenticated
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- ============================================================
-- 2. CARDS TABLE
-- ============================================================
CREATE TABLE public.cards (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    card_number   TEXT UNIQUE NOT NULL,
    cvv           TEXT NOT NULL,
    expiry_date   TEXT NOT NULL,
    balance       NUMERIC(12,2) DEFAULT 0.00,
    is_active     BOOLEAN DEFAULT true,
    created_at    TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.cards ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own cards"
    ON public.cards FOR SELECT TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own cards"
    ON public.cards FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Note: We do NOT allow users to directly UPDATE their balance. Only RPCs can do that.

-- ============================================================
-- 3. TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE public.transactions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id          UUID REFERENCES public.profiles(id),       -- NULL for top-ups
    receiver_id        UUID NOT NULL REFERENCES public.profiles(id),
    sender_card_id     UUID REFERENCES public.cards(id),          -- NULL for top-ups
    receiver_card_id   UUID REFERENCES public.cards(id),
    sender_name        TEXT, -- Stored historically
    receiver_name      TEXT, -- Stored historically
    amount             NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    type               TEXT NOT NULL CHECK (type IN ('top_up', 'transfer')),
    description        TEXT,
    created_at         TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own transactions"
    ON public.transactions FOR SELECT TO authenticated
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Only RPCs insert transactions directly (for atomicity and security).
-- We can add a generic insert policy if needed, but keeping it strict is better.

-- ============================================================
-- 4. TOP-UP FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION public.top_up(p_card_id UUID, p_amount NUMERIC)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_id UUID;
    v_receiver_name TEXT;
BEGIN
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be positive';
    END IF;

    -- Verify card belongs to caller
    SELECT user_id INTO v_user_id
    FROM public.cards
    WHERE id = p_card_id;

    IF v_user_id IS NULL OR v_user_id != auth.uid() THEN
        RAISE EXCEPTION 'Card not found or does not belong to user';
    END IF;

    -- Get receiver name
    SELECT first_name || ' ' || last_name INTO v_receiver_name
    FROM public.profiles
    WHERE id = auth.uid();

    -- Add balance
    UPDATE public.cards
    SET balance = balance + p_amount
    WHERE id = p_card_id;

    -- Record the transaction
    INSERT INTO public.transactions (
        sender_id, receiver_id, sender_card_id, receiver_card_id, 
        sender_name, receiver_name, amount, type, description
    )
    VALUES (
        NULL, auth.uid(), NULL, p_card_id, 
        'External Card', v_receiver_name, p_amount, 'top_up', 'Alimentare din exterior'
    );
END;
$$;

-- ============================================================
-- 5. TRANSFER FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION public.perform_transfer(
    p_from_card_id UUID,
    p_recipient_card_number TEXT,
    p_amount NUMERIC
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_sender_id UUID;
    v_receiver_id UUID;
    v_to_card_id UUID;
    v_sender_balance NUMERIC;
    v_sender_name TEXT;
    v_receiver_name TEXT;
BEGIN
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be positive';
    END IF;

    v_sender_id := auth.uid();

    -- Check if sender card is valid and belongs to sender
    SELECT balance INTO v_sender_balance
    FROM public.cards
    WHERE id = p_from_card_id AND user_id = v_sender_id
    FOR UPDATE;

    IF v_sender_balance IS NULL THEN
        RAISE EXCEPTION 'Sender card not found or unauthorized';
    END IF;

    IF v_sender_balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient balance on selected card. Available: % RON', v_sender_balance;
    END IF;

    -- Look up receiver card
    SELECT id, user_id INTO v_to_card_id, v_receiver_id
    FROM public.cards
    WHERE card_number = p_recipient_card_number;

    IF v_to_card_id IS NULL THEN
        RAISE EXCEPTION 'Recipient card number not found';
    END IF;

    IF p_from_card_id = v_to_card_id THEN
        RAISE EXCEPTION 'Cannot transfer to the exact same card';
    END IF;

    -- Get Names
    SELECT first_name || ' ' || last_name INTO v_sender_name
    FROM public.profiles WHERE id = v_sender_id;

    SELECT first_name || ' ' || last_name INTO v_receiver_name
    FROM public.profiles WHERE id = v_receiver_id;

    -- Deduct from sender
    UPDATE public.cards
    SET balance = balance - p_amount
    WHERE id = p_from_card_id;

    -- Add to receiver
    UPDATE public.cards
    SET balance = balance + p_amount
    WHERE id = v_to_card_id;

    -- Record the transaction
    INSERT INTO public.transactions (
        sender_id, receiver_id, sender_card_id, receiver_card_id,
        sender_name, receiver_name, amount, type, description
    )
    VALUES (
        v_sender_id, v_receiver_id, p_from_card_id, v_to_card_id,
        v_sender_name, v_receiver_name, p_amount, 'transfer', 'Transfer de bani'
    );
END;
$$;

-- ============================================================
-- 6. LOOKUP FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION public.lookup_card(p_card_number TEXT)
RETURNS TABLE(first_name TEXT, last_name TEXT, card_number TEXT)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT p.first_name, p.last_name, c.card_number
    FROM public.cards c
    JOIN public.profiles p ON c.user_id = p.id
    WHERE c.card_number = p_card_number;
END;
$$;

-- ============================================================
-- IMPORTANT SETUP INSTRUCTIONS:
-- ============================================================
-- You MUST re-run this entire script in Supabase SQL Editor.
-- It will wipe out old data to apply the new schema.
