package com.example.fomo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.screen.Screen
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.fomo.models.MyViewModel
import com.example.fomo.const.Friend
import com.example.fomo.const.activities
import com.example.fomo.const.Colors

class FriendsScreen(private val myViewModel: MyViewModel) : Screen {
  @Composable
  override fun Content() {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Header()
      Nav()
      FriendsList(myViewModel)
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
  "MyFriends",
  "Requests",
  "Sent",
)

@Composable
fun Nav(){
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    for(title in navCardTitles) {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Colors.primary),
      ) {
        Text(
          text = title
        )
      }
    }
  }
}

@Composable
fun FriendsList(myViewModel: MyViewModel) {
  var isLoaded by remember { mutableStateOf(false) }
  var friends by remember { mutableStateOf(arrayOf(
    Friend(id=1, name="Steven", status=0, online=false),
    Friend(id=2, name="Daniel", status=1, online=true),
    Friend(id=225, name="Daniel2", status=2, online=true),
  )) }

  Column {
    for(friend in friends) {
      Row {
        Image(
          painter = painterResource(id = R.drawable.placeholder_pfp),
          contentDescription = "Profile Picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(75.dp) // Set size as needed
            .clip(CircleShape)
        )
        Column {
          Text(
            text = friend.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = activities[friend.status].name,
            fontSize = 10.sp,
          )
        }
      }
    }
  }
}