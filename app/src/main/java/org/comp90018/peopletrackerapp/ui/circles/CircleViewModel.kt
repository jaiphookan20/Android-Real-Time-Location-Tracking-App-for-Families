package org.comp90018.peopletrackerapp.ui.circles

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.models.service.StorageService
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject
import org.comp90018.peopletrackerapp.R.string as AppText

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val accountService: AccountService,
    private val storageService: StorageService,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    var uiState = mutableStateOf(CircleUiState())
    var isLoading = mutableStateOf(false)
    var joinCircleIsLoading = mutableStateOf(false)
    val circles = storageService.circles

    private val circleName
        get() = uiState.value.circleName
    private val inviteCode
        get() = uiState.value.inviteCode
    private val isDialogVisible
        get() = uiState.value.isDialogVisible
    private val selectedCircle
        get() = uiState.value.selectedCircle

    fun onCircleNameChange(newValue: String) {
        uiState.value = uiState.value.copy(circleName = newValue)
    }

    fun onInviteCodeChange(newValue: String) {
        uiState.value = uiState.value.copy(inviteCode = newValue)
    }

    fun onCircleClick(circle: Circle, members: List<User>) {
        uiState.value = uiState.value.copy(
            isDialogVisible = true,
            selectedCircle = circle,
            circleMembers = members
        )
    }

    fun onAlertDismiss() {
        uiState.value = uiState.value.copy(
            isDialogVisible = false,
            selectedCircle = null,
            circleMembers = emptyList()
        )
    }

    fun onCreateCircle(navController: NavController) {
        if(circleName.isBlank()) {
            SnackbarManager.showMessage(AppText.blank_circle_name)
            return
        }

        val creatorID = accountService.currentUserId
        val circleID = UUID.randomUUID().toString()
        val inviteCode = UUID.randomUUID().toString().substring(0,6)

        val members = listOf(creatorID)

        val newCircle = Circle(
            circleID = circleID,
            creatorID = creatorID,
            inviteCode = inviteCode,
            name = circleName,
            members = members,
        )

        viewModelScope.launch {
            isLoading.value = true
            val circleCreated = storageService.createCircle(newCircle)
            if (!circleCreated) {
                SnackbarManager.showMessage(AppText.duplicate_circle_name)
            } else {
                SnackbarManager.showMessage(AppText.create_circle_success)
                // Go to circle page after creation
                Handler(Looper.getMainLooper()).postDelayed({
                    navController.navigate("${Routes.Group.route}/${circleID}")
                }, 2000)
            }
            isLoading.value = false
        }
    }

    fun onJoinCircle(navController: NavController) {
        if(inviteCode.isBlank()) {
            SnackbarManager.showMessage(AppText.blank_invite_code)
            return
        }

        viewModelScope.launch {
            joinCircleIsLoading.value = true
            val circleId = storageService.joinCircle(inviteCode)
            Log.d("joincircle", circleId)

            when (circleId) {
                "Exception" -> {
                    SnackbarManager.showMessage(AppText.generic_error)
                }
                "alreadyMember" -> {
                    SnackbarManager.showMessage(AppText.already_in_circle)
                }
                "circleNotExist" -> {
                    SnackbarManager.showMessage(AppText.invalid_invite_code)
                }
                else -> {
                    SnackbarManager.showMessage(AppText.join_circle_success)
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.navigate("${Routes.Group.route}/${circleId}")
                    }, 2000)
                }
            }

            joinCircleIsLoading.value = false
        }
    }

    fun getMemberNames(memberIDs: List<String>): Flow<List<User>> {
        var members = emptyFlow<List<User>>()
        launchCatching {
            members = storageService.circleMembers(memberIDs)
        }
        return members
    }

}