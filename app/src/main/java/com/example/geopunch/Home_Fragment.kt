package com.example.geopunch

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Home_Fragment : Fragment(), OnMapReadyCallback {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var startbtn: AppCompatImageButton
    private lateinit var manual: AppCompatImageButton
    private lateinit var mapFrag : FragmentContainerView
    private lateinit var manuallayout : ConstraintLayout
    private var btnclicked: Boolean? = null

    private lateinit var geofencingClient: GeofencingClient
    private val geo_Id = "MyGeofence"
    private var geofencelist  : MutableList<Geofence> = mutableListOf()
    private val entry_latitude = 26.267775
    private val entry_longitude = 81.505404
    private val geoRad = 278F
    private val expiration_time = 10000000000000

    private lateinit var mMap: GoogleMap


    private val targetLatLng = LatLng(26.264578, 81.5047300)
    private val radius = 20.0

    private lateinit var locationUpdateReceiver: BroadcastReceiver
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        createNotificationChannel()

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted && (btnclicked !=null) ) {
                startLocationService()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Request permissions if necessary
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        }

        // Register receiver for location updates
        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
                val latLng = LatLng(latitude, longitude)

                // Update map and coordinates
                updateMapAndCoordinates(latLng)
            }
        }
        requireContext().registerReceiver(locationUpdateReceiver, IntentFilter("LOCATION_UPDATE"),
            Context.RECEIVER_NOT_EXPORTED)


    }




    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_, container, false)

        // Initialize views
        startbtn = view.findViewById(R.id.startbtn)


        // Set up button click listener
        startbtn.setOnClickListener {
            if(btnclicked ==null){
                btnclicked= true
            }
            else
            {
                btnclicked= !btnclicked!!
            }
            // Check location permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {
                if(btnclicked== true){
                    startLocationService()}
                else{
                    stopLocationService()
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
                }
            }
        }

        // Initialize the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        //Face Recognition
//        val options = FaceDetectorOptions.Builder()
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//            .enableTracking() // Useful for tracking the same face over frames
//            .build()
//
//        val detector = FaceDetection.getClient(options)
//
//        fun detectFaces(image: InputImage) {
//            detector.process(image)
//                .addOnSuccessListener { faces ->
//                    for (face in faces) {
//                        // Face detected, implement face recognition logic here
//                    }
//                }
//                .addOnFailureListener {
//                    // Handle failure
//                }
//        }
//
//        for (face in faces) {
//            val leftEyeOpenProbability = face.leftEyeOpenProbability
//            val rightEyeOpenProbability = face.rightEyeOpenProbability
//
//            if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
//                if (leftEyeOpenProbability < 0.4 && rightEyeOpenProbability < 0.4) {
//                    // Both eyes are closed (possible blink)
//                }
//            }
//        }



        // Create the NotificationChannel.
        val name = "Work"
        val descriptionText = "geofencing"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(mChannel)
        //mChannel.
        val textTitle = "You hae checked in "
        val textContent = "dggdgfdf"

        // Create an explicit intent for an Activity in your app.
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //Geofencing
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        geofencelist.add(Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(geo_Id)

            // Set the circular region of this geofence.
            .setCircularRegion(
                entry_latitude,
                entry_longitude,
                geoRad
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(expiration_time)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT )
            .setLoiteringDelay(10000)
            // Create the geofence.
            .build())

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = "com.google.android.gms.location.ACTION_GEOFENCE_TRANSITION"
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            PendingIntent.getBroadcast(requireContext(), 0, intent, flags)
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences added
                val fence = LatLng(entry_latitude, entry_longitude)
                mMap.addMarker(MarkerOptions().position(fence).title("Marker in center"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(fence))

                val geofenceCircle = mMap.addCircle(
                    CircleOptions()
                        .center(LatLng(entry_latitude, entry_longitude))
                        .radius(geoRad.toDouble())
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(64, 0, 125, 0))
                )
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }

        manual = view.findViewById(R.id.manual_but)
        mapFrag = view.findViewById(R.id.map)
        manuallayout = view.findViewById(R.id.manual_layout)
        var isManual : Boolean = true


        var layoutHeight = getView()?.height

        //manual button
        manual.setOnClickListener(){
            if (isManual) {
                val params = manuallayout.layoutParams

                if (layoutHeight != null) {
                    params.height = (layoutHeight/1.2).toInt()
                }   // Set the height
                manuallayout.layoutParams = params
                isManual = false

            }else{
                val params = manuallayout.layoutParams
                params.height = 1// Set the height
                manuallayout.layoutParams = params
                isManual = true
            }
        }
        //slider
        val slider = view.findViewById<SlideToActView>(R.id.sliderbut)

        slider.isReversed = false


        slider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{
            override fun onSlideComplete(view: SlideToActView) {

                if(slider.isReversed) {
                    //slided back
                    Toast.makeText(context, "Slide reversedComplete", Toast.LENGTH_SHORT).show()
                    slider.outerColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.innerColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.text = "       Check in >>"
                    slider.textColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.iconColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                }else{
                    //slided forward
                    Toast.makeText(context, "Slide Complete", Toast.LENGTH_SHORT).show()
                    slider.innerColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.outerColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.text = " << Check out      "
                    slider.textColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.iconColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                }
            }
        }
    }

    private fun startLocationService() {

        Toast.makeText(context, "Location service started", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().startService(intent)


    }

    private fun stopLocationService() {
        // This method should stop your location service
        Toast.makeText(context, "Location service stopped", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().stopService(intent)
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GeoPunch Channel"
            val descriptionText = "Channel for GeoPunch notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("GeoPunchChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

   // @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

       if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
           == PackageManager.PERMISSION_GRANTED) {
           mMap.isMyLocationEnabled = true
       } else {
           requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
       }

    }



    fun updateMapAndCoordinates(latLng: LatLng) {
        // Add marker to the map
        mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))

        // Update the map to show the new marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Update the TextView with the current coordinates

    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofencelist)
        }.build()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}