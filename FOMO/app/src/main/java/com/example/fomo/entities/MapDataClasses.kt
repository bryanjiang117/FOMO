package com.example.fomo.entities

import kotlinx.serialization.Serializable

@Serializable
data class GeocodeResponse(
  val results: List<Result>,
  val status: String
)

@Serializable
data class Result(
  val place_id: String,
  val formatted_address: String
)

@Serializable
data class DirectionsResponse(
  val routes: List<Route>
)

@Serializable
data class Route(
  val legs: List<Leg>,
  val overview_polyline: Polyline
)

@Serializable
data class Leg(
  val distance: Distance,
  val duration: Duration
)

@Serializable
data class Distance(
  val text: String,
  val value: Int
)

@Serializable
data class Duration(
  val text: String,
  val value: Int
)

@Serializable
data class Polyline(
  val points: String
)

@Serializable
data class LatLng(
  val latitude: Double,
  val longitude: Double
)