package org.comp90018.peopletrackerapp.ui.map


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.models.service.StorageService
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import java.util.UUID
import javax.inject.Inject
import org.comp90018.peopletrackerapp.R.string as AppText
import org.comp90018.peopletrackerapp.models.Geofence as GeofenceModel

@HiltViewModel
class MapViewModel @Inject constructor(
    private val storage: StorageService,
    private val accountService: AccountService,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    // queue
    private val TAG = "MAPVIEWMODEL"
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    var uiSettings = mutableStateOf(MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false))
    var properties = mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false))
    var isLoadingStartTracking = mutableStateOf(false)
    var isLoadingStopTracking = mutableStateOf(false)
    var isLoadingGeofenceCreate = mutableStateOf(false)
    var isLoadingGeofenceDelete = mutableStateOf(false)

    var circles = storage.circles
    var circleChange = mutableStateOf<Flow<Boolean>>(emptyFlow())
    var userLocations = mutableStateOf<Flow<List<User>>>(emptyFlow())
    var geofences = mutableStateOf<Flow<List<GeofenceModel>>>(emptyFlow())
    private var geofenceListener:ListenerRegistration? = null

    init {
        viewModelScope.launch {
            // Set location to user
            accountService.currentUser.collect {
                if(it.latitude!=null && it.longitude!=null){
                    _uiState.update { curr ->
                        curr.copy(
                            setLocation = LatLng(it.latitude, it.longitude)
                        )
                    }
                }
            }
        }
    }

    private val selectedCircle
        get() = uiState.value.selectedCircle
    private val geofenceName
        get() = uiState.value.geofenceName
    private val geofenceCoordinate
        get() = uiState.value.geofenceCoordinate
    private val sliderPosition
        get() = uiState.value.sliderPosition
    private val geofenceIsSelected
        get() = uiState.value.geofenceIsSelected
    private val selectedGeofenceID
        get() = uiState.value.selectedGeofenceID

    fun dismissGeofence() {
        if(geofenceIsSelected) {
            _uiState.update {curr ->
                curr.copy(
                    geofenceIsSelected = false,
                    selectedGeofenceName = "",
                    selectedGeofenceID = "",
                )
            }
        }
    }

    fun selectGeofence(geofence: GeofenceModel) {
        _uiState.update {curr ->
            curr.copy(
                geofenceIsSelected = true,
                selectedGeofenceName = geofence.name,
                selectedGeofenceID = geofence.geofenceID
            )
        }
    }

    fun onDeleteGeofence() {
        launchCatching (isLoadingGeofenceDelete) {
            isLoadingGeofenceDelete.value = true
            storage.removeGeofence(selectedCircle!!.circleID, selectedGeofenceID)
            isLoadingGeofenceDelete.value = false
        }
        dismissGeofence()
    }

    fun isMyLocationEnabled(isEnabled: Boolean) {
        if(isEnabled) {
            uiSettings.value = uiSettings.value.copy(myLocationButtonEnabled = true)
            properties.value = properties.value.copy(isMyLocationEnabled = true)
        }
    }

    fun startTracking(context: Context, circles: List<Circle>) {
        isLoadingStartTracking.value = true
        launchCatching (isLoadingStartTracking) {
            Log.d(TAG,"Start tracking Registering geofences...")
            var circleIDs = circles.map { circle -> circle.circleID }
            Log.d(TAG,"fetched circles: $circleIDs\nRegistering geofences...")
            storage.registerGeofences(context, circleIDs)
            Log.d(TAG,"Monitoring database...")
            geofenceListener = storage.monitorGeofences(context)
            isLoadingStartTracking.value = false

        }
        _uiState.update { curr ->
            curr.copy(
                isTracking = true,
                context = context
            )
        }
        uiSettings.value = uiSettings.value.copy(myLocationButtonEnabled = true)
        properties.value = properties.value.copy(isMyLocationEnabled = true)
    }

