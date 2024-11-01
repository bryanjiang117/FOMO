package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
  @SerialName("id") val id: Long,                                // Primary key for the user
  @SerialName("created_at") val createdAt: String?,              // Timestamp for when the user was created
  @SerialName("email") val email: String?,                       // User's email address
  @SerialName("display_name") val displayName: String,          // User's display name
  @SerialName("username") val username: String?,                 // Username
  @SerialName("password") val password: String?,                 // Password (consider encrypting if used in production)
  @SerialName("latitude") val latitude: Double,                 // User's latitude location
  @SerialName("longitude") val longitude: Double,               // User's longitude location
  @SerialName("status") val status: Int,                        // Status ID (potentially a foreign key)
  @SerialName("noti_nearby") val notiNearby: Boolean?,           // Notification setting for nearby events
  @SerialName("noti_status") val notiStatus: Boolean?,           // Notification setting for status updates
  @SerialName("noti_messages") val notiMessages: Boolean?        // Notification setting for messages
)
