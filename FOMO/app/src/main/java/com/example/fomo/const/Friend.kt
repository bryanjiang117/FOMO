package com.example.fomo.const

data class Friend(
  val id: Int,
  val name: String,
  val email: String? = "",
  var status: Int = 0,
  var online: Boolean = false,
)