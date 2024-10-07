package com.example.geopunch.user

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geopunch.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeofenceReceiver"
        const val CHANNEL_ID = "GeofenceChannel"
    }
    private var userId:String?=null
    private var isInside:Boolean?=null
    private var status:String="Not Working"
    private var currentEntryDocId: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent!!.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent!!.geofenceTransition

        // Get the triggered geofences
        val triggeringGeofences = geofencingEvent!!.triggeringGeofences

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                context!!,
                geofenceTransition,
                triggeringGeofences!!
            )



            // Send notification and log the transition details.
            sendNotification(context, geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                context!!,
                geofenceTransition,
                triggeringGeofences!!
            )



            // Send notification and log the transition details.
            sendNotification(context, geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun getGeofenceTransitionDetails(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entering"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exiting"
            else -> "Unknown Transition"
        }

        val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
        val triggeringGeofencesIdsString = triggeringGeofencesIdsList.joinToString(", ")

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun sendNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, "Geofence Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_bell)
            .setContentTitle("Geofence Transition")
            .setContentText(message)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

    private fun uploadCheckIn(location: String) {
        val db = FirebaseFirestore.getInstance()
        val time = Timestamp.now()

        // Converting the date to a formatted string
        val date = time.toDate()
        val timeInMillis = time.toDate().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        var Id: String? = null


        Id = userId + "_" + timeInMillis

        val entry = Entry(
            EntryId = Id,
            EntryTime = time,
            ExitTime = null,
            Status = "Working",
            Date = formattedDate,
            latLng = LatLng(12.25121212, 66.6556121), // Replace with the actual location
            OfficeLocation = location,
            mode = "Automatic"
        )

        // Add entry document for check-in
        db.collection("Employees Data")
            .document(userId.toString())
            .collection("Entries Data")
            .document(Id)
            .set(entry)
            .addOnSuccessListener {
                currentEntryDocId = Id // Store the document ID
                //  Toast.makeText(this, "Uploaded check-in time", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                // Toast.makeText(this, "Error during check-in: ${e.message}", Toast.LENGTH_LONG).show()
            }

        status = "Working at $location" // Correct the status format
        // Toast.makeText(this, "Status changed to $status", Toast.LENGTH_SHORT).show()
    }

        private fun uploadCheckOut(location: String) {

            val db = FirebaseFirestore.getInstance()
            val time = Timestamp.now()

            // Converting the date to a formatted string
            val date = time.toDate()
            val timeInMillis = time.toDate().time
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(date)
            var Id: String? = null


            Id = userId + "_" + timeInMillis

            val entry = Entry(
                EntryId = Id,
                EntryTime = time,
                ExitTime = null,
                Status = "Working",
                Date = formattedDate,
                latLng = LatLng(12.25121212, 66.6556121), // Replace with the actual location
                OfficeLocation = location,
                mode = "Automatic"
            )
            // Toast.makeText(this, "Checkout called", Toast.LENGTH_SHORT).show()
            currentEntryDocId?.let { docId ->
                db.collection("Employees Data")
                    .document(userId.toString())
                    .collection("Entries Data")
                    .document(docId) // Use the correct document ID
                    .update(
                        "exitTime",
                        FieldValue.serverTimestamp(), // Ensure ExitTime field name matches Firestore
                        "status",
                        "Complete"
                    )
                    .addOnSuccessListener {
                        //   Toast.makeText(this, "Uploaded checkout time", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        //    Toast.makeText(this, "Error updating checkout: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            status = "Not Working"
            //  Toast.makeText(this, "Status changed to $status", Toast.LENGTH_SHORT).show()
        }
    data class Entry(
        val EntryId: String,
        val ExitTime: Timestamp?,
        val EntryTime: Timestamp,
        val Status:String,
        val Date: String,
        val latLng: LatLng,
        val OfficeLocation:String,
        val mode:String
    )
    }


