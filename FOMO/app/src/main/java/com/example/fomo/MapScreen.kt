package com.example.fomo

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.fomo.models.MapViewModel
import com.example.fomo.models.MyViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState


class MapScreen(private val myViewModel: MyViewModel, private val mapViewModel: MapViewModel) : Screen {
  @Composable
  override fun Content() {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      Map(mapViewModel, myViewModel)
    }
  }
}

@Composable
fun Map(mapViewModel: MapViewModel, myViewModel: MyViewModel) {
  var isMapLoaded by remember { mutableStateOf(false) }
  var friendList = myViewModel.friendsList

  // Camera position state, initialized with a default value
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(mapViewModel.center, 15f)
  }

  // Update camera position whenever mapViewModel.center changes
  LaunchedEffect(mapViewModel.center) {
    cameraPositionState.animate(
      CameraUpdateFactory.newLatLngZoom(mapViewModel.center, 15f),
      1000 // Optional animation duration in milliseconds
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Log.d("MapDebug", "API key is: ${BuildConfig.GOOGLE_MAPS_API_KEY}")
    Log.d("MapDebug", "Center is: ${mapViewModel.center}")

    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      onMapLoaded = {
        isMapLoaded = true
        Log.d("MapDebug", "Map loaded successfully")},
    ) {
      if (isMapLoaded) {
        val homePosition = LatLng(mapViewModel.userLatitude, mapViewModel.userLongitude)  // Create LatLng object
        Log.d("Home Position", homePosition.toString())

        Marker(
          state = rememberMarkerState(key = "Home", homePosition),
          title = myViewModel.displayName,
          snippet = "${myViewModel.activity.emoji} ${myViewModel.activity.name}",
        )

        for(friend in friendList) {
          val friendLocation = LatLng(friend.latitude, friend.longitude)
          val markerState = rememberMarkerState(
            key = "friend" + friend.id.toString(),
            position = friendLocation
          ).apply {
            showInfoWindow() // This ensures the info window is displayed when the marker is rendered
          }
          Marker(
            state = markerState,
            title = friend.displayName,
            snippet = "${myViewModel.activity.emoji} ${myViewModel.activity.name}",
          )
        }
      }
    }



    // ...
  }
}
