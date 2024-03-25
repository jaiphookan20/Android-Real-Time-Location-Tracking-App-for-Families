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

package org.comp90018.peopletrackerapp.models.service

import android.content.Context
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.models.Geofence
import org.comp90018.peopletrackerapp.models.Location
import org.comp90018.peopletrackerapp.models.Notification
import org.comp90018.peopletrackerapp.models.User

interface StorageService {
    val circles: Flow<List<Circle>>
    val geofences: Flow<List<Geofence>>
    suspend fun geofencesInCircle(circleID: String): Flow<List<Geofence>>
    suspend fun getUser(userId: String): User?
    // suspend fun save(task: Task): String
    // suspend fun update(task: Task)
    // suspend fun delete(taskId: String)
    suspend fun addUser(userId: String, newUser: User)
    fun getCircle(circleID: String): Flow<Circle>
    suspend fun circleChangeListener(circleID: String): Flow<Boolean>
    fun setFCMToken(token: String)
    suspend fun createCircle(newCircle: Circle): Boolean
    suspend fun getUsers(userIDs: List<String>): List<User?>

    suspend fun setUsername(userID: String, username: String)
    suspend fun circleMembers(memberIDs: List<String>): Flow<List<User>>
//    suspend fun observeMemberLocations(circleID: String): Flow<List<Location>>
    suspend fun getCurrentLocation(): Location
    suspend fun updateUserLocation(location: MutableMap<String, Any>)
    suspend fun circleLocations(memberIDs: List<String>): Flow<List<User>>
    suspend fun joinCircle(inviteCode: String): String
    suspend fun getGeofence(geofenceID: String): Geofence
    suspend fun addGeofence(circleID: String, geofence: Geofence)
    suspend fun removeGeofence(circleID: String, geofenceID: String)
    suspend fun registerGeofences(context: Context, circleIDs: List<String>)
    suspend fun monitorGeofences(context: Context): ListenerRegistration
    fun removeGeofences(context: Context)
    fun createNotification(notID: String, notification: Notification)
    suspend fun removeUserFromCircle(userId: String, circleId: String)
    suspend fun deleteCircle(circleId: String)
}
