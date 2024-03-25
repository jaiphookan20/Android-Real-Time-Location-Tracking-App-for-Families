package org.comp90018.peopletrackerapp.ui.posts

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.utils.getDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    navController: NavController,
    circleID: String,
    viewModel: PostViewModel = hiltViewModel()
) {
    val userId = viewModel.userId
    val users by viewModel.users.collectAsState()
    val imagesList by viewModel.imagesFlow.collectAsState()
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val (fullScreenImage, setFullScreenImage) = remember { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            // Save the image here using MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            // Get the content resolver and insert the new image
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            // Open an output stream using the URI and save your bitmap
            // Inside your `ActivityResultLauncher` for the `TakePicturePreview` contract
            uri?.let { imageUri ->
                val outputStream = resolver.openOutputStream(imageUri)
                outputStream?.use { outStream ->
                    // If the stream is not null, use it to compress the bitmap
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                } ?: run {
                    // Handle the situation where you couldn't open an output stream
                    Log.e("SaveImage", "Could not open an output stream for the given URI")
                }


                // Now that the image is saved, mark it as no longer pending
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }

            viewModel.saveImage(
                bitmap = bitmap,
                userId = userId,
                circleId = circleID,
                onSuccess = {
                    // Handle successful upload
                    Log.d("SaveImage", "Image successfully saved to Firestore")
                },
                onError = { exception ->
                    // Handle any errors here
                    Log.e("SaveImage", "Error saving image to Firestore", exception)
                }
            )
        } else {
            // Handle the error situation when the bitmap is null
            Log.d("SaveImage", "Image not stored into database yet")
        }
    }

    val cameraPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Use the takePictureLauncher to take a picture.
            takePictureLauncher.launch(null)
        } else {
            // Permission is denied. Explain to the user why the permission is needed.
        }
    }

    /* Fetch images for the circle */
    LaunchedEffect(circleID) {
        circleID?.let {
            viewModel.fetchImagesForCircle(it)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Posts",
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize
                    )
                 },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cameraPermissionsLauncher.launch("android.permission.CAMERA")
                    }) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Take Picture")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            // Show loading indicator in the center
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues)) {
                if (fullScreenImage != null) {
                    FullScreenImage(imageUrl = fullScreenImage!!) {
                        setFullScreenImage(null) // To dismiss the full screen image
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 7.dp)
                    ) {
                        items(imagesList.sortedByDescending { it.timestamp }) { imagePost ->

                            // date and time
                            Box(
                                modifier = Modifier
                                    .weight(0.2f)
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = getDateTime(imagePost.timestamp), // Replace with your timestamp
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                )
                            }

                            // User item (including avatar, user name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp), // You can adjust this value for top margin
                            ) {
                                Spacer(modifier = Modifier.weight(0.04f))

                                Box(
                                    modifier = Modifier
                                        .weight(0.16f)
                                        .align(Alignment.Top)
                                ) {
                                    val user = users[imagePost.userId]
                                    if (user == null) {

                                        LaunchedEffect(imagePost.userId) {
                                            viewModel.getUserFromImagePost(imagePost.userId)
                                        }
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    } else {
                                        // Use the UserItem composable to display the user details
                                        UserItem(user = user)
                                    }
                                }

                                // posted picture
                                Box(
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imagePost.imageUrl),
                                        contentDescription = "Posted Image",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { setFullScreenImage(imagePost.imageUrl) }
                                            .aspectRatio(1f) // Maintain the aspect ratio if needed
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .weight(0.2f)
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxWidth()
                            ) {
                                Divider(
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    thickness = 0.3.dp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth(0.9F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user.profileImage.isNullOrEmpty()) {
            // Display placeholder if profile image is null or empty
            Image(
                painter = painterResource(R.drawable.user_photo_placeholder),
                contentDescription = "User profile pic",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        } else {
            // Display user's profile image if it is not null or empty
            Image(
                painter = rememberAsyncImagePainter(model = user.profileImage),
                contentDescription = "User profile pic",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        }
        // Display the user's name dynamically
        Text(
            text = user.username ?: "Anonymous", // Fallback to "Anonymous" if username is null
        )
    }
}

@Composable
fun FullScreenImage(imageUrl: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = "Full-Screen Image",
            modifier = Modifier
                .fillMaxSize()
//                .padding(1.dp)
                .aspectRatio(1f), // Maintain the aspect ratio if needed
//                .align(Alignment.Center)
            contentScale = ContentScale.Fit // Image will fit within the bounds of the container, maintaining aspect ratio
        )
    }
}