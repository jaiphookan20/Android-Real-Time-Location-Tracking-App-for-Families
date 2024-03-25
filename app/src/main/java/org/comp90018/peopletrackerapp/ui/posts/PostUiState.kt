package org.comp90018.peopletrackerapp.ui.posts

import android.content.Context
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.comp90018.peopletrackerapp.models.Circle
data class PostUiState(
    var selectedCircle: Circle? = null,
    var userId: String? = null
)