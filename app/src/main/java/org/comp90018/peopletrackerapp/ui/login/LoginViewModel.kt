package org.comp90018.peopletrackerapp.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import org.comp90018.peopletracker.app.common.ext.isValidEmail
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import javax.inject.Inject
import org.comp90018.peopletrackerapp.R.string as AppText

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    var uiState = mutableStateOf(LoginUiState())
    var isLoading = mutableStateOf(false)

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun hasLoggedIn(): Boolean {
        return accountService.hasUser
    }
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onLoginClick(navController: NavController) {
        // Below two checks if any fields are empty anyways?
        if (email.isBlank() || password.isBlank()) {
            SnackbarManager.showMessage(AppText.empty_fields)
            return
        }

        if (!email.isValidEmail()) {
            SnackbarManager.showMessage(AppText.email_error)
            return
        }

        if (password.isBlank()) {
            SnackbarManager.showMessage(AppText.empty_password_error)
            return
        }

        launchCatching(isLoading) {
            isLoading.value = true
            accountService.authenticate(email,password)
            SnackbarManager.showMessage(AppText.login_success)
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Login.route) {
                    inclusive = true
                }
            }
            isLoading.value = false
        }
    }
}