package com.example.fomo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.screen.Screen
import android.util.Log
import com.example.fomo.models.MyViewModel
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.ripple.rememberRipple
import com.example.fomo.const.Colors
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import com.example.fomo.models.User


class FriendsScreen(private val myViewModel: MyViewModel) : Screen {
  @Composable
  override fun Content() {
    var selectedTab by remember { mutableStateOf(0)}

    fun onSelectTab(newTab: Int) {
      selectedTab = newTab
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Header()
      Nav(selectedTab, ::onSelectTab)
      when (selectedTab) {
        0 -> FriendsList(myViewModel)
        1 -> AddFriends(myViewModel)
        2 -> Requests(myViewModel)
      }
    }
  }
}

@Composable
fun Header() {
  Row(
    modifier = Modifier
      .padding(top = 20.dp)
  ) {
    Text(
      text = "Friends",
      fontWeight = FontWeight.ExtraBold,
      fontSize = 30.sp
    )
  }
}

val navCardTitles = arrayOf(
  "My Friends",
  "Add Friends",
  "Requests",
)

@Composable
fun Nav(selectedTab: Int, onSelectTab: (Int) -> Unit){
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for((i, title) in navCardTitles.withIndex()) {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (i == selectedTab) Colors.primary else Color.LightGray),
        modifier = Modifier
          .clip(RoundedCornerShape(24.dp))
          .clickable (
            onClick = {onSelectTab(i)},
            // bounds ripple animation to rounded shape instead of rectangle
            indication = rememberRipple(bounded = true),
            interactionSource = remember { MutableInteractionSource() }
          )
      ) {
        Text(
          text = title,
          color = if (i == selectedTab) Color.White else Color.Black,
          fontWeight = if (i == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }
  }
}

@Composable
fun FriendsList(myViewModel: MyViewModel) {
  var isLoaded by remember { mutableStateOf(false) }
  val friends = myViewModel.friendsList
  val myLocation = myViewModel.center

  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    for(friend in friends) {
      val friendLocation = LatLng(friend.latitude, friend.longitude)
      val distance = myViewModel.calculateDistance(friendLocation, myLocation)
      val friendStatus = myViewModel.statusList.filter {it.id == friend.status_id}[0]

      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.height(75.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.placeholder_pfp),
          contentDescription = "Profile Picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(75.dp)
            .clip(CircleShape)
        )
        Column(
          verticalArrangement = Arrangement.SpaceEvenly,
          modifier = Modifier.fillMaxHeight()
        ) {
          Text(
            text = friend.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "${distance.toInt()}m away",
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
          )
          Text(
            text = "${friendStatus.emoji} ${friendStatus.description}",
            fontSize = 16.sp,
          )
        }
      }
    }
  }
}

@Composable
fun AddFriends(myViewModel: MyViewModel) {
  var text by remember { mutableStateOf("") }


  OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Add Friend") },
    placeholder = { Text("Enter your friend's username") },
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions (
      onDone = {
        myViewModel.createRequest(text)
      }
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
  )
}

@Composable
fun Requests(myViewModel: MyViewModel) {
  var requests by remember { mutableStateOf<List<User>>(myViewModel.requestList) }


  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    for(request in requests) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
          .height(75.dp)
          .fillMaxWidth()
      ) {
        Image(
          painter = painterResource(id = R.drawable.placeholder_pfp),
          contentDescription = "Profile Picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(75.dp)
            .clip(CircleShape)
        )
        Column(
          verticalArrangement = Arrangement.SpaceEvenly,
          modifier = Modifier.fillMaxHeight()
        ) {
          Text(
            text = request.username ?: "",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        // puts space between name and check to push check to far right
        Spacer(modifier = Modifier.weight(1f))

        Column(
          verticalArrangement = Arrangement.SpaceEvenly,
          modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check, contentDescription = "Check Icon",
            modifier = Modifier.clickable (
              onClick = {
                myViewModel.acceptRequest(request.id ?: -1, myViewModel.id)
              }
            )
          )
          Icon(
            imageVector = Icons.Default.Close, contentDescription = "X Icon",
            modifier = Modifier.clickable (
              onClick = {
                myViewModel.declineRequest(request.id ?: -1, myViewModel.id)
              }
            )
          )
        }
      }
    }
  }
}