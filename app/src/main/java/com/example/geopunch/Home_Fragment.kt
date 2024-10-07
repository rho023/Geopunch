package com.example.geopunch

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.example.geopunch.user.Home_Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
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
    private lateinit var searchlayout : ConstraintLayout
    private var btnclicked: Boolean? = null

    private lateinit var geofencingClient: GeofencingClient
    private val geo_Id = "MyGeofence"
    private var geofencelist  : MutableList<Geofence> = mutableListOf()
    private val entry_latitude = 26.267775
    private val entry_longitude = 81.505404
    private val geoRad = 278F
    private val expiration_time = 10000000000000

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mMap: GoogleMap

    private var al = 64
    private var geofenceCircle: Circle? = null





    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = "com.google.android.gms.location.ACTION_GEOFENCE_TRANSITION"
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(requireContext(), 0, intent, flags)
    }




    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())




    }




    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_, container, false)

        // Initialize views


        // Initialize the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())


        startbtn = view.findViewById(R.id.startbtn)




        // Set up button click listener
        startbtn.setOnClickListener {


            btnclicked= !btnclicked!!

            // Check location permission

            if(btnclicked== true){
                startGeofencing()}
            else{
                stopGeofencing()
            }

        }


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


        manual = view.findViewById(R.id.manual_but)
        mapFrag = view.findViewById(R.id.map)
        manuallayout = view.findViewById(R.id.manual_layout)
        searchlayout = view.findViewById(R.id.searchContainer)

        var isManual : Boolean = true

        searchlayout.visibility = View.GONE

        //manual button
        manual.setOnClickListener(){
            if (isManual) {
//                val params = manuallayout.layoutParams
//
//                if (layoutHeight != null) {
//                    params.height = (layoutHeight/1.2).toInt()
//                }   // Set the height
//                manuallayout.layoutParams = params
//                isManual = false

                manuallayout.visibility = View.GONE
                searchlayout.visibility = View.VISIBLE

            }else{
//                val params = manuallayout.layoutParams
//                params.height = 1// Set the height
//                manuallayout.layoutParams = params
//                isManual = true
                manuallayout.visibility = View.VISIBLE
                searchlayout.visibility = View.GONE
            }
            isManual = !isManual
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





    private fun createNotificationChannel() {
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
       enableMyLocation()
       // Live location
       fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
       locationCallback = object : LocationCallback() {
           override fun onLocationResult(locationResult: LocationResult) {
               for (location in locationResult.locations) {
                   val cur = LatLng(location.latitude, location.longitude)

                   mMap.animateCamera(CameraUpdateFactory.newLatLng(cur))
               }
           }
       }
       startLocationUpdates()

        if (btnclicked==null){
            startGeofencing()
            btnclicked=true
        }

    }




    private fun startGeofencing() {

        //Geofencing


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



        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            return
        }
        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences added
                val fence = LatLng(entry_latitude, entry_longitude)
                mMap.addMarker(MarkerOptions().position(fence).title("Marker in center"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(fence))

                geofenceCircle?.remove()

                 geofenceCircle = mMap.addCircle(
                    CircleOptions()
                        .center(LatLng(entry_latitude, entry_longitude))
                        .radius(geoRad.toDouble())
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(al, 0, 125, 0))
                )

            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofencelist)
        }.build()
    }

    private fun stopGeofencing() {
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(context, "Geofences removed", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                // ...
            }
        }
    }

    private fun startLocationUpdates() {
        // ... request location permissions ...
        val locationRequest = LocationRequest.create().apply {
            interval = 100// Update interval in milliseconds
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    private fun enableMyLocation() {
        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            // Enable the blue dot on the map if permissions are granted
            mMap.isMyLocationEnabled = true
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }
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