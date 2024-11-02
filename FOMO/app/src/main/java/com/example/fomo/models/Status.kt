package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Status(
  @SerialName("id") val id: Long,                             // Primary key for the status
  @SerialName("created_at") val createdAt: String,    // Timestamp for when the status was created
  @SerialName("description") val description: String,   // Status' description
  @SerialName("emoji") val emoji: String,          // Status' emoji
)
