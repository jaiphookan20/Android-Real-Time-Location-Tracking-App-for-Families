package org.comp90018.peopletrackerapp.common.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ShowLocationPermissionRationale() {
    AlertDialog(
        onDismissRequest = {

        },
        title = {
            Text("Permission Required")
        },
        text = {
            Text("You need to approve this permission in order to share your location to others")
        },
        confirmButton = {
            TextButton(onClick = {
                //Logic when user confirms
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                //Logic when denies
            }) {
                Text("Deny")
            }
        }
    )
}