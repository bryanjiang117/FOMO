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
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
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
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
        install(Auth)
    }

    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // start of signed in stateflow
    private val _signedInFlow = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedInFlow

    private fun setSignedInState(isSignedIn: Boolean) {
        _signedInFlow.value = isSignedIn
    }

    // end of signed in stateflow



    var id = 2L // sample logged in account

    // user
    var displayName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var uid by mutableStateOf("")
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
    var mode by mutableStateOf<String>("walking")
    var places by mutableStateOf<List<Place>>(emptyList())

    // Start of Map Functions

    fun onMyWay(coords: LatLng) {
        if (status.description != "On my way") {
            return
        }
        selectedLocation = null
        routePoints = null
        viewModelScope.launch {
            selectedLocation = coords
            routePoints = getRoute(center, selectedLocation!!, mode)
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
    suspend fun getRoute(origin: LatLng, destination: LatLng, mode: String): List<LatLng>? {
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

    // Start of Auth Functions

    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Call Supabase's signUpWith function
                val user = supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }


                if (user != null) {
                    // this line should not run for now
                    Log.d("SupabaseAuth", "User created with email confirmation: $email")
                    onResult(true)
                } else {
                    val userObject = supabase.auth.retrieveUserForCurrentSession(updateSession = true)
                    val users = supabase.from("users")

                    users.insert(User(
                        uid = userObject.id,
                        displayName = email,
                        createdAt = dateFormat.format(Date()),
                        email = email,
                        username = email,
                        password = password,
                        latitude = 0.0,
                        longitude = 0.0,
                        status_id = 2,
                        notiMessages = false,
                        notiNearby = false,
                        notiStatus = false
                    ))

                    Log.d("SupabaseAuth", "User signed up created and added to DB: $email")
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SupabaseAuth", "Sign-up failed: ${e.message}")
                onResult(false)
            }
        }
    }
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try{
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val session = supabase.auth.currentSessionOrNull()
                val user = supabase.auth.retrieveUserForCurrentSession(updateSession = true)

                // initialize sign-in state and establish userid
                if (session != null) {
                    Log.d("SupabaseAuth", "Session active, userid ${user.id}")
                    uid = user.id
                    onResult(true)
                    setSignedInState(true)
                } else {
                    Log.d("SupabaseAuth", "Sign-in credentials invalid")
                    onResult(false)
                    setSignedInState(false)
                }

            } catch (e: Exception) {
                Log.e("SupabaseAuth", "Sign-in failed: ${e.message}")
                onResult(false)
            }
        }

    }

    fun logout() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                Log.d("SupabaseAuth", "User signed out")
                setSignedInState(false)
                uid = ""
            } catch (e: Exception) {
                Log.e("SupabaseAuth", "failed to sign out: ${e.message}")
            }
        }
    }


    // End of Auth Functions


    // Start of Database Functions

    fun fetchPlaces() {
        viewModelScope.launch {
            try {
                places = supabase.from("places").select() {
                    filter {
                        eq("owner_id", id)
                    }
                }.decodeList<Place>()
                Log.d("Supabase fetchPlaces()", "Places fetched")
            } catch (e: Exception) {
                Log.e("Supabase fetchPlaces()", "Error: ${e.message}")
            }
        }
    }

    fun setPlace(name:String, coords: LatLng, radius: Double) {
        viewModelScope.launch {
            try {
                val newPlace = Place(name, coords.latitude, coords.longitude, radius, id)
                supabase.from("places").insert(newPlace)
            } catch (e: Exception) {
                Log.e("Supabase setPlace()", "Error: ${e.message}")
            }
        }
    }

    fun removePlace(id: Long) {
        viewModelScope.launch {
            try {
                supabase.from("places").delete() {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                Log.e("Supabase removePlace()", "Error: ${e.message}")
            }
        }
    }

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
                    if (friendship.accepted && friendship.receiverId == uid) {
                        val friendId = friendship.requesterId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("uid", friendId)
                                }
                            }.decodeSingle<User>()
                        tempFriends.add(tempFriend)
                    } else if (friendship.accepted && friendship.requesterId == uid) {
                        // Add receiverId if requesterId is the current user
                        val friendId = friendship.receiverId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("uid", friendId)
                                }
                            }.decodeSingle<User>()
                        tempFriends.add(tempFriend)
                    } else if (!friendship.accepted && friendship.receiverId == uid) {
                        // Add requester if receiverId is 2
                        val friendId = friendship.requesterId
                        val tempFriend = userRes
                            .select() {
                                filter {
                                    eq("uid", friendId)
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
                        eq("uid", uid)
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
                fetchPlaces()

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
                       eq("uid", uid)
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
                        eq("uid", uid)
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
                        eq("uid", uid)
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
                        eq("uid", uid)
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
                        eq("uid", uid)
                    }
                }
                userLongitude = longitude
                userLatitude = latitude
                center = LatLng(latitude, longitude)

                Log.d("Map View Model Location Update", "Latitude: $latitude, Longitude: $longitude")
                Log.d("Supabase Location Update", "Latitude: $latitude, Longitude: $longitude")
            } catch (e: Exception) {
                Log.e("Supabase", "Location Update Error: ${e.message}")
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
                            eq("uid", uid)
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
                            eq("uid", uid)
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
                        eq("requester_id", receiver.uid)
                        eq("receiver_id", uid)
                    }
                }.decodeList<Friendship>()
                if (oppositeCheck.isNotEmpty() && !oppositeCheck[0].accepted) { // if the opposite request exists just become friends
                    supabase.from("friendship").update({
                        set("accept_date", dateFormat.format(Date()))
                        set("accepted", true)
                    }) {
                        filter {
                            eq("requester_id", receiver.uid)
                            eq("receiver_id", uid)
                        }
                    }
                    fetchFriends()
                } else if (oppositeCheck.isEmpty() && receiver.uid != uid) { // if not create a new friend request
                    val newRequest = Friendship(createdAt = dateFormat.format(Date()), requesterId = uid,
                        receiverId = receiver.uid, accepted = false)
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
                                eq("requester_id", friendToRemove.uid);
                                eq("receiver_id", uid);
                            }
                            and {
                                eq("requester_id", uid);
                                eq("receiver_id", friendToRemove.uid);
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

    fun acceptRequest(requester: String, receiver: String) {
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

    fun declineRequest(requester: String, receiver: String) {
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
                        eq("uid", uid)
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
                        eq("uid", uid)
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
                        eq("uid", uid)
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