//    fun stopTracking(context: Context) {
//        isLoadingStopTracking.value = true
//        geofenceListener?.remove()
//        launchCatching (isLoadingStopTracking) {
//            storage.removeGeofences(context)
//            isLoadingStopTracking.value = false
//            _uiState.update { curr ->
//                curr.copy(isTracking = false)
//            }
//        }
//        savedStateHandle.set("_uiState", _uiState.value)
//    }
fun stopTracking(context: Context) {
    isLoadingStopTracking.value = true
    viewModelScope.launch {
        try {
            storage.removeGeofences(context)

            // Remove the listener, if you have such a method
            geofenceListener?.remove()

            // Log that tracking has stopped
            Log.d(TAG, "Stopped tracking. Unregistered geofences and removed listeners.")

            // Update the UI state to reflect that tracking has stopped
            _uiState.update { curr ->
                curr.copy(isTracking = false)
            }

            // Disable the My Location Button and the property as tracking has stopped
            uiSettings.value = uiSettings.value.copy(myLocationButtonEnabled = false)
            properties.value = properties.value.copy(isMyLocationEnabled = false)

        } catch (e: Exception) {
            // Handle any exceptions during storage operation
            Log.e(TAG, "Error while trying to stop tracking", e)
        } finally {
            // Ensure isLoadingStopTracking is always updated at the end of the operation
            isLoadingStopTracking.value = false
        }
    }
}



    fun chooseCircle(circleID: String) {
        launchCatching {
            circles.collect { circleList ->
                for (circle in circleList) {
                    if(circle.circleID == circleID) {
                        _uiState.update { curr ->
                            curr.copy(selectedCircle = circle)
                        }
                        circleChange.value = storage.circleChangeListener(circleID)
                        userLocations.value = storage.circleLocations(circle.members)
                        geofences.value = storage.geofencesInCircle(circle.circleID)
                    }
                }

            }

        }
    }

    fun startGeofenceMode(latLng: LatLng) {
        _uiState.update { curr ->
            curr.copy(
                geofenceMode = true,
                geofenceCoordinate = latLng
            )
        }
    }

    fun stopGeofenceMode() {
        _uiState.update { curr ->
            curr.copy(
                geofenceMode = false,
                geofenceName = "",
                geofenceCoordinate = null,
                sliderPosition = 0f,
            )
        }
    }

    fun onSliderValueChange(newValue: Float) {
        _uiState.update { curr ->
            curr.copy(
                sliderPosition = newValue
            )
        }
    }

    fun onGeofenceNameChange(newValue: String) {
        _uiState.update { curr ->
            curr.copy(
                geofenceName = newValue
            )
        }

    }

    fun onCreateGeofence() {
        if(geofenceName.isBlank()) {
            SnackbarManager.showMessage(AppText.geofence_name_empty)
            return
        }

        if(geofenceCoordinate == null) {
            SnackbarManager.showMessage(AppText.geofence_center_empty)
            return
        }

        if(selectedCircle == null) {
            SnackbarManager.showMessage(AppText.geofence_circle_empty)
            return
        }

        if (sliderPosition < 1) {
            SnackbarManager.showMessage(AppText.radius_is_zero)
            return
        }

        val geofenceID = UUID.randomUUID().toString()

        val newGeofence = GeofenceModel(
            name = geofenceName,
            geofenceID = geofenceID,
            circleID = selectedCircle!!.circleID,
            centerLatitude = geofenceCoordinate!!.latitude,
            centerLongitude = geofenceCoordinate!!.longitude,
            radius = sliderPosition
        )
        launchCatching (isLoadingGeofenceCreate) {
            isLoadingGeofenceCreate.value = true
            storage.addGeofence(selectedCircle!!.circleID,newGeofence)
            isLoadingGeofenceCreate.value = false
        }
        stopGeofenceMode()
    }

    // Location permission Handling
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

    // TODO add case where the admin is the only member of the circle and chooses to leave - circle should be deleted as well
    fun leaveCircle(navController: NavController) {
        if (selectedCircle != null) {
            viewModelScope.launch {
                storage.removeUserFromCircle(
                    accountService.currentUserId,
                    selectedCircle!!.circleID
                )
            }
            SnackbarManager.showMessage(AppText.removed_from_circle)
            Handler(Looper.getMainLooper()).postDelayed({
                navController.navigate(Routes.Home.route)
            }, 1000)
        }
    }

    // Admin check, currently for dertermining whether user will see option to delete the circle
    fun isUserCircleOwner(): Boolean {
        if (selectedCircle != null) {
            return selectedCircle!!.creatorID == accountService.currentUserId
        }
        return false
    }

    fun deleteCircle(navController: NavController) {
        if (selectedCircle != null) {
            viewModelScope.launch {
                storage.deleteCircle(
                    selectedCircle!!.circleID
                )
            }
            SnackbarManager.showMessage(AppText.removed_from_circle)
            Handler(Looper.getMainLooper()).postDelayed({
                navController.navigate(Routes.Home.route)
            }, 1000)
        }
        Log.d(TAG, "$navController")

    }
}

