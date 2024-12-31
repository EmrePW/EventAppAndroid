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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var events: MutableList<Event>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val json = Json{
            ignoreUnknownKeys = true
        }
        events = json.decodeFromString(intent.getStringExtra("events") ?: "[]")
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
        mMap.setMapColorScheme(MapColorScheme.FOLLOW_SYSTEM)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this@MapsActivity))
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        for (event in events) {
            val marker: Marker? = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(event._embedded.venues[0].location.latitude.toDouble(), event._embedded.venues[0].location.longitude.toDouble()))
                    .title(event.name)
            )

            val startZonedDateTime = ZonedDateTime.parse(event.sales.public.startDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .withZoneSameInstant(ZoneId.systemDefault())
            val endZonedDateTime = ZonedDateTime.parse(event.sales.public.endDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .withZoneSameInstant(ZoneId.systemDefault())

            marker?.tag = EventInfo("${event.classifications[0].segment.name} ${event.classifications[0].genre.name}",
                "${startZonedDateTime.year} ${startZonedDateTime.month.name.lowercase().capitalize()} ${startZonedDateTime.dayOfMonth} through ${endZonedDateTime.year} ${endZonedDateTime.month.name.lowercase().capitalize()} ${endZonedDateTime.dayOfMonth}",
                "${event.dates.start.localDate} ${event.dates.start.localTime}")
        }

        mMap.setOnMarkerClickListener { marker ->
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(marker.position.latitude, marker.position.longitude), 10f), 2000, null)
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