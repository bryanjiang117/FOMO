package com.example.fomo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import androidx.compose.runtime.mutableDoubleStateOf

class MapViewModel : ViewModel() {
  var userLongitude by mutableDoubleStateOf(0.0)
  var userLatitude by mutableDoubleStateOf(0.0)
  var center = LatLng(43.4723, -80.5449)

  fun updateUserLocation(latitude: Double, longitude: Double) {
    userLongitude = longitude
    userLatitude = latitude
    center = LatLng(latitude, longitude)

    Log.d("Map View Model Location Update", "Latitude: $latitude, Longitude: $longitude")
  }


}