package com.example.fomo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
  @SerialName("latitude") val latitude: Double,
  @SerialName("longitude") val longitude: Double,
  @SerialName("radius") val radius: Int,
)
