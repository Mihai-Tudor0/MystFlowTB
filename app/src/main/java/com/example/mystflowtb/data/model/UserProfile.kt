package com.example.mystflowtb.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to the `profiles` table in Supabase.
 *
 * This table stores user profile data (first_name, last_name, phone).
 * Authentication is handled entirely by Supabase Auth (Phone OTP) —
 * no passwords or PINs are stored in this table.
 *
 * The local 6-digit PIN is stored ONLY on-device (SharedPreferences).
 */
@Serializable
data class UserProfile(
    /** Matches auth.users.id — set after OTP verification */
    val id: String? = null,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("phone_number")
    val phoneNumber: String,

    @SerialName("created_at")
    val createdAt: String? = null
)
