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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.screen.Screen
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.mutableStateOf
import com.example.fomo.models.MyViewModel
import com.example.fomo.const.Friend
import com.example.fomo.const.activities
import com.example.fomo.const.Colors

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
        1 -> ReceivedRequests()
        2 -> SentRequests()
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
  "Requests",
  "Sent",
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
  var friends by remember { mutableStateOf(arrayOf(
    Friend(id=1, name="Steven", status=0, online=false),
    Friend(id=2, name="Daniel", status=1, online=true),
    Friend(id=225, name="Daniel2", status=2, online=true),
  )) }

  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    for(friend in friends) {
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
            text = friend.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "${activities[friend.status].name} ${activities[friend.status].emoji}",
            fontSize = 16.sp,
          )
        }
      }
    }
  }
}

@Composable
fun ReceivedRequests() {

}

@Composable
fun SentRequests() {

}