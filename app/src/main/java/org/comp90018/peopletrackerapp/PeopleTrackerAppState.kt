package org.comp90018.peopletrackerapp

import android.content.res.Resources
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.comp90018.peopletracker.app.common.snackbar.SnackbarManager
import org.comp90018.peopletracker.app.common.snackbar.SnackbarMessage.Companion.toMessage

@Stable
class PeopleTrackerAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController,
    private val snackbarManager: SnackbarManager,
    private val resources: Resources,
    coroutineScope: CoroutineScope
) {
    init {
        coroutineScope.launch {
         snackbarManager.snackbarMessages.filterNotNull().collect { snackbarMessage ->
             val text = snackbarMessage.toMessage(resources)
             scaffoldState.snackbarHostState.showSnackbar(
                 message = text
             )
           }
        }
    }

//    val bottomBarRoutes = Routes::class.sealedSubclasses
//        .filterIsInstance<Routes>()
//        .filter {it.showBottomBar == true}
//        .map {it.route}
//
//    val shouldShowBottomBar: Boolean
//        @Composable get() = navController
//            .currentBackStackEntryAsState().value?.destination?.route in bottomBarRoutes

    // fun popUp() {
    //     navController.popBackStack()
    //   }
    
    //   fun navigate(route: String) {
    //     navController.navigate(route) { launchSingleTop = true }
    //   }
    
    //   fun navigateAndPopUp(route: String, popUp: String) {
    //     navController.navigate(route) {
    //       launchSingleTop = true
    //       popUpTo(popUp) { inclusive = true }
    //     }
    //   }
    
    //   fun clearAndNavigate(route: String) {
    //     navController.navigate(route) {
    //       launchSingleTop = true
    //       popUpTo(0) { inclusive = true }
    //     }
    //   }
}