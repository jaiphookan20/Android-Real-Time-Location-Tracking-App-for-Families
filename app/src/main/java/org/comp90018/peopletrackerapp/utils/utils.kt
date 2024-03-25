package org.comp90018.peopletrackerapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun getDateTime(timestamp: Long?): String {
    return try {
        if(timestamp != null) {
            val netDate = Date(timestamp)
//            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mmZ", Locale.getDefault())
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("AEST")
            sdf.format(netDate)
        } else {
            "unknown"
        }
    } catch (e: Exception) {
        e.toString()
    }
}