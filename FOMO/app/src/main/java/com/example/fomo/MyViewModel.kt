package com.example.fomo

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MyViewModel : ViewModel() {
    var curScreen by mutableStateOf("ur mom")
}