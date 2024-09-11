package com.example.geopunch

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import android.Manifest
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "GeoChannel"
//import com.example.geo.MapsActivity.Companion.ACTION_GEOFENCE_EVENT

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAGED = "TAGED"
    // ...
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(TAGED, "Intent is $intent")
        if (intent?.action == "com.google.android.gms.location.ACTION_GEOFENCE_TRANSITION") {
            val geofencingEvent = intent.let { GeofencingEvent.fromIntent(it) }
            Log.e(TAGED, "geofencingEvent is $geofencingEvent")
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    val errorMessage = geofencingEvent.let {
                        GeofenceStatusCodes
                            .getStatusCodeString(it.errorCode)
                    }
                    Log.e(TAG, errorMessage)
                    return
                }
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent?.geofenceTransition
            Log.e(TAG, "geofenceTransition is $geofenceTransition")

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
            ) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // Get the transition details as a String.
                val geofenceTransitionDetails = triggeringGeofences?.let {
                    getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        it
                    )
                }

                // Send notification and log the transition details.
                sendNotification(context!!, geofenceTransitionDetails)
                if (geofenceTransitionDetails != null) {
                    Log.i(TAG, geofenceTransitionDetails)
                }
            } else {
                // Log the error.
                Log.e(
                    TAG,
                    geofenceTransition.toString()
                )
            }
        }
    }


    private fun sendNotification(context:Context, message: String?) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_bell) // Replace with your notification icon
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Consider requesting the missing permission here
            return
        }

        val notificationId = 123 // You can use a unique ID for each notification
        notificationManager.notify(notificationId, builder.build())
    }
    private fun getGeofenceTransitionDetails(
        context: GeofenceBroadcastReceiver, geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited"

            else -> "Unknown Transition"
        }

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = mutableListOf<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = triggeringGeofencesIdsList.joinToString(", ")

        return "Geofence transition: $geofenceTransitionString - $triggeringGeofencesIdsString"
    }
}

