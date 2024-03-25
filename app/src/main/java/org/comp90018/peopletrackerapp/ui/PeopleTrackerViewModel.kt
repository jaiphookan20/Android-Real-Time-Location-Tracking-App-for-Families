package org.comp90018.peopletrackerapp.ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletracker.app.common.snackbar.SnackbarMessage.Companion.toSnackbarMessage
import org.comp90018.peopletrackerapp.models.service.LogService

open class PeopleTrackerViewModel(
    private val logService: LogService,
) : ViewModel() {
    // Handles logging errors to firebase, showing snackbar messages 
    // Without disrupting the other threads.

    fun launchCatching(isLoading: MutableState<Boolean>? = null, snackbar: Boolean = true, block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                 if (snackbar) {
                     SnackbarManager.showMessage(throwable.toSnackbarMessage())
                 }
                if (isLoading != null) {
                    isLoading.value = false
                }

                logService.logNonFatalCrash(throwable)
            },
            block = block
        )
}