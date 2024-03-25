package org.comp90018.peopletrackerapp.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dagger.hilt.android.EntryPointAccessors
import org.comp90018.peopletrackerapp.PeopleTrackerAppState
import org.comp90018.peopletrackerapp.ui.GroupScreen
import org.comp90018.peopletrackerapp.ui.circles.CircleScreen
import org.comp90018.peopletrackerapp.ui.circles.CreateCircleScreen
import org.comp90018.peopletrackerapp.ui.circles.JoinCircleScreen
import org.comp90018.peopletrackerapp.ui.home.HomeScreen
import org.comp90018.peopletrackerapp.ui.login.LoginScreen
import org.comp90018.peopletrackerapp.ui.map.MapViewModel
import org.comp90018.peopletrackerapp.ui.map.MapScreen
import org.comp90018.peopletrackerapp.ui.posts.PostsScreen
import org.comp90018.peopletrackerapp.ui.profile.EditProfileScreen
import org.comp90018.peopletrackerapp.ui.profile.UserProfileScreen
import org.comp90018.peopletrackerapp.ui.signup.SignupScreen
import org.comp90018.peopletrackerapp.ui.start.StartScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
    appState: PeopleTrackerAppState,
    modifier: Modifier,
) {
    // Navigation animation logic based on example here: https://proandroiddev.com/screen-transition-animations-with-jetpack-navigation-17afdc714d0e
    NavHost(
        navController = appState.navController,
        startDestination = Routes.Start.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(0)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(0)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(0)
            )
       },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(0)
            )
        }
    ) {
        composable(
            route = Routes.Home.route) {
            HomeScreen(navController = appState.navController)
        }
        composable(route = Routes.UserProfile.route
        ) {
            UserProfileScreen(navController = appState.navController)
        }
        composable(route = Routes.Signup.route){
            SignupScreen(navController = appState.navController)
        }
        composable(route = Routes.Login.route) {
            LoginScreen(navController = appState.navController)
        }
        composable(route = Routes.Start.route) {
            StartScreen(navController = appState.navController)
        }
        composable(route = Routes.CreateCircle.route) {
            CreateCircleScreen(navController = appState.navController)
        }
        composable(route = Routes.JoinCircle.route) {
            JoinCircleScreen(navController = appState.navController)
        }
        composable(
            route = Routes.Group.route + "/{circleID}",
            arguments = listOf(navArgument("circleID") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val circleID = backStackEntry.arguments?.getString("circleID")

            val viewModel: MapViewModel = hiltViewModel()
            GroupScreen(navController = navController, circleID!!, viewModel)
        }

//        composable(route = Routes.Map.route) {
//            MapScreen(navController = appState.navController)
//        }

        composable(route = Routes.Circle.route) {
            CircleScreen(navController = appState.navController)
        }

        composable(route = Routes.EditProfile.route) {
            EditProfileScreen(navController = appState.navController)
        }

//        composable(route = Routes.Posts.route) {
//            PostsScreen(navController = appState.navController)
//        }
        composable(
            route = Routes.Posts.route,
            arguments = listOf(navArgument("circleID") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val circleID = backStackEntry.arguments?.getString("circleID")
            PostsScreen(navController = navController, circleID = circleID!!)
        }


    }

}
