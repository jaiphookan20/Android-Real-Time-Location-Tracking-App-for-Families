package org.comp90018.peopletrackerapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector

// Navigation flow taken from following tutorials
// 'Jetpack Compose Navigation for Beginners' - https://www.youtube.com/watch?v=4gUeyNkGE3g
// 'Navigation Basics in Compose' -  https://www.youtube.com/watch?v=glyqjzkc4fk
sealed class Routes (
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null,
    val showBottomBar: Boolean?=null,
) {
    object Start: Routes(route = "start_screen")
    object Login: Routes(route = "login_screen")
    object Signup: Routes(route = "signup_screen")

    object Home: Routes(
        route = "home_screen",
        title = "Home",
        icon = Icons.Outlined.Place,
        showBottomBar = true
    )
    object UserProfile: Routes(
        route = "profile_screen",
        title = "Profile",
        icon = Icons.Outlined.Person,
        showBottomBar = true
    )
    object Circles: Routes(
        route = "circle_screen",
        title = "Circles",
        icon = Icons.Outlined.Groups,
        showBottomBar = true
    )

    object Map: Routes(
        route = "map_screen",
        title = "Map",
        icon = Icons.Outlined.Map,
        showBottomBar = true
    )
    object EditProfile: Routes(route = "edit_profile_screen")
    object JoinCircle: Routes(route = "join_circle_screen")
    object CreateCircle: Routes(route = "create_circle_screen")
    object CreateGeofence: Routes(route = "add_geofence_screen")
    object Group: Routes(route = "group_screen/{circleID}")
    object JoinGroup: Routes(route = "join_group_screen")
    object CreateGroup: Routes(route = "create_group_screen")
    object Location: Routes(route = "location")
    object Circle: Routes(route = "circle_screen")
    object Posts: Routes(route = "posts/{circleID}")


    // TODO Old routing logic
    //object Home: Routes(route = "home_screen")
    //object UserProfile: Routes(route = "profile_screen")
    //object Group: Routes(route = "group_screen")
    //object Map: Routes(route = "map_screen/{circleID}")

}