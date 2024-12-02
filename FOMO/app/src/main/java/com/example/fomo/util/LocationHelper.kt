// LocationHelper.kt
package com.example.fomo.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.fomo.viewmodel.MyViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationHelper {


  // Check if fine and coarse location permissions are granted
  fun areLocationPermissionsGranted(context: Context): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
      context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = ContextCompat.checkSelfPermission(
      context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineLocationGranted && coarseLocationGranted
  }

  // Check if background location permission is granted (for Android Q and above)
  fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  @Composable
  fun LocationChecker(
    foregroundPermissionsGranted: Boolean,
    backgroundPermissionGranted: Boolean,
    mapViewModel: MyViewModel,
    context: Context
  ) {
    val locationPermissions =
      mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    var requestBackgroundPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions: Map<String, Boolean> ->
          permissions.forEach { (permission, isGranted) ->
            println("Permission: $permission is granted: $isGranted")
          }
          val foregroundGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

          if (foregroundGranted &&
            !backgroundPermissionGranted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundPermission = true
          } else if (foregroundGranted){
            getPreciseLocation(context, mapViewModel)
          }
        })

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
        getPreciseLocation(context, mapViewModel)
      }
    }

    // Trigger the background location permission request in a proper composable context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        RequestBackgroundLocationPermission(requestBackgroundPermission, context)
    }
  }

  fun getPreciseLocation(context: Context, viewState: MyViewModel) {
    val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        6000
      ).apply {
        setMaxUpdates(1) // only do it once
      }.build()

      val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          val location = locationResult.lastLocation
          if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            viewState.updateUserLocation(latitude, longitude)
            Log.d("PreciseLocation", "Updated location: ($latitude, $longitude)")
          }
          fusedLocationClient.removeLocationUpdates(this)
        }
      }

      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        null
      )
    } else {
      Log.e("getPreciseLocation", "Access Fine Location Permission Not Granted")
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  @Composable
  fun RequestBackgroundLocationPermission(requestBackgroundPermission: Boolean, context: Context) {

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
      onResult = { isGranted: Boolean ->
        if (isGranted) {
          println("Background location permission was granted")
        } else {
          println("Background location permission was denied")
        }
      })

    LaunchedEffect(requestBackgroundPermission) {
      // Only request if permission is not already granted
      if (requestBackgroundPermission &&
        ContextCompat.checkSelfPermission(
          context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        try {
          backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
