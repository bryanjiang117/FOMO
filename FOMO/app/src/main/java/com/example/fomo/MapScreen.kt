package com.example.fomo

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState


class MapScreen : Screen {
  @Composable
  override fun Content() {
    val viewState: MapViewModel = viewModel()
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      Map(viewState)
    }
  }
}

@Composable
fun Map(viewState: MapViewModel) {
  var isMapLoaded by remember { mutableStateOf(false) }
  Box(modifier = Modifier.fillMaxSize()) {
    val cameraPositionState = rememberCameraPositionState {
      position = CameraPosition.fromLatLngZoom(viewState.center, 15f)
    }
    Log.d("MapDebug", "API key is: ${BuildConfig.GOOGLE_MAPS_API_KEY}")
    Log.d("MapDebug", "Center is: ${viewState.center}")

    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      onMapLoaded = {
        isMapLoaded = true
        Log.d("MapDebug", "Map loaded successfully")},
    ) {
      if (isMapLoaded) {
        val homePosition = LatLng(viewState.userLatitude, viewState.userLongitude)  // Create LatLng object
        Marker(
          state = rememberMarkerState(key = "Home", homePosition),
          title = "Your Location",
          snippet = "This is where you are",
        )
      }
    }



    // ...
  }
}
