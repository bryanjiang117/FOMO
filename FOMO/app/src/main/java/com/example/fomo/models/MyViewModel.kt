package com.example.fomo.models

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MyViewModel : ViewModel() {

    var displayName by mutableStateOf("Kevin Yang") // Initial display name

    var activity by mutableStateOf(ActivityModel("Idle", "\uD83D\uDCA4")) // Initial display status

    fun updateDisplayName(newName: String) {
        displayName = newName
    }

    fun updateActivity(newActivity: ActivityModel) {
        activity = newActivity
    }


}