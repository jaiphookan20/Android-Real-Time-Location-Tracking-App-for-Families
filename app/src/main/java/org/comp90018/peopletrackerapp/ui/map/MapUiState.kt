package org.comp90018.peopletrackerapp.ui.map

import android.content.Context
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.comp90018.peopletrackerapp.models.Circle

data class MapUiState(
    var selectedCircle: Circle? = null,
    var geofenceIsSelected: Boolean = false,
    var selectedGeofenceName : String = "",
    var selectedGeofenceID : String = "",
    var setLocation: LatLng = LatLng(-37.8136, 144.9631), // Default at melbourne
    var membersLocations: Map<String, LatLng>? = null,
    var isTracking: Boolean = false,
    var geofenceMode: Boolean = false,
    var sliderPosition: Float = 0f,
    var geofenceName: String = "",
    var geofenceCoordinate: LatLng? = null,
    var context: @RawValue Context? = null,
    )
