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
    var friendships by mutableStateOf<List<Friendship>>(emptyList())
        private set

    fun fetchDatabase() {
        viewModelScope.launch {
            try {
                val userRes = supabase.from("users").select().decodeList<User>()
                val friendshipRes = supabase.from("friendship").select().decodeList<Friendship>()
                users = userRes
                friendships = friendshipRes

                Log.d("SupabaseConnection", "Friendships fetched: $friendships")
                Log.d("SupabaseConnection", "Users fetched: $users")
            } catch (e: Exception) {
                Log.e("SupabaseConnection", "Failed to connect to database: ${e.message}")
            }
        }
    }

    var displayName by mutableStateOf("Kevin Yang") // Initial display name

    var activity by mutableStateOf(ActivityModel("Idle", "\uD83D\uDCA4")) // Initial display status

    fun updateDisplayName(newName: String) {
        displayName = newName
    }

    fun updateActivity(newActivity: ActivityModel) {
        activity = newActivity
    }


}