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
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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

    private lateinit var mMap: GoogleMap
    private lateinit var tvCoordinates: TextView

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
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            }
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
                    params.height = (layoutHeight/2.5).toInt()
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
            == PackageManager.PERMISSION_GRANTED && (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            }
        }
        // Configure your map here if needed
        mMap.addCircle(
            CircleOptions()
                .center(targetLatLng)       // Center of the circle
                .radius(20.0)             // Radius in meters
                .strokeColor(0xFFFF0000.toInt()) // Border color (Red)
                .fillColor(0x22FF0000.toInt())   // Fill color with transparency
                .strokeWidth(2f)            // Border width in pixels
        )


        mMap.addMarker(MarkerOptions().position(targetLatLng).title("marked location").icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)))
    }



    fun updateMapAndCoordinates(latLng: LatLng) {
        // Add marker to the map
        mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))

        // Update the map to show the new marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Update the TextView with the current coordinates

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