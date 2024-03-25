package org.comp90018.peopletrackerapp.ui.start

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme

@Composable
fun StartScreen(
    navController: NavController,
    viewModel: StartViewModel = hiltViewModel()
) {

    if (viewModel.hasLoggedIn()) {
        navController.navigate(Routes.Home.route) {
            launchSingleTop = true
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    PeopleTrackerTheme {
        Surface{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Image(
                    painter = painterResource(id = R.drawable.geofence),
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(300.dp)
                )

                Text(
                    text = "SafeNest",
                    fontSize = 64.sp,
                    fontWeight = FontWeight(480),
                    color = MaterialTheme.colorScheme.primary
                )

                // Button column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(Routes.Login.route) }
                    ) {
                        Text(
                            text = "Login",
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(Routes.Signup.route) }
                    ) {
                        Text(
                            text = "Signup",
                        )
                    }
                }


            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun StartScreenPreview(){
    StartScreen(navController = rememberNavController())
}