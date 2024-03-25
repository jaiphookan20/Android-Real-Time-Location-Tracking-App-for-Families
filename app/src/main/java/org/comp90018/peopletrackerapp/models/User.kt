package org.comp90018.peopletrackerapp.models

data class User(
    val userID: String? = "",
    val username: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    var profileImage: String? = "",
    val circlesJoined: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long? = null,
    val fcmToken: String? = "",
//    val profilePictureUrl: String? = null,
)