package org.comp90018.peopletrackerapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.comp90018.peopletrackerapp.common.composables.PermissionDialog
import org.comp90018.peopletrackerapp.common.composables.PushNotificationPermissionTextProvider
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme


//TODO Prevent swipe back to go to signup/login?
/*
    TODO Add some kind of progress indicator to explain lag when loading in the page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    if(!viewModel.hasLoggedIn()) {
        navController.navigate(Routes.Start.route) {
            launchSingleTop = true
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    val context = LocalContext.current
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    val notificationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                isGranted = isGranted)
        }
    )
    
    LaunchedEffect(Unit) {
        notificationPermissionResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val circles by viewModel.circles.collectAsStateWithLifecycle(initialValue = emptyList())
    val user by viewModel.user.collectAsStateWithLifecycle(initialValue = null)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Reset scroll position when returning to this page
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        listState.scrollToItem(0)
    }

    val greeting: String = when {
        user == null -> ""
        user!!.username?.isNotBlank() == true -> "${user!!.username}'s Circles"
        else -> { "" }
    }

    // Show permission dialogs
    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = PushNotificationPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    context as Activity,
                    permission
                ),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    notificationPermissionResultLauncher.launch(permission)
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
    PeopleTrackerTheme {
        Surface {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    MediumTopAppBar(
                        title = {
                            Text(
                                text = greeting,
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        actions = {
                            HomeMenu(navController, viewModel)
                        })
                },
                bottomBar = {
                    /*TODO*/
                }
            )
            { scaffoldPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ActionsRow(navController = navController)

                    GroupListColumn(navController, circles, listState)
                }
            }
        }
    }
}

@Composable
fun GroupListColumn(
    navController: NavController,
    circleList: List<Circle> = emptyList(),
    listState: LazyListState
) {
    LazyColumn(
        state = listState
    ) {
        items(circleList) { circle: Circle -> // Explicitly set the type to Circle
            GroupItem(circle.name) {
                navController.navigate("${Routes.Group.route}/${circle.circleID}") // Using circleID here
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupItem(id: String, action: () -> Unit) {
    Card(
        onClick = action,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(
            vertical = 16.dp,
            horizontal = 24.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(150.dp)
        ) {
            Text(
                text = id,
                modifier = Modifier.padding(8.dp),
                fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            )
        }
    }
}


@Composable
fun ActionsRow(
    navController: NavController,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        AssistChip(
            modifier = Modifier.padding(horizontal = 8.dp),
            label = { Text(text = "Join a circle") },
            onClick = {navController.navigate(Routes.JoinCircle.route) },
            shape = MaterialTheme.shapes.small
        )
        AssistChip(
            modifier = Modifier.padding(horizontal = 8.dp),
            label = { Text(text = "Create a circle") },
            onClick = {navController.navigate(Routes.CreateCircle.route) }
        )

    }
}



@Composable
fun HomeMenu(navController: NavController, viewModel: HomeViewModel) {
    var expanded by remember { mutableStateOf(false) }

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

            // VIEW ACCOUNT
            DropdownMenuItem(
                text = { Text("Account") },
                onClick = {
                    navController.navigate(Routes.UserProfile.route)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "account"
                    )
                }
            )

            // SETTINGS
//            DropdownMenuItem(
//                text = { Text("Settings") },
//                onClick = {
//                    navController.navigate(Routes.UserProfile.route)
//                },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Default.Settings,
//                        contentDescription = "account"
//                    )
//                }
//            )

            Divider()

            // LOGOUT
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    viewModel.logOut()
                    navController.navigate(Routes.Start.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                          //logoutUser(context, navController)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "logout"
                    )
                }
            )
        }
    }
}