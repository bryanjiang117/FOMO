package com.example.fomo

import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.voyager.core.screen.Screen
import com.example.fomo.models.MyViewModel

class ProfileScreen(private val myViewModel: MyViewModel) : Screen {
  @Composable
  override fun Content() {
    var showDialog by remember { mutableStateOf(false) }  // popup state
    var toChange by remember { mutableStateOf("null") } // what to change
    var droppedDown by remember { mutableStateOf(false) } // display dropdown
    var newValue by remember { mutableStateOf("") } // updated name state

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(text = "Profile",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ){
        Image(
          painter = painterResource(id = R.drawable.placeholder_pfp),
          contentDescription = "Profile Picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(125.dp)
            .clip(CircleShape)
        )

      }

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
          onClick = {
            showDialog = true
            toChange = "Display Name"
          }
        ) {
          Text("Change", color = MaterialTheme.colorScheme.primary)
        }
      }

      // Row to display the current name and the "Change" button on the same line
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Email: ${myViewModel.email}",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f) // Push the text to the left
        )

        TextButton(
          onClick = {
            showDialog = true
            toChange = "Email"
          }
        ) {
          Text("Change", color = MaterialTheme.colorScheme.primary)
        }
      }

      // Row to display the current name and the "Change" button on the same line
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Username: ${myViewModel.username}",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f) // Push the text to the left
        )

        TextButton(
          onClick = {
            showDialog = true
            toChange = "Username"
          }
        ) {
          Text("Change", color = MaterialTheme.colorScheme.primary)
        }
      }

      // Row to display the current name and the "Change" button on the same line
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Password: ******",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f) // Push the text to the left
        )

        TextButton(
          onClick = {
            showDialog = true
            toChange = "Password"
          }
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
            Text(text = "${myViewModel.status.emoji} ${myViewModel.status.description}")
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
            myViewModel.statusList.forEach { activity ->
              DropdownMenuItem(
                text = { Text(text = "${activity.emoji} ${activity.description}") },
                onClick = {
                  myViewModel.updateStatus(activity)
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
              Text("Enter New $toChange")

              Spacer(modifier = Modifier.height(8.dp))

              // TextField to input new display name
              OutlinedTextField(
                value = newValue,
                onValueChange = { newValue = it },
                label = { Text("New $toChange") },
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
                  if ( toChange == "Display Name") {
                    myViewModel.updateDisplayName(newValue)
                  } else if ( toChange == "Email") {
                    myViewModel.updateEmail(newValue)
                  } else if ( toChange == "Username") {
                    myViewModel.updateUsername(newValue)
                  } else {
                    myViewModel.updatePassword(newValue)
                  }
                  showDialog = false
                  newValue = ""
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
