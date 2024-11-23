package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupLink(
  @SerialName("id") val id: Long? = null,                                 // Primary key
  @SerialName("created_at") val createdAt: String, // Timestamp for creation date
  @SerialName("user_uid") val userId: String,  // ID of the user
  @SerialName("group_id") val groupId: Long,    // ID of the group
  @SerialName("accepted") val accepted: Boolean       // Boolean indicating if the request was accepted
)
