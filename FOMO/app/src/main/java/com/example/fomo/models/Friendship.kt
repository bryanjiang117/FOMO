package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Friendship(
  @SerialName("id") val id: Long? = null,                                 // Primary key
  @SerialName("created_at") val createdAt: String, // Timestamp for creation date
  @SerialName("requester_id") val requesterId: String,  // ID of the user who sent the request
  @SerialName("receiver_id") val receiverId: String,    // ID of the user receiving the request
  @SerialName("accept_date") val acceptDate: String? = null, // Timestamp for acceptance date
  @SerialName("accepted") val accepted: Boolean       // Boolean indicating if the request was accepted
)
