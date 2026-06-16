package com.example.mystflowtb.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the `lookup_card` RPC function.
 * Contains only non-sensitive profile information for transfer preview.
 */
@Serializable
data class CardLookupResult(
    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("card_number")
    val cardNumber: String
)
