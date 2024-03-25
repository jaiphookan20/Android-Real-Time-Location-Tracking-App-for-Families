package org.comp90018.peopletrackerapp.service.location

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.models.Geofence
import org.comp90018.peopletrackerapp.models.service.StorageService
import javax.inject.Inject

@AndroidEntryPoint
class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    @Inject lateinit var storage: StorageService
    @Inject lateinit var auth: FirebaseAuth
    private lateinit var geofences: Flow<List<Geofence>>

    private lateinit var circleID: String

    val TAG = "LocationService"

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            circleID = intent.getStringExtra("circleID").toString()
            serviceScope.launch {
                try {
                    when(intent.action) {
                        ACTION_START -> start()
                        ACTION_STOP -> stop()
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient
            .getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace()}
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val timestamp = System.currentTimeMillis()
                val userLocationData: MutableMap<String, Any> = hashMapOf(
                    "latitude" to lat.toDouble(),
                    "longitude" to long.toDouble(),
                    "timestamp" to timestamp
                )

                try {
                    storage.updateUserLocation(userLocationData)
                    Log.d(TAG, "Updating location to Firestore!")
                } catch(e: Exception) {
                    Log.e("Location Service", "Error updating location",e)
                }

                // Check GEOFENCE ACTION

                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent leaks on destroying service (closing app, etc)
        serviceScope.cancel()
    }

//    private fun createNotificationMessage(action: String, geofence: Geofence) {
//        val username = auth.currentUser?.displayName
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
//        val current = LocalDateTime.now().format(formatter)
//        val title = "People Tracker: Geofence Trigger"
//        val message = "$username has $action ${geofence.name} @ $current"
//        PushNotification(
//            NotificationData(title, message),
//            circleID
//        ).also {
//            sendNotification(it)
//        }
//    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}