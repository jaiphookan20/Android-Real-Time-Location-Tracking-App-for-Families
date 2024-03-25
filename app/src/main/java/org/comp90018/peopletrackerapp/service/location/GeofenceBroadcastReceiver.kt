package org.comp90018.peopletrackerapp.service.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.MainActivity
import org.comp90018.peopletrackerapp.models.Notification
import org.comp90018.peopletrackerapp.models.service.StorageService
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var storage: StorageService
    @Inject lateinit var messaging: FirebaseMessaging

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            // Handle geofence transition events

            when (geofencingEvent.geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    if(!geofencingEvent.triggeringGeofences.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val geofenceName = storage.getGeofence(geofencingEvent.triggeringGeofences!!.first().requestId).name
                            showToast(context, "You have entered $geofenceName!")
                            sendFBNotification("entered", geofencingEvent.triggeringGeofences!!.first().requestId)
                            //sendNotification(context, "Geofence Alert!", "You have entered the $geofenceName.")
                        }

                    }
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    if(!geofencingEvent.triggeringGeofences.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val geofenceName = storage.getGeofence(geofencingEvent.triggeringGeofences!!.first().requestId).name
                            showToast(context, "You have exited ${geofenceName}!")
                            sendFBNotification("exited", geofencingEvent.triggeringGeofences!!.first().requestId)
                            //sendNotification(context, "Geofence Alert!", "You have exited $geofenceName!")
                        }

                    }
                }
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun sendNotification(context: Context, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "Geofence_Channel_ID"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // You can set your app's icon here
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE)) // Change MainActivity to your main activity class

        notificationManager.notify(4, notificationBuilder.build()) // '1' here is a notification id, you can generate a unique id for every notification
    }

    // Create a notification document in firestore, which triggers
    // a cloud function to send a FCM to target devices
    private fun sendFBNotification(action: String, geofenceID: String) = CoroutineScope(serviceScope.coroutineContext).launch {
        try {
            var username = auth.currentUser?.displayName
            if (username == null) {
                username = ""
            }
            val title = "SafeNest: $username has made a move!"
            val geofence = storage.getGeofence(geofenceID)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val current = LocalDateTime.now().format(formatter)
            val message = "$username has $action ${geofence.name} @ $current"
            val destination = "${geofence.circleID}"
            val now = LocalDateTime.now()
            val expiry = now.plusDays(1)
            val notification = Notification(
                title,
                message,
                destination,
                Timestamp(Date.from(expiry.toInstant(ZoneId.of("Australia/Sydney").rules.getOffset(now))))
            )
            val notID = UUID.randomUUID().toString()
            storage.createNotification(notID, notification)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
        const val GEOFENCE_CHANNEL_ID = "Geofence_Channel_ID"
    }
}