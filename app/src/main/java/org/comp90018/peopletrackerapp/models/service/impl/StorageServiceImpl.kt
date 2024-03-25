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

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.dataObjects
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.Location
import org.comp90018.peopletrackerapp.models.Notification
import org.comp90018.peopletrackerapp.models.User
import org.comp90018.peopletrackerapp.models.service.AccountService
import org.comp90018.peopletrackerapp.models.service.StorageService
import org.comp90018.peopletrackerapp.utils.GeofenceUtil
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.comp90018.peopletrackerapp.models.Geofence as GeofenceModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

class StorageServiceImpl
@Inject
constructor(
    private val firestore: FirebaseFirestore,
    private val auth: AccountService,
    private val messaging: FirebaseMessaging
) : StorageService {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val circles: Flow<List<Circle>>
        get() =
            auth.currentUser.flatMapLatest { user ->
                if (user.circlesJoined.isNotEmpty()) {
                    firestore.collection(CIRCLE_COLLECTION)
                        .whereIn(CIRCLE_ID_FIELD, user.circlesJoined).dataObjects()
                } else {
                    flowOf(emptyList())
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun circleMembers(memberIDs: List<String>): Flow<List<User>> {
        return auth.currentUser.flatMapLatest { user ->
            if (user.circlesJoined.isNotEmpty()) {
                firestore.collection(USER_COLLECTION).whereIn("userID", memberIDs).dataObjects()
            } else {
                flowOf(emptyList())
            }
        }
    }

    override fun getCircle(circleID: String): Flow<Circle> {
        val circleCollection = firestore.collection(CIRCLE_COLLECTION)
        val query = circleCollection.whereEqualTo("circleID", circleID)

        return query.snapshots().map {
            it.toObjects<Circle>()[0]
        }
    }

    override suspend fun circleChangeListener(circleID: String): Flow<Boolean> {
        firestore.collection(CIRCLE_COLLECTION).addSnapshotListener { snapshot, exception ->
            if(exception!=null) {
                return@addSnapshotListener
            }
            for(docChange in snapshot!!.documentChanges) {
                if(docChange.document.id == circleID && docChange.type == DocumentChange.Type.REMOVED) {
                    flowOf(true)
                } else {
                    flowOf(false)
                }
            }
        }
        return emptyFlow()
    }

    override suspend fun circleLocations(memberIDs: List<String>): Flow<List<User>> {
        val usersRef = firestore.collection(USER_COLLECTION)
        val query = usersRef.whereIn("userID", memberIDs)

        return query.snapshots().map {
            it.toObjects<User>()
        }
    }

    override suspend fun getUsers(userIDs: List<String>): List<User?> {
        val userCollection = firestore.collection(USER_COLLECTION)
        val users = mutableListOf<User?>()
        val query = userCollection.whereIn(USER_ID_FIELD, userIDs)
        query.get().addOnSuccessListener { snapshot ->
            val docs = snapshot.documents
            for (doc in docs) {
                val userDoc = doc.toObject<User>()
                Log.d("STORAGE", userDoc.toString())
                users.add(userDoc)
            }
        }
        return users
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val geofences: Flow<List<GeofenceModel>>
        get() =
            circles.flatMapLatest { circleObjs ->
                val circleIDs = circleObjs.map { it -> it.circleID }
                firestore.collection(GEOFENCE_COLLECTION).whereIn(CIRCLE_ID_FIELD, circleIDs)
                    .dataObjects()
            }

    override suspend fun geofencesInCircle(circleID: String): Flow<List<GeofenceModel>> {
        val usersRef = firestore.collection(GEOFENCE_COLLECTION)
        val query = usersRef.whereEqualTo(CIRCLE_ID_FIELD, circleID)

        return query.snapshots().map {
            it.toObjects<GeofenceModel>()
        }
    }


    override suspend fun getCurrentLocation(): Location {
        val snapshot = firestore
            .collection(USER_COLLECTION)
            .document(auth.currentUserId)
            .get()
            .await()

        return snapshot.toObject(Location::class.java)!!
    }

    override suspend fun updateUserLocation(location: MutableMap<String, Any>) {
        firestore.collection(USER_COLLECTION).document(auth.currentUserId).update(location)
    }

    // API Call to retrieve user document from firestore
    override suspend fun getUser(userId: String): User? =
        firestore.collection(USER_COLLECTION).document(userId).get().await().toObject()

    override suspend fun setUsername(userID: String, username: String) {
        firestore.collection(USER_COLLECTION).document(userID).update(mapOf("username" to username))
    }


    override fun setFCMToken(token: String) {
        val scope = CoroutineScope(Dispatchers.Main + Job())
        // Start a coroutine in the passed scope
        scope.launch {
            // Keep trying until currentUserId is not null and not blank
            while (auth.currentUserId == null || auth.currentUserId.isBlank()) {
                // Wait for a second before checking again
                delay(1000)
            }

            scope.cancel()
            // Now that we have a non-null and non-blank userId, we can update the FCM token
            firestore.collection(USER_COLLECTION).document(auth.currentUserId).update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "Updated FCM Token for user ID: ${auth.currentUserId}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating FCM Token", e)
                }
        }
    }

    // API Call to add new user document to firestore
    override suspend fun addUser(userId: String, newUser: User) {
        firestore.collection(USER_COLLECTION).document(userId).set(newUser).await()
    }

    override suspend fun createCircle(newCircle: Circle): Boolean = suspendCoroutine { it ->
        if (auth.hasUser) {
            val circleExists =
                firestore.collection(CIRCLE_COLLECTION).whereEqualTo("name", newCircle.name)
                    .whereEqualTo("creatorID", newCircle.creatorID).get()
                    .addOnCompleteListener { task ->
                        if (task.result.isEmpty()) {
                            firestore.collection(CIRCLE_COLLECTION).document(newCircle.circleID)
                                .set(newCircle)
                            firestore.collection(USER_COLLECTION).document(auth.currentUserId)
                                .update(
                                    USER_CIRCLES_JOINED_FIELD,
                                    FieldValue.arrayUnion(newCircle.circleID)
                                )
                                .addOnSuccessListener { _ ->
                                    // Subscribe to circle notifications
                                    Log.d(TAG, "Subscribed nots for: ${newCircle.circleID}")
                                    val token = "${newCircle.circleID}"
                                    messaging.subscribeToTopic(token)
                                    it.resume(true)
                                }
                        } else {
                            it.resume(false)
                        }
                    }
        }
    }

    override suspend fun joinCircle(inviteCode: String): String = suspendCoroutine { it ->
        var joinedCircleID = ""
        if (auth.hasUser) {
            val collectionRef = firestore.collection(CIRCLE_COLLECTION)
            val query = collectionRef.whereEqualTo(CIRCLE_INVITE_CODE_FIELD, inviteCode)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result.documents
                    if (documents.isNotEmpty()) {
                        val document = documents[0].toObject<Circle>()
                        // Check if user is already in circle
                        if (document?.members?.contains(auth.currentUserId) != true) {
                            // Add userID to member field
                            collectionRef.document(document?.circleID!!).update(
                                CIRCLE_MEMBERS_FIELD,
                                FieldValue.arrayUnion(auth.currentUserId)
                            )
                            // Add circleID to circlesJoined field
                            firestore.collection(USER_COLLECTION).document(auth.currentUserId)
                                .update(
                                    USER_CIRCLES_JOINED_FIELD,
                                    FieldValue.arrayUnion(document.circleID)
                                )
                                .addOnSuccessListener { _ ->
                                    // Subscribe to circle notifications
                                    Log.d(TAG, "Subscribed nots for: ${document.circleID}")
                                    val token = document.circleID
                                    messaging.subscribeToTopic(token)
                                    joinedCircleID = document.circleID
                                    it.resume(joinedCircleID)
                                }
                                .addOnFailureListener { e ->
                                    it.resume("Exception")
                                }
                        } else {
                            it.resume("alreadyMember")
                        }
                    } else {
                        it.resume("circleNotExist")
                    }
                }
            }
        }
    }

     override suspend fun removeUserFromCircle(userId: String, circleId: String) {
         firestore.collection(CIRCLE_COLLECTION).document(circleId).update("members", FieldValue.arrayRemove(userId))
         firestore.collection(USER_COLLECTION).document(userId).update(USER_CIRCLES_JOINED_FIELD, FieldValue.arrayRemove(circleId))
    }

    override suspend fun deleteCircle(circleId: String)  {
        val circle = firestore.collection(CIRCLE_COLLECTION).document(circleId)
        circle.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val members = doc.get("members") as? List<String>
                    if (members != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            for (member in members) {
                                removeUserFromCircle(member, circleId)
                            }
                        }
                    }
                }
            }.addOnSuccessListener {
                CoroutineScope(Dispatchers.Main).launch {
                    geofencesInCircle(circleId).collect() { geofences ->
                        for (geofence in geofences) {
                            firestore.collection(GEOFENCE_COLLECTION).document(geofence.geofenceID).delete()
                        }
                    }
                }
                firestore.collection(CIRCLE_COLLECTION).document(circleId).delete()
            }
    }

    override suspend fun getGeofence(geofenceID: String): GeofenceModel {
        return firestore
            .collection(GEOFENCE_COLLECTION)
            .document(geofenceID)
            .get()
            .await()
            .toObject<GeofenceModel>() as GeofenceModel
    }

    override suspend fun addGeofence(circleID: String, geofence: GeofenceModel) {
        firestore
            .collection(GEOFENCE_COLLECTION).document(geofence.geofenceID).set(geofence)
    }

    override suspend fun removeGeofence(circleID: String, geofenceID: String) {
        firestore
            .collection(GEOFENCE_COLLECTION)
            .document(geofenceID)
            .delete()
            .addOnSuccessListener { _ ->
//        firestore.collection(CIRCLE_COLLECTION).document(circleID)
//          .update(CIRCLE_GEOFENCE_FIELD, FieldValue.arrayRemove(geofenceID))
            }
    }

    override suspend fun registerGeofences(context: Context, circleIDs: List<String>) {
        val geofenceCollection = firestore.collection(GEOFENCE_COLLECTION)
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofencePendingIntent: PendingIntent by lazy {
            GeofenceUtil.getGeofencePendingIntent(context)
        }
        var geofenceList: MutableList<Geofence> = mutableListOf()
        geofenceCollection
            .whereIn("circleID", circleIDs)
            .get()
            .addOnSuccessListener { task ->
                for (doc in task.documents) {
                    val geofenceData = doc.toObject(GeofenceModel::class.java)
                    val geofence = Geofence.Builder()
                        .setRequestId(doc.id)
                        .setCircularRegion(
                            geofenceData!!.centerLatitude,
                            geofenceData.centerLongitude,
                            geofenceData.radius
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()

                    geofenceList.add(geofence)
                }
                if (geofenceList.isEmpty()) {
                    Log.d(TAG, "No geofences to be tracked")
                    return@addOnSuccessListener
                }
                val geofenceRequest = GeofencingRequest.Builder()
                    .addGeofences(geofenceList)
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build()
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "New Geofence registered successfully ${geofenceList.map { g -> g.requestId }}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Could not register new geofences, $e")
                        }
                }
            }
    }

    override suspend fun monitorGeofences(context: Context): ListenerRegistration {
        val geofenceCollection = firestore.collection(GEOFENCE_COLLECTION)
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofencePendingIntent: PendingIntent by lazy {
            GeofenceUtil.getGeofencePendingIntent(context)
        }
        return geofenceCollection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            var userCircles = emptyList<String>()
            firestore
                .collection(USER_COLLECTION)
                .document(auth.currentUserId)
                .get()
                .addOnSuccessListener { task ->
                    userCircles = task.get("circlesJoined") as List<String>
                }


            for (docChange in snapshots!!.documentChanges) {
                if (docChange.type == DocumentChange.Type.ADDED) {
                    val newGeofenceData = docChange.document.toObject(GeofenceModel::class.java)
                    if (newGeofenceData.circleID in userCircles) {
                        val newGeofence = Geofence.Builder()
                            .setRequestId(docChange.document.id)
                            .setCircularRegion(
                                newGeofenceData.centerLatitude,
                                newGeofenceData.centerLongitude,
                                newGeofenceData.radius,
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()

                        val geofenceRequest = GeofencingRequest.Builder()
                            .addGeofence(newGeofence)
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .build()

                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
                                .addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "New Geofence registered successfully ${docChange.document.id}"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Could not register new Geofence, $e")
                                }
                        }
                    }
                } else if (docChange.type == DocumentChange.Type.REMOVED) {
                    geofencingClient.removeGeofences(listOf(docChange.document.id))
                        .addOnSuccessListener {
                            Log.d(TAG, "Succesfully removed Geofence ${docChange.document.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to remove geofence ${docChange.document.id}\n$e")
                        }
                }
            }
        }
    }

    override fun removeGeofences(context: Context) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofencePendingIntent: PendingIntent by lazy {
            GeofenceUtil.getGeofencePendingIntent(context)
        }
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "All geofences removed successfully")
            }
    }

    override fun createNotification(notID: String, notification: Notification) {
        firestore.collection(NOTIFICATION_COLLECTION).document(notID).set(notification)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully created notification message!")
            }
    }

    companion object {
        private const val TAG = "STORAGE_SERVICE"
        private const val USER_COLLECTION = "users"
        private const val USER_ID_FIELD = "userID"
        private const val USER_CIRCLES_JOINED_FIELD = "circlesJoined"
        private const val CIRCLE_COLLECTION = "circles"
        private const val CIRCLE_ID_FIELD = "circleID"
        private const val CIRCLE_INVITE_CODE_FIELD = "inviteCode"
        private const val CIRCLE_MEMBERS_FIELD = "members"
        private const val GEOFENCE_COLLECTION = "geofences"
        private const val NOTIFICATION_COLLECTION = "notifications"
    }
}
