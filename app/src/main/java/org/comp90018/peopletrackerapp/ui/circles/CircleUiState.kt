package org.comp90018.peopletrackerapp.ui.circles

import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.User

data class CircleUiState(
    val circleName: String = "",
    val inviteCode: String = "",
    var isDialogVisible: Boolean = false,
    var selectedCircle: Circle? = null,
    var circleMembers: List<User> = emptyList()
)
