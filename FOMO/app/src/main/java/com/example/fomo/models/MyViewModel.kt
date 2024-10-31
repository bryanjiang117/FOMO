package com.example.fomo.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.fomo.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {


    val supabase = createSupabaseClient(
        supabaseUrl = "https://vwapghztewutqqmzaoib.supabase.co",
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
    }
    // State to hold the list of users
    var users by mutableStateOf<List<User>>(emptyList())
        private set

    // State to hold the list of friendships
    var friendsList by mutableStateOf<List<User>>(emptyList())

    var statuses by mutableStateOf<List<Status>>(emptyList())
        private set
    //
    var userId = 2L

    var displayName by mutableStateOf("Kevin Yang") // Initial display name


    fun fetchDatabase() {
        viewModelScope.launch {
            try {
                val userRes = supabase.from("users")
                val friendshipRes = supabase.from("friendship")
                    .select()
                    .decodeList<Friendship>()
                val statusRes = supabase.from("statuses").select().decodeList<Status>()

                val me = supabase.from("users").select {
                    filter {
                        eq("id", 2)
                    }
                }.decodeSingle<User>()

                displayName = me.displayName

                val tempFriends = mutableListOf<User>()
                for (friendship in friendshipRes) {
                    if (friendship.accepted) {
                        if (friendship.receiverId == userId) {
                            // Add requesterId if receiverId is 2
                            val friendId = friendship.requesterId
                            val tempFriend = userRes
                                .select() {
                                    filter {
                                        eq("id", friendId)
                                    }
                                }.decodeSingle<User>()
                            tempFriends.add(tempFriend)
                        } else if (friendship.requesterId == userId) {
                            // Add receiverId if requesterId is 2
                            val friendId = friendship.receiverId
                            val tempFriend = userRes
                                .select() {
                                    filter {
                                        eq("id", friendId)
                                    }
                                }.decodeSingle<User>()
                            tempFriends.add(tempFriend)
                        }
                    }
                }
                friendsList = tempFriends
                users = userRes.select().decodeList<User>()
                statuses = statusRes


                Log.d("SupabaseConnection", "Friends fetched: $friendsList")
                Log.d("SupabaseConnection", "Users fetched: $users")
                Log.d("SupabaseConnection", "Statuses \uD83D\uDCAA fetched: $statuses")
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
                       eq("id", 2)
                   }
               }

                displayName = newDisplayName
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "Failed to connect to database: ${e.message}")
            }
        }
    }



    var activity by mutableStateOf(ActivityModel("Idle", "\uD83D\uDCA4")) // Initial display status

//    fun updateDisplayName(newName: String) {
//        displayName = newName
//    }

    fun updateActivity(newActivity: ActivityModel) {
        activity = newActivity
    }


}


