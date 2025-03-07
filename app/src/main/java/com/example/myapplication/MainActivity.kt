package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

// TODO : fix times on cards on the recyclerview

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ticketMasterAPIKey: String = BuildConfig.TICKETMASTER_API_KEY
    private val baseTicketMasterUrl: String = "https://app.ticketmaster.com/discovery/v2/events?apikey=${ticketMasterAPIKey}"
    private lateinit var mainEventsObject: MutableList<Event>
    private lateinit var recyclerView: RecyclerView
    private var adapter: EventMain_RecyclerViewAdapter = EventMain_RecyclerViewAdapter(this@MainActivity, mutableListOf()){pos -> pos}
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("loginTest", "mainActivity onCreate")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        recyclerView = binding.myRecycler
        mainEventsObject = mutableListOf()
        auth = Firebase.auth
        db = Firebase.firestore
        createNotificationChannel()

        setContentView(binding.root)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val coords: MutableList<Double> = getLocation(fusedLocationClient)
        Log.d("coords", "${coords[0]}, ${coords[1]}")

        val geohash = encodeGeohash(coords[0], coords[1], 8)

        fetchItems("$baseTicketMasterUrl&countryCode=TR&size=200&sort=distance,asc&geoPoint=$geohash")

        binding.searchEditText.addTextChangedListener{ query ->
            val myPredicate: (Event) -> Boolean = { obj -> query.toString().lowercase() in obj.name.lowercase() }
            val result: List<Event> = mainEventsObject.filter(myPredicate).toMutableList()
            adapter.updateData(result)
        }

        binding.filterButton.setOnClickListener{
            val popup = PopupMenu(this, binding.filterButton )
            popup.menuInflater.inflate(R.menu.filters_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId) {
                    // Music
                    R.id.option_1 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "music" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Sports
                    R.id.option_2 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "sports" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Arts & Theatre
                    R.id.option_3 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "arts & theatre" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Film
                    R.id.option_4 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "film" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Non-ticket
                    R.id.option_5 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "nonticket" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Miscellaneous
                    R.id.option_6 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "miscellaneous" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    else -> {
                        false
                    }
                }

            }
            // Show the popup menu.
            popup.show()
        }
        binding.removeFilterButton.setOnClickListener {
            adapter.updateData(mainEventsObject)
            binding.removeFilterButton.visibility = View.INVISIBLE
        }

        binding.extendedFab.setOnClickListener{
            if(mainEventsObject.size == 0) {
                return@setOnClickListener
            }
            val intent = Intent(this, MapsActivity::class.java)
            var eventString = "["

            for (event in mainEventsObject) {
                val indiviualEventString: String = Json.encodeToString(Event.serializer(), event)
                eventString += indiviualEventString + ","
            }
            eventString = eventString.reversed().drop(1).reversed()
            eventString += "]"

            val file = File(this.cacheDir ,"maps_events_temp.txt")
            file.writeText(eventString)
            intent.putExtra("events", file.toURI().toString())
            startActivity(intent)
        }
    }
    override fun onStart() {
        Log.i("loginTest", "mainActivity onStart")
        super.onStart()
        if (auth.currentUser == null) {
            Log.i("loginTest", "user is null")
            binding.loginProfileButton.setImageResource(R.drawable.updatedlogin)
            binding.loginProfileButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
        } else {
            val user = auth.currentUser!!
            val userData: User = User()
            checkUser(user.uid)
            binding.loginProfileButton.setImageResource(R.drawable.profile_photo)
            binding.loginProfileButton.setOnClickListener {
                Log.i("loginTest", "click!")
                val popup = PopupMenu(this, binding.loginProfileButton)
                popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.settings -> {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            true
                        }

                        R.id.logout -> {
                            Firebase.auth.signOut()
                            startActivity(Intent(this@MainActivity, MainActivity::class.java))
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
                popup.show()
            }
        }
    }

    private fun checkAndSendNotification(res: MutableList<Event>) {
        if(auth.currentUser == null) {
            Log.i("MainActivitySendNotification", "There is no user")
            return
        }
        var userData: User = User()
        val userDocRef = db.collection("users").document(auth.currentUser!!.uid)
        userDocRef.get()
            .addOnSuccessListener { document ->
                userData = document.toObject<User>()!!
                Log.d("notification", "got userData successfully")

                for (event in res) {
                    var position = 0
                    val eventDocRef = db.collection("events").document(event.id)
                    eventDocRef.get()
                        .addOnSuccessListener { eventDocument ->
                            if(!eventDocument.exists()){
                                val eventData = hashMapOf(
                                    "comments" to emptyList<EventRating>()
                                )
                                eventDocRef.set(eventData)
                                    .addOnSuccessListener {
                                        Log.i("Firestore", "Event saved successfully")
                                    }
                                    .addOnFailureListener{ e->
                                        Log.e("Firestore", "There was a problem when creating an event: ${e.message}")
                                    }

                                // send notification

                                val intent = Intent(this@MainActivity, EventDetailsActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    putExtra("event", Json.encodeToString(Event.serializer(), res.get(position)))
                                }
                                val pendingIntent: PendingIntent = PendingIntent.getActivity(this@MainActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                                val builder = NotificationCompat.Builder(this@MainActivity, getString(R.string.channelId))
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle("My Application")
                                    .setContentText("There is a new event tap this to see details")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent)

                                val segment_code = resolveEventSegment(event.classifications[0].segment.name)
                                Log.i("notification", "$segment_code")

                                if (userData.eventPreferences.contains(segment_code) && userData.notificationPreference && userData.notifyOnNewEvent)
                                {
                                    Log.i("notification", "ENTERED THE IF")
                                    with(NotificationManagerCompat.from(this)) {
                                        if (ActivityCompat.checkSelfPermission(
                                                this@MainActivity,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            return@with
                                        }
                                        // notificationId is a unique int for each notification that you must define.
                                        notify(123, builder.build())
                                    }
                                }
                            }
                        }
                    position += 1
                }
            }
            .addOnFailureListener{
                Log.e("notification","There was an error at retrieving user data")
            }
    }

    private fun resolveEventSegment(segmentName: String): String {
        return when(segmentName.lowercase()) {
            "sports" -> "A"
            "films" -> "B"
            "arts & theatre" -> "C"
            "music" -> "D"
            "nonticket" -> "E"
            "miscellaneous" -> "F"
            else -> "N"
        }

    }

    private fun checkUser(uid: String) {
        val userDocRef = db.collection("users").document(uid)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if(!document.exists()){
                    val userData = hashMapOf(
                        "favouriteEvents" to emptyList<String>(),
                        "ratedEvents" to emptyList<ratedEvent>(),
                        "joinedEvents" to emptyList<String>(),
                        "eventPreferences" to "NNNNNN",
                        "notificationPreference" to true,
                        "notifyOnUpcomingEvent" to true,
                        "notifyOnNewEvent" to true
                    )

                    userDocRef.set(userData)
                        .addOnSuccessListener {
                            Log.i("Firestore", "User saved successful")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Kullanıcı verisi kaydedilirken hata oluştu: ${e.message}")
                            Firebase.auth.signOut()
                        }
                }
                else{
                    Log.i("Firestore", "User data is already exists")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Firestore'dan veri alınırken hata oluştu: ${e.message}")
            }
    }

    private fun fetchItems(url: String){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        Log.i("APITEST", "Starting request...")
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // Handle the error
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("APITEST", "Response received!")
                response.use {
                    if (response.isSuccessful) {
                        val json = Json{
                            ignoreUnknownKeys = true
                        }
                        val res: List<Event> = json.decodeFromString<EventMain>(response.body!!.string())._embedded.events

                        runOnUiThread {
                            adapter = EventMain_RecyclerViewAdapter(this@MainActivity, res.toMutableList()){
                                position ->
                                val intent = Intent(this@MainActivity, EventDetailsActivity::class.java)
                                intent.putExtra("event", json.encodeToString(Event.serializer(), res.get(position)))
                                startActivity(intent)
                            }
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                            mainEventsObject.addAll(res)
                        }
                        Log.d("notification", "in OnResponse")
                        checkAndSendNotification(mainEventsObject)
                    }
                }
            }
        })
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(getString(R.string.channelId), name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getLocation(locationClient: FusedLocationProviderClient): MutableList<Double> {
        var latitude: Double = 39.9055
        var longitude: Double = 41.2658
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                12
            )
        }
        else {
            locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    return@addOnSuccessListener
                }
                else {
                    Log.e("Location", "location is null")
                }
            }
        }
    return mutableListOf(latitude, longitude)
    }

    fun encodeGeohash(latitude: Double, longitude: Double, precision: Int): String {
        val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"
        val latRange = doubleArrayOf(-90.0, 90.0)
        val lonRange = doubleArrayOf(-180.0, 180.0)
        var hash = ""
        var isEven = true
        var bit = 0
        var ch = 0

        while (hash.length < precision) {
            val mid: Double
            if (isEven) {
                mid = (lonRange[0] + lonRange[1]) / 2
                if (longitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    lonRange[0] = mid
                } else {
                    lonRange[1] = mid
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2
                if (latitude > mid) {
                    ch = ch or (1 shl (4 - bit))
                    latRange[0] = mid
                } else {
                    latRange[1] = mid
                }
            }
            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                hash += base32[ch]
                bit = 0
                ch = 0
            }
        }
        return hash
    }
}