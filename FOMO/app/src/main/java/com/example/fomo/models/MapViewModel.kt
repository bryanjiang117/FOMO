package com.example.fomo.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.maps.android.SphericalUtil

class MapViewModel : ViewModel() {
  var userLongitude by mutableDoubleStateOf(0.0)
  var userLatitude by mutableDoubleStateOf(0.0)
  var center by mutableStateOf(LatLng(43.4723, -80.5449))

  var pac = LatLng(43.47221980317695, -80.54570549190352)

  fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    return SphericalUtil.computeDistanceBetween(point1, point2)
  }

  fun updateUserLocation(latitude: Double, longitude: Double) {
    userLongitude = longitude
    userLatitude = latitude

    center = LatLng(latitude, longitude)

    Log.d("distance calc: ", calculateDistance(pac, center).toString())

    Log.d("Map View Model Location Update", "Latitude: $latitude, Longitude: $longitude")
  }


}