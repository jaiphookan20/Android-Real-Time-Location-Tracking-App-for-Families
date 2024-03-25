package org.comp90018.peopletrackerapp.ui.signup

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
)