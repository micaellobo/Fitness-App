package com.example.fitnessmoblie.models

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var age: Int = 0,
    var height: Int = 0,
    var weight: Int = 0,
    var totalDistance: Int = 0,
    val totalBurnedCalories: Int = 0
)
