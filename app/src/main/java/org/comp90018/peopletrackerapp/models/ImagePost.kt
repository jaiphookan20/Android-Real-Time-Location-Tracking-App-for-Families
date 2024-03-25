package org.comp90018.peopletrackerapp.models

data class ImagePost(
    val userId: String = "",
    val circleId: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String = ""
)
