package com.example.fomo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.fomo.models.Activity

class MyViewModel : ViewModel() {
    var curScreen by mutableStateOf("ur mom")

    var displayName by mutableStateOf("Kevin Yang") // Initial display name

    var activity by mutableStateOf(Activity("Idle", "\uD83D\uDCA4")) // Initial display status

    fun updateDisplayName(newName: String) {
        displayName = newName
    }

    fun updateActivity(newActivity: Activity) {
        activity = newActivity
    }


}