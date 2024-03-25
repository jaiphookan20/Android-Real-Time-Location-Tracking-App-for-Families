package org.comp90018.peopletrackerapp.ui.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.models.service.StorageService
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val storageService: StorageService,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    var uiState = mutableStateOf(HomeUiState())
    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    val circles
        get() = storageService.circles

    val user = accountService.currentUser


    fun hasLoggedIn(): Boolean {
        return accountService.hasUser
    }

    fun logOut() {
        launchCatching {
            accountService.signOut()
        }
    }

    // permissions
    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

}
