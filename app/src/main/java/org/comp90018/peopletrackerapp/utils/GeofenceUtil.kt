package org.comp90018.peopletrackerapp.utils

// GeofenceUtil.kt
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.comp90018.peopletrackerapp.service.location.GeofenceBroadcastReceiver

object GeofenceUtil {

    private const val PENDING_INTENT_REQUEST_CODE = 0

    fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE, intent, flags)
    }
}