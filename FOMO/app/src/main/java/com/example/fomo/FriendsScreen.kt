package com.example.fomo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import android.util.Log
import com.example.fomo.models.MapViewModel
import com.example.fomo.models.MyViewModel
import com.google.android.gms.maps.model.LatLng

class FriendsScreen(private val myViewModel: MyViewModel, private val mapViewModel: MapViewModel) : Screen {
  var friendsList = myViewModel.friendsList
  var myLocation = mapViewModel.center
  @Composable
  override fun Content() {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("current userId:" + myViewModel.userId.toString())
      Text("Friends List:")

      for(friend in friendsList) {
        val friendLocation = LatLng(friend.latitude, friend.longitude)
        val distance = mapViewModel.calculateDistance(friendLocation, myLocation)

        Text(friend.displayName + ": " + distance.toInt().toString() + "m away")
      }
    }
  }
}