package org.comp90018.peopletrackerapp.models

import com.google.firebase.Timestamp

data class Notification(
    val title: String = "",
    val message: String = "",
    val toTopic: String = "",
    val expiry: Timestamp? = null,
)
