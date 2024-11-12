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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.google.maps.android.PolyUtil

@Serializable
data class GeocodeResponse(
    val results: List<Result>,
    val status: String
)

@Serializable
data class Result(
    val place_id: String,
    val formatted_address: String
)

@Serializable
data class DirectionsResponse(
    val routes: List<Route>
)

@Serializable
data class Route(
    val legs: List<Leg>,
    val overview_polyline: Polyline
)

@Serializable
data class Leg(
    val distance: Distance,
    val duration: Duration
)

@Serializable
data class Distance(
    val text: String,
    val value: Int
)

@Serializable
data class Duration(
    val text: String,
    val value: Int
)

@Serializable
data class Polyline(
    val points: String
)

class MyViewModel : ViewModel() {

    // constants
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private val defaultStatus = Status(id=7, dateFormat.format(Date()), "Idle", "\uD83D\uDCA4")

    // clients
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://vwapghztewutqqmzaoib.supabase.co",
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
    }
    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // temp
    var signedIn by mutableStateOf<Boolean>(false) // temp variable to simulate auth
    var id = 2L // sample logged in account

    // user
    var displayName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    private var password by mutableStateOf("")
    var notiNearby by mutableStateOf(false)
    var notiStatus by mutableStateOf(false)
    var notiMessages by mutableStateOf(false)
    var userLongitude by mutableDoubleStateOf(0.0)
    var userLatitude by mutableDoubleStateOf(0.0)
    var center by mutableStateOf(LatLng(43.4723, -80.5449))
    var status by mutableStateOf<Status>(defaultStatus)
    var friendsList by mutableStateOf<List<User>>(emptyList())
    var requestList by mutableStateOf<List<User>>(emptyList())
    var statusList by mutableStateOf<List<Status>>(emptyList())
    var selectedLocation by mutableStateOf<LatLng?>(null)
    var routePoints by mutableStateOf<List<LatLng>?>(null)

    // Start of Map Functions

    fun onMyWay(coords: LatLng) {
        viewModelScope.launch {
            selectedLocation = coords
            routePoints = getRoute(center, selectedLocation!!)
            updateStatus(statusList.filter{status -> status.description == "On my way"}[0])
        }
    }

    suspend fun getPlaceId(coords: LatLng): String? {
        try {
            val response: GeocodeResponse = ktorClient.get("https://maps.googleapis.com/maps/api/geocode/json") {
                parameter("latlng", "${coords.latitude},${coords.longitude}")
                parameter("location_type", "ROOFTOP")
                parameter("result_type", "street_address")
                parameter("key", BuildConfig.GOOGLE_MAPS_API_KEY)
            }.body()
            if (response.results.isEmpty()) {
                throw IllegalArgumentException("Place_id API returned no results")
            }
            val place_id = response.results[0].place_id
            return place_id
        } catch(e: Exception) {
            "Request failed: ${e.message}"
            return null
        }
    }

    // mode should be "walking", "driving", "bicycling", "transit"
    suspend fun getRoute(origin: LatLng, destination: LatLng, mode: String = "walking"): List<LatLng>? {
        try {
            val response: DirectionsResponse = ktorClient.get("https://maps.googleapis.com/maps/api/directions/json") {
                parameter("origin", "${origin.latitude},${origin.longitude}")
                parameter("destination", "${destination.latitude},${destination.longitude}")
                parameter("mode", mode)
                parameter("key", BuildConfig.GOOGLE_MAPS_API_KEY)
            }.body()
            if (response.routes.isEmpty()) {
                throw IllegalArgumentException("Directions API returned no routes")
            }
            // finds the route with shortest distance
            val encodedRoute = response.routes.minByOrNull{ it.legs[0].distance.value }!!.overview_polyline.points
            val route = PolyUtil.decode(encodedRoute)
            return route
        } catch(e: Exception) {
            "Request failed: ${e.message}"
            return null
        }
    }

    // End of Map Functions


    // Start of Database Functions

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
                } else if (newStatus.description == "On My Way") {

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

    fun removeFriend(username: String) {
        viewModelScope.launch {
            try {
                val friendToRemove = supabase.from("users").select() {
                    filter {
                        eq("username", username);
                    }
                }.decodeSingle<User>()
                val res = supabase.from("friendship").delete() {
                    filter {
                        or {
                            and {
                                eq("requester_id", friendToRemove.id);
                                eq("receiver_id", id);
                            }
                            and {
                                eq("requester_id", id);
                                eq("receiver_id", friendToRemove.id);
                            }
                        }
                    }
                }
                fetchFriends()
            } catch(e: Exception) {
                Log.e("SupabaseConnection", "DB Error: ${e.message}");
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

    // End of Database Functions

}


