package org.comp90018.peopletrackerapp.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val auth: FirebaseAuth,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    var user = accountService.currentUser
    var uiState = mutableStateOf(ProfileUiState())
    var isLoadingSignout = mutableStateOf(false)

    fun uploadAndSetProfileImage(data: ByteArray, user: User, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        // Get the current time for naming the image file and to use as a timestamp
        val timestamp = System.currentTimeMillis()
        val fileName = "${timestamp}_${user.userID}_image.jpg"

        // Get references to Firestore and Firebase Storage
        val db = FirebaseFirestore.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/profile/$fileName")

        // Upload image to Firebase Storage
        storageRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result.toString()
                        val profileUpdates = userProfileChangeRequest {
                             photoUri = Uri.parse(downloadUrl)
                        }
                        auth.currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { taskTwo ->
                                if (taskTwo.isSuccessful) {
                                    Log.d("ProfileView", "User profile updated.")
                                    // Update the user document in the 'users' collection
                                    db.collection("users").document(user.userID!!)
                                        .update("profileImage", downloadUrl)
                                        .addOnSuccessListener {
                                            onSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            onError(e)
                                        }
                                }
                            }
                    } else {
                        task.exception?.let { exception ->
                            onError(exception)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    fun onClickLogout(navController: NavController) {
        navController.navigate(Routes.Start.route) {
            launchSingleTop = true
            popUpTo(Routes.UserProfile.route) {
                inclusive = true
            }
        }
        launchCatching (isLoadingSignout) {
            isLoadingSignout.value = true
            accountService.signOut()
            isLoadingSignout.value = false
        }
    }
}