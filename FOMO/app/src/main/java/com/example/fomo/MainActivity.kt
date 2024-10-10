package com.example.fomo

// Voyager Navigator
// Theme
import android.Manifest
import android.location.Location
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.example.fomo.ui.theme.FOMOTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.Q)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val fineLocationGranted = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val backgroundLocationGranted = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationPermissionsAlreadyGranted = fineLocationGranted && coarseLocationGranted

    enableEdgeToEdge()
    setContent {
      FOMOTheme {
        LocationChecker(
          foregroundPermissionsGranted = locationPermissionsAlreadyGranted,
          backgroundPermissionGranted = backgroundLocationGranted
        )
        NavigatorFun()

      }
    }
  }
}

@Composable
fun LocationChecker(
  foregroundPermissionsGranted: Boolean,
  backgroundPermissionGranted: Boolean
){
  val context = LocalContext.current

  val locationPermissions = mutableListOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
  )

  var requestBackgroundPermission by remember { mutableStateOf(false) }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions: Map<String, Boolean> ->
      permissions.forEach { (permission, isGranted) ->
        println("Permission: $permission is granted: $isGranted")
      }
      val foregroundGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
              permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

      if (foregroundGranted && !backgroundPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        requestBackgroundPermission = true
      }
    }
  )

  LaunchedEffect(Unit) {
    if (!foregroundPermissionsGranted) {
      try {
        locationPermissionLauncher.launch(locationPermissions.toTypedArray())
      } catch (e: Exception) {
        println("Error requesting permissions")
        e.printStackTrace()
      }
    } else if (!backgroundPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      requestBackgroundPermission = true
    } else {
      getPreciseLocation(context)
    }
  }

  // Trigger the background location permission request in a proper composable context
  if (requestBackgroundPermission) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      RequestBackgroundLocationPermission()
      getPreciseLocation(context)
    }
  }
}


@SuppressLint("MissingPermission")
fun getPreciseLocation(context: Context){
  val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
  fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
    if (location != null) {
      val latitude = location.latitude
      val longitude = location.longitude
      Log.d("PreciseLocation", "Latitude: $latitude, Longitude: $longitude")
      println("Latitude: $latitude, Longitude: $longitude")
    } else {
      Log.d("PreciseLocation", "Location is null, unable to retrieve location.")
      println("Location is null, unable to retrieve location.")
    }
  }.addOnFailureListener { exception ->
    Log.e("PreciseLocation", "Failed to get location: ${exception.message}")
    println("Failed to get location: ${exception.message}")
  }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun RequestBackgroundLocationPermission() {
  var launcherInitialized by remember { mutableStateOf(false) }

  val backgroundPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { isGranted: Boolean ->
      if (isGranted) {
        println("Background location permission was granted")
      } else {
        println("Background location permission was denied")
      }
    }
  )

  LaunchedEffect(Unit) {
    launcherInitialized = true
  }

// Correct usage for a single permission request
  if (launcherInitialized) {
    try {
      backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

@Composable
fun NavigatorFun(){
  Navigator(MapScreen()) { Content() }
}

@Composable
fun Content() {
  val myViewModel: MyViewModel = viewModel()

  Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = { Navbar(viewModel = myViewModel) }) {
      innerPadding ->
    // Pass the innerPadding as a modifier to the Content
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          CurrentScreen() // Show the current screen
        }
  }
}

@Composable
fun State(state: String, modifier: Modifier = Modifier) {
  Text(state, modifier = modifier)
}

@Composable
fun Navbar(viewModel: MyViewModel) {
  val navigator = LocalNavigator.current
  Row(
      modifier = Modifier.fillMaxWidth(),
  ) {
    Button(onClick = { navigator?.push(MapScreen()) }, modifier = Modifier.weight(1f)) {
      Text("Map")
    }
    Button(onClick = { navigator?.push(FriendsScreen()) }, modifier = Modifier.weight(1f)) {
      Text("Friends")
    }
    Button(onClick = { navigator?.push(ProfileScreen()) }, modifier = Modifier.weight(1f)) {
      Text("Profile")
    }
    Button(onClick = { navigator?.push(SettingsScreen()) }, modifier = Modifier.weight(1f)) {
      Text("Settings")
    }
  }
}
