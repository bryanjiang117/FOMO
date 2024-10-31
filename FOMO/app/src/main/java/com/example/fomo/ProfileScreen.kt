package com.example.fomo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.voyager.core.screen.Screen
import com.example.fomo.const.activities
import com.example.fomo.models.MyViewModel

class ProfileScreen(private val myViewModel: MyViewModel) : Screen {
  @Composable
  override fun Content() {
    var showDialog by remember { mutableStateOf(false) }  // popup state
    var droppedDown by remember { mutableStateOf(false) } // display dropdown
    var newDisplayName by remember { mutableStateOf("") } // updated name state

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.Top
    ) {
      Text(text = "My Profile",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 64.dp))


      Spacer(modifier = Modifier.height(16.dp))

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

      // Row to display status and the "Change" button on the same line
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {

        Text(
          text = "Current Activity: ",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f) // Push the text to the left
        )

        Box(
        ) {

          // Main button to trigger the dropdown
          Button(
            onClick = { droppedDown = !droppedDown },
            modifier = Modifier.width(200.dp))
          {
            Text(text = "${myViewModel.activity.emoji} ${myViewModel.activity.name}")
          }

          // Dropdown menu items
          DropdownMenu(
            expanded = droppedDown,
            onDismissRequest = { droppedDown = false },
            modifier = Modifier
              .width(200.dp)
              .heightIn(max = 400.dp) // Set max height of the dropdown
              .background(Color.LightGray)
              .padding(8.dp)
              .background(Color.White, shape = RoundedCornerShape(10.dp)),
            offset = DpOffset(x = 0.dp, y = 0.dp),
            properties = PopupProperties(focusable = true)
          ) {
            activities.forEach { activity ->
              DropdownMenuItem(
                text = { Text(text = "${activity.emoji} ${activity.name}") },
                onClick = {
                myViewModel.updateActivity(activity)
                droppedDown = false
              })
            }
          }
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