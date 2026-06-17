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

    @SerialName("sender_card_id")
    val senderCardId: String? = null,

    @SerialName("receiver_card_id")
    val receiverCardId: String? = null,

    @SerialName("sender_name")
    val senderName: String? = null,

    @SerialName("receiver_name")
    val receiverName: String? = null,

    val amount: Double,

    /** "top_up" or "transfer" */
    val type: String,

    val description: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
