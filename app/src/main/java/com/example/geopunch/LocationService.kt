package com.example.geopunch


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Define the target location
    private val targetLatLng = LatLng(26.264578, 81.5047300) // Example: New Delhi coordinates
    private val radius = 20.0 // Radius in meters

    override fun onCreate() {
        super.onCreate()

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        startForegroundService()

        // Set up location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
//                    checkDistanceFromTarget(location.latitude, location.longitude)
                }
            }
        }
        /*
        onCreate(): Called when the service is first created,setting up initial configurations.

fusedLocationClient: Initializes the location client using LocationServices.getFusedLocationProviderClient(this).

createNotificationChannel(): Sets up the notification channel (required for Android O and later).

startForegroundService(): Starts the service in the foreground with a persistent notification.

locationCallback: Defines the callback for handling location updates. onLocationResult is called when the location is updated.

startLocationUpdates(): Requests location updates from the location client.
         */

        startLocationUpdates()
    }

    private fun createNotificationChannel() {
        val channelId = "location_service_channel"
        val channelName = "Location Service"
        val channelDescription = "Channel for location service notifications."
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    /*

    createNotificationChannel(): Creates a notification channel for Android 8.0 (Oreo) and higher.

channelId: Unique identifier for the notification channel.

channelName: User-visible name for the channel.

channelDescription: Description of the channel's purpose.

importance: The importance level of notifications posted to this channel (e.g., IMPORTANCE_LOW means notifications are not intrusive).
NotificationChannel: Creates and configures the notification channel.
notificationManager.createNotificationChannel(channel): Registers the channel with the system.
     */

    private fun startForegroundService() {
        val channelId = "location_service_channel"

        // Notification for foreground service to indicate tracking
        val persistentNotification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked.")
            .setSmallIcon(R.drawable.notification_bell)
            .setOngoing(true) // Persistent notification
            .build()

        startForeground(1, persistentNotification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 30_000 // 30 seconds
            fastestInterval = 15_000 // 15 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    /*
    startLocationUpdates(): Sets up and requests location updates.
LocationRequest.create(): Creates a new LocationRequest object.
interval: How frequently the app receives location updates (30 seconds here).
fastestInterval: The fastest rate at which the app can receive location updates (15 seconds here).
priority: The accuracy of location updates (e.g., PRIORITY_HIGH_ACCURACY for GPS-based updates).
ActivityCompat.checkSelfPermission(): Checks if the app has the required location permissions.
fusedLocationClient.requestLocationUpdates(): Requests location updates using the specified LocationRequest and LocationCallback.
     */

//    private fun checkDistanceFromTarget(latitude: Double, longitude: Double) {
//        val distance = FloatArray(1)
//        Location.distanceBetween(
//            latitude, longitude,
//            targetLatLng.latitude, targetLatLng.longitude,
//            distance
//        )
//
//        if (distance[0] > radius) {
//            sendNotification("You have moved outside the 50-meter radius!")
//        }
//
//        // Broadcast the location to MainActivity
//        val intent = Intent("LOCATION_UPDATE").apply {
//            putExtra("latitude", latitude)
//            putExtra("longitude", longitude)
//        }
//        sendBroadcast(intent)
//    }

    @SuppressLint("NotificationPermission")
    private fun sendNotification(message: String) {
        val notificationId = 2
        val channelId = "location_service_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_bell)
            .setContentTitle("Location Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
