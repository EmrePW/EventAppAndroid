package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.MapColorScheme
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()

        // Add a marker in Sydney and move the camera
        val BURSA = LatLng(40.1885, 29.0610)
        val TRABZON = LatLng(41.0027, 39.7168)
        mMap.setMapColorScheme(MapColorScheme.FOLLOW_SYSTEM)
        // zooms between 0f and 20f
        val bursa: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(BURSA)
                .title("Bursa")
                .snippet("Hello")
        )

//         val trabzon: Marker? = mMap.addMarker(
//            MarkerOptions()
//                .position(TRABZON)
//                .title("Trabzon")
//
//        )

        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this@MapsActivity))
        bursa?.tag = EventInfo("Arts", "20.10.2024", "20:00:00")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BURSA, 8f))
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
            return
        }
        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            201
        )
    }

    class CustomInfoWindowAdapter(private val context: Context) : InfoWindowAdapter {
        override fun getInfoContents(p0: Marker): View? {
            return null
        }

        override fun getInfoWindow(p0: Marker): View? {
            val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
            val title = view.findViewById<TextView>(R.id.eventTitleViewMap)
            val segment = view.findViewById<TextView>(R.id.eventSegmentGenreMap)
            val sales = view.findViewById<TextView>(R.id.eventSalesMap)
            val start = view.findViewById<TextView>(R.id.eventStartMap)

            val placeInfo = p0.tag as EventInfo


            title.text = p0.title ?: "Default"
            segment.text = placeInfo.segmentGenre ?: "Default"
            sales.text = placeInfo.sales ?: "Default"
            start.text = placeInfo.start ?: "Default"

            return view
        }

    }

    data class EventInfo(
        val segmentGenre: String,
        val sales: String,
        val start: String
    )
}