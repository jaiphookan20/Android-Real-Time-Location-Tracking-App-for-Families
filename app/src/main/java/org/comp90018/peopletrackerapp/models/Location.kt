package org.comp90018.peopletrackerapp.models

data class Location(
    val userID: String = "",
    val circleID: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0
)
