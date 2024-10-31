package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Status(
  @SerialName("id") val id: Long,                                // Primary key for the user
  @SerialName("created_at") val createdAt: String?,              // Timestamp for when the user was created
  @SerialName("description") val email: String?,                       // User's email address
  @SerialName("emoji") val displayName: String?,          // User's display name
)
