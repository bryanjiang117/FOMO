package com.example.fomo

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.example.fomo.const.Colors
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.fomo.models.MyViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState


class MapScreen(private val myViewModel: MyViewModel) : Screen {
  @Composable
  override fun Content() {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      Map(myViewModel)
    }
  }
}

@Composable
fun Map(myViewModel: MyViewModel) {
  var isMapLoaded by remember { mutableStateOf(false) }
  val friendList = myViewModel.friendsList
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(myViewModel.center, 15f)
  }
  val markerState = rememberMarkerState(
    key = "Selected Location",
  )

  // Update camera position whenever mapViewModel.center changes
  LaunchedEffect(myViewModel.center) {
    cameraPositionState.animate(
      CameraUpdateFactory.newLatLngZoom(myViewModel.center, 15f),
      1000 // Optional animation duration in milliseconds
    )
  }

  LaunchedEffect(myViewModel.selectedLocation) {
    if (myViewModel.selectedLocation != null) {
      markerState.position = myViewModel.selectedLocation!!
      markerState.showInfoWindow()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Log.d("MapDebug", "API key is: ${BuildConfig.GOOGLE_MAPS_API_KEY}")
    Log.d("MapDebug", "Center is: ${myViewModel.center}")

    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      onMapLoaded = {
        isMapLoaded = true
        Log.d("MapDebug", "Map loaded successfully")
      },
      onMapClick = { coords ->
        myViewModel.onMyWay(coords)
      }
    ) {
      if (isMapLoaded) {
        val userPosition = LatLng(myViewModel.userLatitude, myViewModel.userLongitude)  // Create LatLng object

        Marker(
          state = rememberMarkerState(key = "User Position", userPosition),
          title = myViewModel.displayName,
          snippet = "${myViewModel.status.emoji} ${myViewModel.status.description}",
        )

        // display friends
        for(friend in friendList) {
          val friendLocation = LatLng(friend.latitude, friend.longitude)
          val friendStatus = myViewModel.statusList.filter {it.id == friend.status_id}[0]
          val friendMarkerState = rememberMarkerState(
            key = "friend" + friend.id.toString(),
            position = friendLocation
          ).apply {
            showInfoWindow() // This ensures the info window is displayed when the marker is rendered
          }
          Marker(
            state = friendMarkerState,
            title = friend.displayName,
            snippet = "${friendStatus.emoji} ${friendStatus.description}",
          )
        }

        if (myViewModel.selectedLocation != null) {
          Marker(
            state = markerState,
            title = "On my way",
          )
          markerState.showInfoWindow()
          if (myViewModel.routePoints != null) {
            Polyline(
              points = myViewModel.routePoints!!,
              color = Color.Blue,
              width = 10f,
            )
          }
        }

      }
    }

    Card(
      modifier = Modifier
        .align(Alignment.TopEnd) // Position card at the top center
        .padding(16.dp), // Padding from the top or screen edges
      colors = CardDefaults.cardColors(
        containerColor = Color.White
      ),
      shape = RoundedCornerShape(24.dp),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        Text(
          text = "${myViewModel.status.description} ${myViewModel.status.emoji}",
          color = Color.Black,
        )
//        Text(
//          text = "MC",
//          color = Color.Black,
//        )
      }
    }
  }

}
