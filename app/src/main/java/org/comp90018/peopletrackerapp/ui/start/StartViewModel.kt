package org.comp90018.peopletrackerapp.ui.start

import dagger.hilt.android.lifecycle.HiltViewModel
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val accountService: AccountService,
    logService: LogService,
): PeopleTrackerViewModel(logService) {
    fun hasLoggedIn(): Boolean {
        return accountService.hasUser
    }
}