package org.comp90018.peopletrackerapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme

@Composable
fun EditProfileScreen(
    navController: NavController
) {
    PeopleTrackerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "to be implemented",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
                Button(onClick = { navController.navigate(Routes.UserProfile.route) }) {
                    Text(text = "Back")
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun EditProfileScreenPreview(){
    EditProfileScreen(navController = rememberNavController())
}