package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.util.Log
import com.google.android.libraries.navigation.ListenableResultFuture
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationApi.NavigatorListener
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.RoutingOptions
import com.google.android.libraries.navigation.SimulationOptions
import com.google.android.libraries.navigation.SupportNavigationFragment
import com.google.android.libraries.navigation.Waypoint
import kotlinx.serialization.json.Json

class NavigationActivity : AppCompatActivity() {
    private var mLocationPermissionGranted = false
    private lateinit var point: waypoint
    private lateinit var myNavigator: Navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkLocationPerms()
        setContentView(R.layout.activity_navigation)

        point = Json.decodeFromString<waypoint>(intent.getStringExtra("coords")
            ?: Json.encodeToString(waypoint.serializer(),waypoint(39.9055, 41.2658)))

         NavigationApi.getNavigator(this, object: NavigatorListener {
             override fun onNavigatorReady(navigator: Navigator?) {
                 Log.i("Navigator", "Navigator ready.")
                 if (navigator != null) {
                     myNavigator = navigator
                 }
                 val mNavFragment: SupportNavigationFragment? = supportFragmentManager.findFragmentById(R.id.navigation_fragment) as? SupportNavigationFragment

                 if (mNavFragment == null) {
                     Log.e("Navigator", "Navigation fragment not found with id R.id.navigation_fragment")
                 }

                 myNavigator.setTaskRemovedBehavior(Navigator.TaskRemovedBehavior.QUIT_SERVICE)

                 val routingOptions = RoutingOptions()
                 routingOptions.travelMode(RoutingOptions.TravelMode.DRIVING)

                 navigateToPlace(point, routingOptions, myNavigator)
             }

             override fun onError(e_code: Int) {
                 Log.e("Navigator", "Something happened $e_code")
             }

         })


    }
    private fun checkLocationPerms(){
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                301
            )
        }
        if (!mLocationPermissionGranted) {
            Log.e("Navigation", "Error loading Navigation SDK: The user has not granted location permission.")
            return
        }
    }

    private fun navigateToPlace(waypoint: waypoint, travelMode: RoutingOptions, navigator: Navigator?) {
        var destination: Waypoint? = null

        try {
            destination = Waypoint.builder().setLatLng(waypoint.lat, waypoint.long).build()
        } catch (e: Waypoint.UnsupportedPlaceIdException) {
            Log.e("Navigator", "Error starting navigation: Place ID is not supported.")
            return
        }

        val pendingRoute: ListenableResultFuture<Navigator.RouteStatus> =
            navigator!!.setDestination(destination, travelMode)

        pendingRoute.setOnResultListener { code ->
            when (code) {
                Navigator.RouteStatus.OK -> {
                    // Hide the toolbar to maximize the navigation UI.
                    supportActionBar?.hide()


                    // Enable voice audio guidance (through the device speaker).
                    navigator.setAudioGuidance(Navigator.AudioGuidance.VOICE_ALERTS_AND_GUIDANCE)

                    // Simulate vehicle progress along the route for demo/debug builds.
                    if (BuildConfig.DEBUG) {
                        Log.d("Navigator", "DEBUG MODE ACTIVE")
                        navigator.simulator.simulateLocationsAlongExistingRoute(
                            SimulationOptions().speedMultiplier(10f)
                        )
                    }

                    // Start turn-by-turn guidance along the current route.
                    navigator.startGuidance()
                }

                Navigator.RouteStatus.NO_ROUTE_FOUND -> Log.e(
                    "Navigator",
                    "Error starting navigation: No route found."
                )

                Navigator.RouteStatus.NETWORK_ERROR -> Log.e(
                    "Navigator",
                    "Error starting navigation: Network error."
                )

                Navigator.RouteStatus.ROUTE_CANCELED -> Log.e(
                    "Navigator",
                    "Error starting navigation: Route canceled."
                )

                else -> Log.e("Navigator", "Error starting navigation: $code")
            }
        }
    }

    override fun onBackPressed() {
        if (myNavigator.isGuidanceRunning) {
            myNavigator.stopGuidance()
            myNavigator.clearDestinations()
            myNavigator.cleanup()

        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myNavigator.cleanup()
    }
}
