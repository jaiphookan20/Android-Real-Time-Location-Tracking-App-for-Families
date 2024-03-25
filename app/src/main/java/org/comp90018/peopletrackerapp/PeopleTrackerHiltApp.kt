package org.comp90018.peopletrackerapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import org.comp90018.peopletrackerapp.service.location.GeofenceBroadcastReceiver

@HiltAndroidApp
class PeopleTrackerHiltApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationChannel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val locNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            locNotificationManager.createNotificationChannel(locationChannel)

            val geofenceChannel = NotificationChannel(
                GeofenceBroadcastReceiver.GEOFENCE_CHANNEL_ID,
                "Geofence",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val geoNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            geoNotificationManager.createNotificationChannel(geofenceChannel)
        }
    }
}