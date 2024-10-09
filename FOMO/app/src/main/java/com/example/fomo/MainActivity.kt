package com.example.fomo

// Voyager Navigator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.example.fomo.ui.theme.FOMOTheme


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FOMOTheme {
        Navigator(MapScreen()) {
//          val myViewModel: MyViewModel = viewModel()
//          Scaffold(
//              modifier = Modifier.fillMaxSize(), bottomBar = { Navbar(viewModel = myViewModel) })
//          { innerPadding ->
//              CurrentScreen()
//          }
          CurrentScreen()
        }
      }
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
    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Friends") }
    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Profile") }
    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Settings") }
  }
}
