package com.example.fomo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen

class ProfileScreen : Screen {
  @Composable
  override fun Content() {
    val myViewModel: MyViewModel = viewModel()
    var showDialog by remember { mutableStateOf(false) }  // popup state
    var newDisplayName by remember { mutableStateOf("") } // updated name state

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.Top
    ) {
      // Row to display the current name and the "Change" button on the same line
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Display Name: ${myViewModel.displayName}",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f) // Push the text to the left
        )

        TextButton(
          onClick = { showDialog = true }
        ) {
          Text("Change", color = MaterialTheme.colorScheme.primary)
        }
      }

      if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("Enter New Display Name")

              Spacer(modifier = Modifier.height(8.dp))

              // TextField to input new display name
              OutlinedTextField(
                value = newDisplayName,
                onValueChange = { newDisplayName = it },
                label = { Text("New Display Name") },
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(16.dp))

              Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
              ) {
                TextButton(onClick = { showDialog = false }) {
                  Text("Cancel")
                }
                TextButton(onClick = {  //save button
                  myViewModel.updateDisplayName(newDisplayName)
                  showDialog = false
                }) {
                  Text("Save")
                }
              }
            }
          }
        }
      }
    }
  }
}