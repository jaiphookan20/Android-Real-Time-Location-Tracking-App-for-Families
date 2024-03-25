/*
Copyright 2022 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.comp90018.peopletrackerapp.models.service.impl

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
  private val auth: FirebaseAuth,
  private val firestore: FirebaseFirestore,
  private val messaging: FirebaseMessaging
) : AccountService {

  override val currentUserId: String
    get() = auth.currentUser?.uid.orEmpty()

  override val hasUser: Boolean
    get() = auth.currentUser != null

  override val currentUser: Flow<User>
    get() = callbackFlow {
      val listener =
        FirebaseAuth.AuthStateListener { auth ->
          val currentUser = auth.currentUser

          if(currentUser != null) {
            val user = User(
              userID = currentUser.uid,
              username = currentUser.displayName,
              email = currentUser.email,
              profileImage = currentUser.photoUrl.toString(),
              fcmToken = "",
            )

            val userDocRef = firestore.collection("users").document(currentUserId)
            userDocRef.get()
              .addOnSuccessListener { docSnapshot ->
                if (docSnapshot.exists()) {
                  val userDoc = docSnapshot.toObject(User::class.java)
                  if (userDoc != null){
                    val updatedUser = user.copy(
                      firstName = userDoc.firstName,
                      lastName = userDoc.lastName,
                      circlesJoined = userDoc.circlesJoined,
                      latitude = userDoc.latitude,
                      longitude = userDoc.longitude,
                      fcmToken = userDoc.fcmToken
                    )
                    this.trySend(updatedUser)
                  }
                }
              }
          }
        }
      auth.addAuthStateListener(listener)
      awaitClose { auth.removeAuthStateListener(listener) }
    }

  override suspend fun authenticate(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password).await()
    // Generate token and store into document
    messaging.token.addOnSuccessListener { token ->
      Log.d("FCMTOKEN" ,"New Token: $token")

      // subscribe to existing circle notifications
      firestore.collection("users").document(currentUserId).get()
        .addOnSuccessListener { snap ->
          val circles = snap.get("circlesJoined") as? List<String> ?: emptyList()
          for (circle in circles) {
            Log.d(TAG, "Subscribed nots for: $circle")
            val topic = "$circle"
            messaging.subscribeToTopic(topic)
          }
        }
    }
  }

  override suspend fun sendRecoveryEmail(email: String) {
    auth.sendPasswordResetEmail(email).await()
  }

  override suspend fun createUserAccount(email: String, password: String, username: String, firstName: String, lastName: String) {
    auth.createUserWithEmailAndPassword(email, password)
      .addOnSuccessListener {
        Log.d("ACCOUNT_SERVICE", "User created: $it.")
        val user = it.user

        val profileUpdates = userProfileChangeRequest {
          displayName = username
          // Add photoURI here if necessary
           photoUri = Uri.parse("URL HERE")
        }
        firestore
          .collection("users")
          .document(user!!.uid)
          .set(User(
            userID = user.uid,
            username = username,
            email = email,
            firstName = firstName,
            lastName = lastName
          ))
          .addOnSuccessListener {
            Log.d(TAG, "User doc created!")
            user.updateProfile(profileUpdates)
              .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  Log.d(TAG, "User profile updated.")
                }
              }
          }
        // Generate new token
        messaging.token.addOnSuccessListener { token ->
          Log.d("FCMTOKEN", "New Token: $token")
        }
      }
  }

  override suspend fun deleteAccount() {
    auth.currentUser!!.delete().await()
    messaging.deleteToken()
  }

  override suspend fun signOut() {
    // Delete FCM token when signed out
    val userID = currentUserId
    auth.signOut()
    messaging.deleteToken()
    firestore.collection("users").document(userID).update(mapOf("fcmToken" to null)).await()
  }

  companion object {
    const val TAG = "ACCOUNT_SERVICE"
  }
}
