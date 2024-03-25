package org.comp90018.peopletrackerapp.ui.signup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.comp90018.peopletracker.app.common.ext.isValidEmail
import org.comp90018.peopletracker.app.common.ext.isValidPassword
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import javax.inject.Inject
import org.comp90018.peopletrackerapp.R.string as AppText

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountService,
    logService: LogService
): PeopleTrackerViewModel(logService) {

    var uiState = mutableStateOf(SignupUiState())
    var isLoading = mutableStateOf(false)

    private val firstName
        get() = uiState.value.firstName
    private val lastName
        get() = uiState.value.lastName
    private val username
        get() = uiState.value.username
    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun hasLoggedIn(): Boolean {
        return accountService.hasUser
    }
    fun onFirstnameChange(newValue: String) {
        uiState.value = uiState.value.copy(firstName = newValue)
    }

    fun onLastnameChange(newValue: String) {
        uiState.value = uiState.value.copy(lastName = newValue)
    }
    fun onUsernameChange(newValue: String) {
        uiState.value = uiState.value.copy(username = newValue)
    }

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onSignUpClick(navController: NavController) {
        if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() ) {
            SnackbarManager.showMessage(AppText.empty_fields)
            return
        }

        if (!email.isValidEmail()) {
            SnackbarManager.showMessage(AppText.email_error)
            return
        }

        if (!password.isValidPassword()) {
            SnackbarManager.showMessage(AppText.password_error)
            return
        }

        // if (!password.passwordMatches(uiState.value.repeatPassword)) {
        //     // SnackbarManager.showMessage(AppText.password_match_error)
        //     return
        // }

        viewModelScope.launch {
                isLoading.value = true
                accountService.createUserAccount(email, password, username, firstName, lastName)
                SnackbarManager.showMessage(AppText.signup_success)
                navController.navigate(Routes.Start.route)
                isLoading.value = false
            }
    }
}