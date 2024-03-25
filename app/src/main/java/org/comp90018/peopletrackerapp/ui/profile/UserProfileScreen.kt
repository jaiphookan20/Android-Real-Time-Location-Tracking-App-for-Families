package org.comp90018.peopletrackerapp.ui.profile
import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.yalantis.ucrop.UCrop
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.navigation.Routes

import java.io.File

import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme



// TODO Improve UI, implement edit functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle(initialValue = null)

    PeopleTrackerTheme {
        Surface (modifier = Modifier.fillMaxSize()) {
            Scaffold (
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Account",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "go back"
                                )
                            }
                        },
                        actions = {}
                    )
                }
            ){ scaffoldPadding ->
                Column(
                    modifier = Modifier
                        .padding(scaffoldPadding)
                        .wrapContentSize()
                        .clip(RoundedCornerShape(24.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 36.dp,
                                horizontal = 24.dp
                            ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        UserProfileCard(user?.profileImage, user?.username)
                        user?.let { UserProfileDetails(it) }
                    }

                }

                ExpandableFAB(navController, viewModel)

            }

        }
    }

}


@Composable
fun ExpandableFAB(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    var isExpanded by remember { mutableStateOf(false) }
    val user by viewModel.user.collectAsStateWithLifecycle(initialValue = null)


    // Handle the result from UCrop
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // UCrop has a predefined key for the result URI
            val resultUri = UCrop.getOutput(result.data!!)
            // Handle the result Uri, for example, upload it
            val resultBA = context.contentResolver.openInputStream(resultUri!!)?.use {
                it.buffered().readBytes()
            }
            viewModel.uploadAndSetProfileImage(
                resultBA!!,
                user!!,
                onSuccess = {
                    // Handle successful upload
                    Log.d("SaveImage", "Image successfully saved to Firestore")
                },
                onError = { exception ->
                    // Handle any errors here
                    Log.e("SaveImage", "Error saving image to Firestore", exception)
                }
            )
        }
    }

    fun startUCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped.jpg"))
        val options = UCrop.Options().apply {
            // Customize options if needed
        }

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .getIntent(context)

        uCropLauncher.launch(uCropIntent)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { startUCrop(it) } // Proceed to crop the image if the result is not null
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            // Handle the case where the user denies the permission
            Log.d("Profile", "Permission not granted")
        }
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = (-16).dp,
                y = (-16).dp
            )
            .padding(16.dp)
    ) {
        if (isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding( vertical = 10.dp ),
                ) {
                    Text(text = "Edit Profile", modifier=Modifier.padding(horizontal=3.dp))
                    FloatingActionButton(
                        onClick = { requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = ""
                        )
                    }
                }


                // TODO Add option to change avatar photo
//                // Edit Profile FAB
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding( vertical = 10.dp ),
//                ) {
//                    Text(text = "Edit Profile", modifier=Modifier.padding(horizontal=3.dp))
//                    FloatingActionButton(
//                        onClick = { navController.navigate(Routes.EditProfile.route) },
//                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Edit,
//                            contentDescription = ""
//                        )
//                    }
//                }
//
//                // Logout FAB
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding( vertical = 5.dp)
//                ) {
//                    Text(text = "Logout", modifier = Modifier.padding(horizontal=3.dp))
//                    FloatingActionButton(
//                        onClick = { viewModel.onClickLogout(navController) },
//                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Logout,
//                            contentDescription = "")
//                    }
//                }

            }
        }

        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
            },
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            if(isExpanded) {
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

// Color(0xffb3d8ff)
@Composable
fun UserProfileCard(
    photoUrl: String?,
    username: String?
) {
    Box( modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(MaterialTheme.colorScheme.inversePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular user profile photo with a border
            Box(

            ) {
                if(photoUrl != "") {
                    AsyncImage(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        model = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        onLoading = {  },
                        onError = { error ->
                            Log.e("UserProfileCard", "Error loading image: ${error.result.throwable}")

                            // Display a fallback image
                        }
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        painter = painterResource(
                            R.drawable.cute_avatar
                        ),
                        contentDescription = "User profile pic",
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // User's full name
            Text(
                text = username ?: "Anon",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun UserDetailField(label:String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Label with bold font
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                fontWeight = MaterialTheme.typography.labelSmall.fontWeight,
                color = MaterialTheme.typography.labelSmall.color
            )

        Spacer(modifier = Modifier.height(4.dp))
        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
            color = MaterialTheme.colorScheme.onSurface
        )

    }
}

@Composable
fun UserProfileDetails(user: User) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        UserDetailField("First Name", user.firstName!!, modifier = Modifier.fillMaxWidth())
        UserDetailField("Last name", user.lastName!!, modifier = Modifier.fillMaxWidth())
        UserDetailField("Email", user.email!!, modifier = Modifier.fillMaxWidth())
    }
}

