package org.comp90018.peopletrackerapp.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.common.composables.CoarseLocationPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.FineLocationPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.PermissionDialog
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.service.location.GeofenceBroadcastReceiver
import org.comp90018.peopletrackerapp.service.location.LocationService
import org.comp90018.peopletrackerapp.utils.getDateTime

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var permissionsAcquired = remember { mutableStateOf(false) }
    var uiSettings by viewModel.uiSettings
    val properties by viewModel.properties
    val circles = viewModel.circles.collectAsStateWithLifecycle(initialValue = emptyList())
    val circleChanged by viewModel.circleChange.value.collectAsStateWithLifecycle(initialValue = false)
    val userLocations =
        viewModel.userLocations.value.collectAsStateWithLifecycle(initialValue = emptyList())
    val geofences =
        viewModel.geofences.value.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoadingGeofenceCreate by remember { viewModel.isLoadingGeofenceCreate }
    val isLoadingStartTracking by remember { viewModel.isLoadingStartTracking }
    val isLoadingStopTracking by remember { viewModel.isLoadingStopTracking }
    val isLoadingGeofenceDelete by remember { viewModel.isLoadingGeofenceDelete }
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = fromLatLngZoom(uiState.setLocation, 15f)
    }
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE,
    )

    Log.d("MAPVIEW", "${uiState.isTracking}")
    Log.d("MAPVIEW", "${uiState.selectedCircle}")

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsAcquired.value = true
            permissionsToRequest.forEach { permission ->
                viewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
                permissionsAcquired.value = perms[permission] == true
            }
        }
    )

    LaunchedEffect(permissionsAcquired) {
        multiplePermissionResultLauncher.launch(permissionsToRequest)
    }

    LaunchedEffect(circleChanged) {
        if(circleChanged) {
            navController.navigate(Routes.Home.route)
        }
    }

    if(uiState.selectedCircle == null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card {
                Text(
                    "Create or Enter a circle before tracking!",
                    Modifier.padding(10.dp)
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            properties = properties,
            uiSettings = uiSettings,
            cameraPositionState = cameraPositionState,
            onMapClick = {
                viewModel.dismissGeofence()
            },
            onMapLongClick = { latLng ->
                if (!uiState.isTracking && uiState.selectedCircle != null) {
                    viewModel.startGeofenceMode(latLng)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                fromLatLngZoom(latLng, 15f)
                            )
                        )
                    }
                }
            },
        ) {
            if (!uiState.geofenceMode) {
                // Show Circle member location markers
                for (user in userLocations.value) {
                    if (user.latitude != null && user.longitude != null) {
                        val location = LatLng(user.latitude, user.longitude)
                        val markerState = MarkerState(position = location)
                        Marker(
                            state = markerState,
                            title = if (user.username != "") "${user.username}" else "No name",
                            snippet = "Last seen: ${getDateTime(user.timestamp)}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                        )
                    }
                }
                // Show Circle Geofences
                geofences.value.forEach { g ->
                    Circle(
                        center = LatLng(g.centerLatitude, g.centerLongitude),
                        radius = g.radius.toDouble(),
                        fillColor = Color(0x80DDDDFF),
                        strokeColor = Color(0x809191ff),
                        strokeWidth = 5f,
                        tag = g.name,
                        clickable = true,
                        onClick = { viewModel.selectGeofence(g) }
                    )
                }
            } else {
                Marker(
                    state = remember { MarkerState(position = uiState.geofenceCoordinate!!) }
                )
                Circle(
                    center = uiState.geofenceCoordinate!!,
                    radius = uiState.sliderPosition.toDouble(),
                )
            }
        }
        if (uiState.geofenceMode) {
            Column(
                modifier = Modifier.matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Geofence
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.geofenceIsSelected) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                        ) {
                            Card {
                                Text(
                                    text = uiState.selectedGeofenceName,
                                    modifier = Modifier.padding(5.dp),
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )
                            }
                        }
                        if (!isLoadingGeofenceDelete) {
                            ExpandableFAB(navController, viewModel::onDeleteGeofence)
                        } else {
                            CircularProgressIndicator(
                                Modifier
                                    .offset(
                                        x = (-16).dp,
                                        y = (-16).dp
                                    )
                            )
                        }
                    }
                    // Enable and disable location
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!uiState.isTracking) {
                            if (!isLoadingStartTracking) {
                                FloatingActionButton(
                                    onClick = {
                                        viewModel.startTracking(context, circles.value)
                                        // Location updates
                                        Intent(
                                            context.applicationContext,
                                            LocationService::class.java
                                        ).apply {
                                            action = LocationService.ACTION_START
                                            context.startService(this)
                                        }
                                        // Listen for Goefence triggers
                                        Intent(
                                            context,
                                            GeofenceBroadcastReceiver::class.java
                                        ).apply {
                                            context.startService(this)
                                        }
                                    }
                                ) {
                                    Text("Enable Location", Modifier.padding(5.dp))
                                }
                            } else {
                                Card {
                                    CircularProgressIndicator()
                                }
                            }
                        } else {
                            if (!isLoadingStopTracking) {
                                FloatingActionButton(
                                    onClick = {
                                        viewModel.stopTracking(context)
                                        Intent(
                                            context.applicationContext,
                                            LocationService::class.java
                                        ).apply {
                                            action = LocationService.ACTION_STOP
                                            context.startService(this)
                                        }
                                        Intent(
                                            context,
                                            GeofenceBroadcastReceiver::class.java
                                        ).apply {
                                            context.stopService(this)
                                        }
                                    }
                                ) {
                                    Text("Disable Location", Modifier.padding(5.dp))
                                }
                            } else {
                                Card {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.matchParentSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    TextField(
                        value = uiState.geofenceName,
                        onValueChange = { viewModel.onGeofenceNameChange(it) },
                        label = {
                            Text("Geofence Name")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )
//                        selectAction(uiState, viewModel::onActionChecked)
                }
                Text("Set a radius")
                Slider(
                    value = uiState.sliderPosition,
                    onValueChange = { viewModel.onSliderValueChange(it) },
                    valueRange = 0f..2000f,
                    steps = 2000
                )
                Row {
                    if (!isLoadingGeofenceCreate) {
                        Button(
                            onClick = {
                                viewModel.onCreateGeofence()
                            }
                        ) {
                            Text("Create")
                        }
                        Button(
                            onClick = { viewModel.stopGeofenceMode() }
                        ) {
                            Text("Cancel")
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    }
    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when(permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        FineLocationPermissionTextProvider()
                    }
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        CoarseLocationPermissionTextProvider()
                    }
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
//                        BackgroundLocationPermissionTextProvider()
//                    }
                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    context as Activity,
                    permission
                ),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            )
        }
}


@Composable
fun ExpandableFAB(
    navController: NavController,
    deleteGeofence: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = (-16).dp,
                y = (-16).dp
            )
    ) {
        if (isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                // Add Edit function later /*TODO*/
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding( vertical = 10.dp ),
//                ) {
//                    Text(text = "Edit Geofence", modifier=Modifier.padding(horizontal=3.dp))
//                    androidx.compose.material.FloatingActionButton(
//                        onClick = { navController.navigate(Routes.CreateCircle.route) }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Add,
//                            contentDescription = "create group button"
//                        )
//                    }
//                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding( vertical = 5.dp)
                ) {
                    Text(text = "Delete Geofence", modifier = Modifier.padding(horizontal=3.dp))
                    FloatingActionButton(
                        onClick = { deleteGeofence() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "delete geofence button"
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
            }
        ) {
            if (isExpanded) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "open circle menu"
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "open circle menu"
                )
            }
        }
    }
}