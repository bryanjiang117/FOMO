package com.example.fomo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.example.fomo.models.MyViewModel
import com.example.fomo.models.User
import com.example.fomo.const.Colors
import com.example.fomo.ui.theme.FOMOTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
  private val myViewModel: MyViewModel by viewModels()
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  @RequiresApi(Build.VERSION_CODES.Q)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      myViewModel.fetchDatabase()
    }

    val backgroundLocationGranted = LocationHelper.isBackgroundLocationPermissionGranted(this)
    val locationPermissionsAlreadyGranted = LocationHelper.areLocationPermissionsGranted(this)

    enableEdgeToEdge()
    setContent {
      val myViewModel: MyViewModel = viewModel()

      FOMOTheme {
        LocationHelper.LocationChecker(locationPermissionsAlreadyGranted, backgroundLocationGranted, myViewModel,this)
        NavigatorFun(myViewModel = myViewModel)
      }
    }

    updateData()
  }

  private fun updateData() {
    coroutineScope.launch {
      while (isActive) {
        myViewModel.fetchFriends()
        delay(20000)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    coroutineScope.cancel()
  }
}

@Composable
fun NavigatorFun(myViewModel: MyViewModel) {
  Navigator(SignIn(myViewModel)) { Content(myViewModel) }
}

@Composable
fun Content(myViewModel: MyViewModel) {
  if (myViewModel.signedIn) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = { Navbar(viewModel = myViewModel) }
    ) {
        innerPadding ->
      // Pass the innerPadding as a modifier to the Content
      Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        CurrentScreen() // Show the current screen
      }
    }
  } else {
    CurrentScreen() // Show the current screen
  }
}

@Composable
fun State(state: String, modifier: Modifier = Modifier) {
  Text(state, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(viewModel: MyViewModel) {
  val navigator = LocalNavigator.current
  val items: Array<Pair<() -> Unit, @Composable () -> Unit>> = arrayOf(
    Pair({ navigator?.push(MapScreen(viewModel)) }, {Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Map Icon")}),
    Pair({ navigator?.push(FriendsScreen(viewModel)) }, {Icon(imageVector = Icons.Default.People, contentDescription = "Friends Icon")}),
    Pair({ navigator?.push(ProfileScreen(viewModel)) }, {Icon(imageVector = Icons.Default.Person, contentDescription = "Profile Icon")}),
    Pair({ navigator?.push(SettingsScreen(viewModel)) }, {Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings Icon")}),
  )
  CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
      for ((onClick, icon) in items) {
        Button(
          onClick = onClick,
          modifier = Modifier.weight(1f),
          shape = RectangleShape,
          contentPadding = PaddingValues(top = 25.dp, bottom = 25.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Colors.primary)
        ) {
          icon()
        }
      }
    }
  }
}
