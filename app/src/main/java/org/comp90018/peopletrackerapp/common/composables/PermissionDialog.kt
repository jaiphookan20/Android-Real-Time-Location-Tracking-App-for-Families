package org.comp90018.peopletrackerapp.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider()
                    Text(
                        text = if(isPermanentlyDeclined) {
                            "Grant Permission"
                        } else {
                            "Ok"
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isPermanentlyDeclined) {
                                    onGoToAppSettingsClick()
                                } else {
                                    onOkClick()
                                }
                            }
                            .padding(16.dp)
                    )
                }
            },
            title = {
                Text("Permission required")
            },
            text = {
                Text(
                    text = permissionTextProvider.getDescription(
                        isPermanentlyDeclined = isPermanentlyDeclined
                    )
                )
            },
            modifier = modifier
        )
    }
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean):String
}

class FineLocationPermissionTextProvider:PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) {
            "It seems you permanently declined fine location permission. "+
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your location so that your friends and family " +
                    "can track your location and safety."
         }
    }
}

class CoarseLocationPermissionTextProvider:PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) {
            "It seems you permanently declined coarse location permission. "+
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your location so that your friends and family " +
                    "can track your location and safety."
        }
    }
}

class BackgroundLocationPermissionTextProvider:PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) {
            "It seems you permanently declined background location permission. "+
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your location so that your friends and family " +
                    "can track your location and safety."
        }
    }
}

class PushNotificationPermissionTextProvider:PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) {
            "It seems you permanently declined notifications. "+
                    "You can go to the app settings to grant it."
        } else {
            "This app needs notifications permissions to alert of any friends or family " +
                    "that enter or exit geofences."
        }
    }
}

class ForegroundPermissionTextProvider:PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) {
            "It seems you permanently declined this app to run in the foreground "+
                    "You can go to the app settings to grant it."
        } else {
            "This app needs foreground permissions to track your location " +
                    "and notify you of any actions."
        }
    }
}