package org.comp90018.peopletrackerapp

// import com.google.accompanist.permissions.ExperimentalPermissionsApi
// import com.google.accompanist.permissions.isGranted
// import com.google.accompanist.permissions.rememberPermissionState
// import com.google.accompanist.permissions.shouldShowRationale
import android.content.res.Resources
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletrackerapp.navigation.RootNavGraph

@Composable
@ExperimentalMaterialApi
fun PeopleTrackerApp() {
    Surface(color = MaterialTheme.colors.background) {
        val appState = rememberAppState()

        Scaffold(
            scaffoldState = appState.scaffoldState
        ) { innerPaddingModifier ->
            RootNavGraph(
                appState = appState,
                modifier = Modifier.padding(innerPaddingModifier),
                navController = appState.navController
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

@Composable
fun rememberAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(scaffoldState, navController, snackbarManager, resources, coroutineScope) {
    PeopleTrackerAppState(scaffoldState, navController, snackbarManager, resources, coroutineScope)
  }