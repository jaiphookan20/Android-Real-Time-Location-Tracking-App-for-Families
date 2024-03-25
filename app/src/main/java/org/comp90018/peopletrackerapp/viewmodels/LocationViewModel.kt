package org.comp90018.peopletrackerapp.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.dataObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.models.User
import javax.inject.Inject

class LocationViewModel: ViewModel() {

    val db = FirebaseFirestore.getInstance()
    private val _users: Flow<List<User>>
        get() = db.collection("users").dataObjects()

    val userList: Flow<List<User>> = flow {
        _users.collect() { userlist ->
            emit(userlist)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )


    var locationTrackingEnabled by mutableStateOf(false)
        private set

    fun enableTracking() {
        locationTrackingEnabled = true
    }

    fun disableTracking() {
        locationTrackingEnabled = false
    }


}