package com.example.mystflowtb.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to the `transactions` table in Supabase.
 *
 * Records every top-up and transfer operation.
 * - type = "top_up":   sender_id is null, receiver_id is the user who topped up
 * - type = "transfer": sender_id sends money to receiver_id
 */
@Serializable
data class Transaction(
    val id: String? = null,

    @SerialName("sender_id")
    val senderId: String? = null,

    @SerialName("receiver_id")
    val receiverId: String,

    val amount: Double,

    /** "top_up" or "transfer" */
    val type: String,

    val description: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
