package org.comp90018.peopletrackerapp.ui.posts

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.comp90018.peopletrackerapp.models.ImagePost
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.LogService
import org.comp90018.peopletrackerapp.models.service.StorageService
import org.comp90018.peopletrackerapp.ui.PeopleTrackerViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val storage: StorageService,
    private val accountService: AccountService,
    logService: LogService
): PeopleTrackerViewModel(logService) {
    // queue

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _imagesFlow = MutableStateFlow<List<ImagePost>>(emptyList())
    val imagesFlow: StateFlow<List<ImagePost>> = _imagesFlow.asStateFlow()
    val userId = accountService.currentUserId
    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    val users: StateFlow<Map<String, User>> = _users.asStateFlow()
    val errorMessage = MutableLiveData<String?>()
    val db = FirebaseFirestore.getInstance()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()



    fun fetchImagesForCircle(circleId: String) {
        _isLoading.value = true // Set loading to true when fetching starts
        db.collection("posts")
            .whereEqualTo("circleId", circleId)
            .get()
            .addOnSuccessListener { documents ->
                val images = documents.toObjects(ImagePost::class.java)
                _imagesFlow.value = images // Update the images flow
                _isLoading.value = false // Set loading to false when fetching is done
            }
            .addOnFailureListener { e ->
                errorMessage.postValue("Error fetching images: ${e.message}")
                _isLoading.value = false // Set loading to false on error
            }
    }

    fun getUserFromImagePost(userId: String) {
        viewModelScope.launch {
            // Only fetch the user if it's not already in the cache
            if (!_users.value.containsKey(userId)) {
                getUser(userId)?.let { user ->
                    // User is not null here, so we can safely add it to the map
                    // Create a new User object with the fetched data
                    val updatedUser = User(user.userID, user.username, user.profileImage)
                    _users.update { currentUsers ->
                        currentUsers + (userId to updatedUser)
                    }
                }
            }
        }
    }

    private suspend fun getUser(userId: String): User? =
        db.collection("users").document(userId).get().await().toObject()


    fun saveImage(bitmap: Bitmap, userId: String, circleId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        // Get the current time for naming the image file and to use as a timestamp
        val timestamp = System.currentTimeMillis()
        val fileName = "${timestamp}_${userId}_image.jpg"

        // Get references to Firestore and Firebase Storage
        val db = FirebaseFirestore.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/posts/$fileName")

        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Upload image to Firebase Storage
        storageRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    // Create a new post object
                    val post = hashMapOf(
                        "userId" to userId,
                        "circleId" to circleId,
                        "timestamp" to timestamp,
                        "imageUrl" to downloadUrl
                    )

                    // Add a new document to the 'posts' collection
                    db.collection("posts").add(post)
                        .addOnSuccessListener {
                            fetchImagesForCircle(circleId)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError(e)
                        }
                }?.addOnFailureListener { e ->
                    onError(e)
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
