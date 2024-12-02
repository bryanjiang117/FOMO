package com.example.fomo.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
  @SerialName("id") val id: Long? = null,                                 // Primary key
  @SerialName("created_at") val createdAt: String, // Timestamp for creation date
  @SerialName("name") val name: String,
  @SerialName("creator_id") val creatorId: String,  // ID of the user who created the group
)
