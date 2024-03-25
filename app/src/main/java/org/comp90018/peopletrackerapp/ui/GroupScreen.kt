package org.comp90018.peopletrackerapp.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.rounded.GroupWork
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.common.composables.BackgroundLocationPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.CoarseLocationPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.FineLocationPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.ForegroundPermissionTextProvider
import org.comp90018.peopletrackerapp.common.composables.PermissionDialog
import org.comp90018.peopletrackerapp.common.composables.PushNotificationPermissionTextProvider
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.service.location.GeofenceBroadcastReceiver
import org.comp90018.peopletrackerapp.service.location.LocationService
import org.comp90018.peopletrackerapp.ui.map.MapViewModel
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme
import org.comp90018.peopletrackerapp.utils.getDateTime

//private const val TAG = "groupscreen"

/*
    TODO Fix bottom sheet user populating
    TODO Change markers to be pictures
    TODO Change initial position to zoom in to view all users or current users, right now it is hardcoded to Melbourne
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavController,
    circleID: String,
    viewModel: MapViewModel
) {

    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionsAcquired = remember { mutableStateOf(false) }

    val uiSettings by viewModel.uiSettings
    val properties by viewModel.properties
    val circles = viewModel.circles.collectAsStateWithLifecycle(initialValue = emptyList())
    val circleChanged by viewModel.circleChange.value.collectAsStateWithLifecycle(initialValue = false)
    val userLocations =
        viewModel.userLocations.value.collectAsStateWithLifecycle(initialValue = emptyList())
    val geofences = viewModel.geofences.value.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoadingGeofenceCreate by remember { viewModel.isLoadingGeofenceCreate }
    val isLoadingStartTracking by remember { viewModel.isLoadingStartTracking }
    val isLoadingStopTracking by remember { viewModel.isLoadingStopTracking }
    val isLoadingGeofenceDelete by remember { viewModel.isLoadingGeofenceDelete }
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.setLocation, 14f)
    }

    var permissionsToRequest = emptyArray<String>()

    permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }

    fun startLocationTracking(){
        viewModel.startTracking(
            context,
            circles.value
        )
        // Location updates
        Intent(
            context.applicationContext,
            LocationService::class.java
        ).apply {
            action =
                LocationService.ACTION_START
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

    fun stopLocationTracking(){
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

    LaunchedEffect(circleID) {
        viewModel.chooseCircle(circleID)
    }

    LaunchedEffect(circleChanged) {
        if (circleChanged) {
            Toast.makeText(context, "This circle has been deleted!", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.Home.route)
        }
    }


    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            var _isGranted = true
            permissionsToRequest.forEach { permission ->
                viewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
                _isGranted = _isGranted && perms[permission] == true
                if(perms[permission] != true) {
                    Log.d("PERMISSIONS", "$permission is not granted!")
                }
            }
            permissionsAcquired.value = _isGranted
            if(_isGranted) {
                startLocationTracking();
            }

        }
    )

    LaunchedEffect(permissionsAcquired.value) {
        viewModel.isMyLocationEnabled(permissionsAcquired.value)
    }

    // TODO Replace this placeholder user icon logic with actually getting the profile images from Firebase
    val avatarPlaceholderURL = "https://cdn4.iconfinder.com/data/icons/cat-circle/248/lucky_cat_animal_avatar_expression_circle-512.png" +
            ""
    var drawable by remember{
        mutableStateOf(BitmapDrawable(context.resources, AppCompatResources.getDrawable(context, R.drawable.user_photo_placeholder)!!.toBitmap()))
    }
    LaunchedEffect(Unit) {
        val iconRequest = ImageRequest.Builder(context)
            .data(avatarPlaceholderURL)
            .transformations(CircleCropTransformation())
            .size(150.dp.value.toInt())
            .build()
        val imageLoader = ImageLoader(context)
        imageLoader.enqueue(iconRequest)
        drawable = ImageLoader(context).execute(iconRequest).drawable as BitmapDrawable
    }

    BackHandler {
        if(uiState.isTracking) {
            Toast.makeText(context, "Can't go back whilst tracking!", Toast.LENGTH_LONG).show()
        }
    }


        // Layout
    if(uiState.selectedCircle != null){
        PeopleTrackerTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                BottomSheetScaffold(
                    topBar = {
                        GroupAppBar(
                            navController = navController,
                            viewModel = viewModel,
                            isTracking = uiState.isTracking,
                            context = context,
                            circle = uiState.selectedCircle!!
                        )
                    },
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 50.dp,
                    sheetContent = {
                        // TODO Need a List<User> here with users who belong to the current circle. This will populate the bottom sheet user icons
                        BottomSheetContent(userLocations.value, cameraPositionState)
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        GoogleMap(
                            modifier = Modifier.matchParentSize(),
                            properties = properties,
                            uiSettings = uiSettings,
                            contentPadding = paddingValues,
                            cameraPositionState = cameraPositionState,
                            onMapClick = {
                                viewModel.dismissGeofence()
                            },
                            onMapLongClick = { latLng ->
                                if (!uiState.isTracking) {
                                    viewModel.startGeofenceMode(latLng)
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newCameraPosition(
                                                CameraPosition.fromLatLngZoom(latLng, 15f)
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
                                            title = if (user.username != null) "${user.username}" else "No name",
                                            snippet = "Last seen: ${getDateTime(user.timestamp)}",
                                            //icon = BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_BLUE),

                                            // TODO Change icon to be user profile image here, need to clip it though
                                            icon = BitmapDescriptorFactory.fromBitmap(drawable.bitmap)
                                        )
                                    }
                                }
                                // Show Circle Geofences
                                for (g in geofences.value) {
                                    Circle(
                                        center = LatLng(g.centerLatitude, g.centerLongitude),
                                        radius = g.radius.toDouble(),
                                        fillColor = MaterialTheme.colorScheme.inversePrimary.copy(
                                            alpha = 0.5f
                                        ),
                                        strokeColor = MaterialTheme.colorScheme.surfaceTint,
                                        strokeWidth = 2f,
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
                                    fillColor = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.5f),
                                    strokeColor = MaterialTheme.colorScheme.surfaceTint,
                                    strokeWidth = 2f,
                                )
                            }
                        }
                        if (!uiState.geofenceMode) {
                            Column(
                                modifier = Modifier.matchParentSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Circle selection dropdown
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    MaterialTheme(
                                        shapes = MaterialTheme.shapes.copy(
                                            medium = RoundedCornerShape(
                                                16.dp
                                            )
                                        )
                                    ) {
                                        val menuExpanded = remember { mutableStateOf(false) }
                                        Column {
                                            if (uiState.selectedCircle != null) {
                                                FloatingActionButton(
                                                    onClick = {
                                                        menuExpanded.value = !menuExpanded.value
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(5.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.GroupWork, "")
                                                        if (menuExpanded.value) {
                                                            Spacer(Modifier.width(10.dp))
                                                            Text("Geofences")
                                                        }
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = menuExpanded.value,
                                                    onDismissRequest = {
                                                        menuExpanded.value = false
                                                    },
                                                ) {
                                                    geofences.value.forEach { geofence ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(text = geofence.name)
                                                            },
                                                            onClick = {
                                                                menuExpanded.value = false
                                                                cameraPositionState.position =
                                                                    CameraPosition.fromLatLngZoom(
                                                                        LatLng(
                                                                            geofence.centerLatitude,
                                                                            geofence.centerLongitude
                                                                        ),
                                                                        15f
                                                                    )
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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
                                            ExpandableFAB(
                                                navController,
                                                viewModel::onDeleteGeofence
                                            )
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
                                                        // Request all permissions when the button is clicked
                                                        multiplePermissionResultLauncher.launch(
                                                            permissionsToRequest
                                                        )
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
                                            // This is the FloatingActionButton for disabling location
                                            if (!isLoadingStopTracking) {
                                                FloatingActionButton(
                                                    onClick = {
                                                        // Stop location tracking
                                                        stopLocationTracking()
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
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
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
                                Text(
                                    text = "Set a radius",
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                                )

                                Slider(
                                    value = uiState.sliderPosition,
                                    onValueChange = { viewModel.onSliderValueChange(it) },
                                    valueRange = 0f..2000f,
                                    steps = 2000
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
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
            }
        }
    }
    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        FineLocationPermissionTextProvider()
                    }
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        CoarseLocationPermissionTextProvider()
                    }
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        BackgroundLocationPermissionTextProvider()
                    }
//                    Manifest.permission.POST_NOTIFICATIONS -> {
//                        PushNotificationPermissionTextProvider()
//                    }
                    Manifest.permission.FOREGROUND_SERVICE -> {
                        ForegroundPermissionTextProvider()
                    }
                    else -> return@forEach
                },
                isPermanentlyDeclined = !ActivityCompat.shouldShowRequestPermissionRationale(
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
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    Text(text = "Delete Circle", modifier = Modifier.padding(horizontal = 3.dp))
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

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun GroupAppBar(navController: NavController, viewModel: MapViewModel, isTracking: Boolean, context: Context, circle: Circle) {

    MediumTopAppBar(
        title = {
            circle.let {
                Text(
                    text = it.name,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            if(!isTracking) {
                IconButton(onClick = { navController.navigate(Routes.Home.route) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "go back"
                    )
                }
            } else {
                IconButton(onClick={Toast.makeText(context, "You can't go back whilst being tracked!", Toast.LENGTH_LONG).show()}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        tint = Color.LightGray.copy(alpha = LocalContentAlpha.current),
                        contentDescription = "go back"
                    )
                }
            }
        },
        actions = {

            // Row to contain both action buttons and align them to the end
            Row(
                modifier = Modifier.fillMaxWidth(0.2F),
                horizontalArrangement = Arrangement.End
            ) {

                viewModel.uiState.value.selectedCircle?.let {
                    val circleID = it.circleID
                    if(!isTracking) {
                        IconButton(onClick = { navController.navigate("posts/$circleID") }) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = "Posts"
                            )
                        }
                    } else {
                        IconButton(onClick={Toast.makeText(context, "You can't go the post whilst being tracked!", Toast.LENGTH_LONG).show()}) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                tint = Color.LightGray.copy(alpha = LocalContentAlpha.current),
                                contentDescription = "Posts"
                            )
                        }
                    }
                }

                GroupMenu(navController = navController, circle, viewModel)
            }

        }
    )
}

@Composable
fun BottomSheetContent(users: List<User>, cameraPositionState: CameraPositionState) {
    Column(
        modifier = Modifier
            .heightIn(min = 100.dp, max = 250.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        UserListComponent(users, cameraPositionState)
    }
}

@Composable
fun UserListComponent(users: List<User>, cameraPositionState: CameraPositionState) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp),
        content = {
            items(users) {
                UserItem(it, cameraPositionState)
            }
        }
    )
}

@Composable
fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
) {
    if (!user.profileImage.isNullOrBlank()) {
        // TODO Add user profile pic here when avaialble
//        Image(
//            painter = painterResource(
//                R.drawable.user_photo_placeholder
//            ),
//            contentDescription = "User profile pic",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.size(size)
//        )
    } else{
        AsyncImage(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            model = user.profileImage,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onLoading = {  },
            onError = { error ->
                Log.e("UserProfileCard", "Error loading image: ${error.result.throwable}")

                // Display a fallback image
            }
        )
    }
    if (!user.firstName.isNullOrBlank() && !user.lastName.isNullOrBlank()) {
        val name = listOf(user.firstName, user.lastName)
            .joinToString(separator = "")
            .uppercase()
        Box(modifier.size(size), contentAlignment = Alignment.Center)
        {
            val initials = (user.firstName.take(1) + user.lastName.take(1)).uppercase()
            Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(SolidColor(Color(0xFFFFD8E4))) }
            Text(text = initials,  color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    } else if (!user.username.isNullOrBlank()){
        Box(modifier.size(size), contentAlignment = Alignment.Center)
        {
            val initials = user.username.take(2).uppercase()
            Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(SolidColor(Color(0xFFFFD8E4))) }
            Text(text = initials,  color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    } else {
        Box(modifier.size(size), contentAlignment = Alignment.Center)
        {
            Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(SolidColor(Color(0xFFFFD8E4))) }
            Text(text = "??",  color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }

}
@Composable
fun UserItem(user: User, cameraPositionState: CameraPositionState) {
    val scope = CoroutineScope(Dispatchers.Main)
    var latlng: LatLng? = null

    if (user.latitude != null && user.longitude != null) {
        latlng = LatLng(user.latitude, user.longitude)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (latlng != null) {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(latlng, 15f)
                            )
                        )
                    }
                }
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            UserAvatar(user = user)
            Text(user.username ?: "anon")
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GroupMenu(navController: NavController, circle: Circle, viewModel: MapViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showInviteDialog by remember {mutableStateOf(false)}
    var confirmDeleteDialog by remember { mutableStateOf(false) }
    var confirmLeaveDialog by remember { mutableStateOf(false) }

    val inviteCode = circle.inviteCode
    val context = LocalContext.current

    // Show alert box to get confirmation before deleting the circle
    if (confirmDeleteDialog) {
        AlertDialog(
            title = {Text("Delete circle?")},
            text = {
                Text("Are you sure you want to delete this circle? This action is irreversible!")
            },
            onDismissRequest = {
                confirmDeleteDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCircle(navController)
                        confirmDeleteDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmDeleteDialog = false
                }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // Show alert box to get confirmation before removing the user from the circle
    if (confirmLeaveDialog) {
        AlertDialog(
            title = {Text("Leave circle?")},
            text = {
                Text("Are you sure you want to leave this circle?")
            },
            onDismissRequest = {
                confirmLeaveDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.leaveCircle(navController)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmLeaveDialog = false
                }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // Show invite code
    if (showInviteDialog) {
        AlertDialog(
            title = {Text("Invite code")},
            text = {
                Text("The invite code for this circle is: $inviteCode")
            },
            onDismissRequest = {
                showInviteDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "$inviteCode")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showInviteDialog = false
                }) {
                    Text(text = "Close")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        horizontalArrangement = Arrangement.End
    ) {

        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {


            DropdownMenuItem(
                text = { Text("Get invite code") },
                onClick = {
                    expanded = false
                    showInviteDialog = true
                },
            )


            if (viewModel.isUserCircleOwner()) {
                DropdownMenuItem(
                    text = { Text("Delete circle") },
                    onClick = {
                        expanded = false
                        confirmDeleteDialog = true
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Leave circle") },
                    onClick = {
                        expanded = false
                        confirmLeaveDialog = true
                    },
                )
            }

        }

    }

}
