package com.example.mystflowtb.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("card_number")
    val cardNumber: String,

    val cvv: String,

    @SerialName("expiry_date")
    val expiryDate: String,

    val balance: Double = 0.0,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String? = null
)
