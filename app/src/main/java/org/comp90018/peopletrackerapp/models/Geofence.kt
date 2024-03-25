package org.comp90018.peopletrackerapp.models

data class Geofence(
    val name: String = "",
    val geofenceID: String = "",
    val circleID: String = "",
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val radius: Float = 0F,
)