package com.example.fomo.models

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.example.fomo.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MyViewModel : ViewModel() {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val defaultStatus = Status(id=7, dateFormat.format(Date()), "Idle", "\uD83D\uDCA4")

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://vwapghztewutqqmzaoib.supabase.co",
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    // State to hold the list of friendships
    var friendsList by mutableStateOf<List<User>>(emptyList())
    var requestList by mutableStateOf<List<User>>(emptyList())
    var statusList by mutableStateOf<List<Status>>(emptyList())
    //
    var id = 2L // sample logged in account

    var displayName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var notiNearby by mutableStateOf(false)
    var notiStatus by mutableStateOf(false)
    var notiMessages by mutableStateOf(false)
    var userLongitude by mutableDoubleStateOf(0.0)
    var userLatitude by mutableDoubleStateOf(0.0)
    var center by mutableStateOf(LatLng(43.4723, -80.5449))
    var status by mutableStateOf<Status>(defaultStatus)

    fun fetchFriends() {
        viewModelScope.launch {
            try {
                val userRes = supabase.from("users")
                val friendshipRes = supabase.from("friendship")
                    .select()
                    .decodeList<Friendship>()
                val tempFriends = mutableListOf<User>()
                val tempRequesters = mutableListOf<User>()
                for (friendship in friendshipRes) {
                    if (friendship.accepted && friendship.receiverId == id) {
                        // Add requesterId if receiverId is 2
                        val friendId = friendship.requesterId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("id", friendId)
                                }
                            }.decodeSingle<User>()
                        tempFriends.add(tempFriend)
                    } else if (friendship.accepted && friendship.requesterId == id) {
                        // Add receiverId if requesterId is 2
                        val friendId = friendship.receiverId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("id", friendId)
                                }
                            }.decodeSingle<User>()
                        tempFriends.add(tempFriend)
                    } else if (!friendship.accepted && friendship.receiverId == id) {
                        // Add requester if receiverId is 2
                        val friendId = friendship.requesterId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("id", friendId)
                                }
                            }.decodeSingle<User>()
                        tempRequesters.add(tempFriend)
                    }
                }
                friendsList = tempFriends
                requestList = tempRequesters


            } catch (e: Exception) {
                Log.e("SupabaseConnection", "Failed to connect to database: ${e.message}")
            }
        }
    }

    fun fetchDatabase() {
        viewModelScope.launch {
            try {
                val statusRes = supabase.from("statuses").select().decodeList<Status>()

                val me = supabase.from("users").select() {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<User>()

                statusList = statusRes
                displayName = me.displayName
                username = me.username
                email = me.email
                password = me.password
                notiNearby = me.notiNearby
                notiStatus = me.notiStatus
                notiMessages = me.notiMessages
                status = statusRes.filter {it.id == me.status_id}[0]


                fetchFriends()


                Log.d("SupabaseConnection", "Friends fetched: $friendsList")
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "Failed to connect to database: ${e.message}")
            }
        }
    }

    fun updateDisplayName(newDisplayName: String){
        viewModelScope.launch {
            try {
               supabase.from("users").update({
                   set("display_name", newDisplayName)
               }){
                   filter {
                       eq("id", id)
                   }
               }

                displayName = newDisplayName
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun updateEmail(newEmail: String){
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("email", newEmail)
                }){
                    filter {
                        eq("id", id)
                    }
                }

                email = newEmail
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }


    fun updateUsername(newUsername: String){
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("username", newUsername)
                }){
                    filter {
                        eq("id", id)
                    }
                }

                username = newUsername
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }


    fun updatePassword(newPassword: String){
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("password", newPassword)
                }){
                    filter {
                        eq("id", id)
                    }
                }

                password = newPassword
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }


    fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        return SphericalUtil.computeDistanceBetween(point1, point2)
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("latitude", latitude)
                    set("longitude", longitude)
                }){
                    filter {
                        eq("id", id)
                    }
                }
                userLongitude = longitude
                userLatitude = latitude
                center = LatLng(latitude, longitude)

                Log.d("Map View Model Location Update", "Latitude: $latitude, Longitude: $longitude")
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun updateStatus(newStatus: Status) {
        viewModelScope.launch {
            try {
                if (newStatus.id == 1L) { // if new status is do not disturb
                    supabase.from("users").update({
                        set("status", newStatus.id)
                        set("noti_nearby", false)
                        set("noti_status", false)
                        set("noti_messages", false)
                    }){
                        filter {
                            eq("id", id)
                        }
                    }
                    notiStatus = false
                    notiNearby = false
                    notiMessages = false
                } else {
                    supabase.from("users").update({
                        set("status", newStatus.id)
                    }){
                        filter {
                            eq("id", id)
                        }
                    }
                }
                status = newStatus
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun createRequest(username: String) {
        viewModelScope.launch {
            try {
                val receiver = supabase.from("users").select() { // Find the receiver
                        filter {
                            eq("username", username)
                        }
                }.decodeSingle<User>()
                val oppositeCheck = supabase.from("friendship").select() { // check if opposite request exists
                    filter {
                        eq("requester_id", receiver.id)
                        eq("receiver_id", id)
                    }
                }.decodeList<Friendship>()
                if (oppositeCheck.isNotEmpty() && !oppositeCheck[0].accepted) { // if the opposite request exists just become friends
                    supabase.from("friendship").update({
                        set("accept_date", dateFormat.format(Date()))
                        set("accepted", true)
                    }) {
                        filter {
                            eq("requester_id", receiver.id)
                            eq("receiver_id", id)
                        }
                    }
                    fetchFriends()
                } else if (oppositeCheck.isEmpty() && receiver.id != id) { // if not create a new friend request
                    val newRequest = Friendship(createdAt = dateFormat.format(Date()), requesterId = id,
                        receiverId = receiver.id, accepted = false)
                    supabase.from("friendship").insert(newRequest)
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun acceptRequest(requester: Long, receiver: Long) {
        viewModelScope.launch {
            try {
                supabase.from("friendship").update({
                    set("accept_date", dateFormat.format(Date()))
                    set("accepted", true)
                }){
                    filter {
                        eq("requester_id", requester)
                        eq("receiver_id", receiver)
                    }
                }
                fetchFriends()
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun declineRequest(requester: Long, receiver: Long) {
        viewModelScope.launch {
            try {
                supabase.from("friendship").delete(){
                    filter {
                        eq("requester_id", requester)
                        eq("receiver_id", receiver)
                    }
                }
                fetchFriends()
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }

    }

    fun updateNotiStatus(setTo: Boolean) {
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("noti_status", setTo)
                }){
                    filter {
                        eq("id", id)
                    }
                }
                notiStatus = setTo
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }

    }

    fun updateNotiNearby(setTo: Boolean) {
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("noti_nearby", setTo)
                }){
                    filter {
                        eq("id", id)
                    }
                }
                notiNearby = setTo
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }

    fun updateNotiMessages(setTo: Boolean) {
        viewModelScope.launch {
            try {
                supabase.from("users").update({
                    set("noti_messages", setTo)
                }){
                    filter {
                        eq("id", id)
                    }
                }
                notiMessages = setTo
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}")
            }
        }
    }


}


