package org.comp90018.peopletrackerapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Circle(
    val name: String = "",
    val creatorID: String = "",
    val circleID: String = "",
    val inviteCode: String = "",
    val members: List<String> = emptyList(),
//    val geoFences: List<Geofence> = emptyList(),
)