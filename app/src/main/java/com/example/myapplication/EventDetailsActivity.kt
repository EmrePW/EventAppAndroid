package com.example.myapplication

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Rating
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityEventDetailsBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.serialization.json.Json
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var thisEvent: Event
    private lateinit var eventCL: MutableList<EventRating>
    private lateinit var adapter: Comment_RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        db = Firebase.firestore
        auth = Firebase.auth

        setContentView(binding.root)

        val eventJson = intent.getStringExtra("event")!!
        val json = Json {
            ignoreUnknownKeys= true
        }

        thisEvent = json.decodeFromString<Event>(eventJson)
        binding.eventTitleView.setText(thisEvent.name)
        binding.eventLocationView.setText(if (thisEvent._embedded.venues[0].address.line1.isBlank()) "No address has been provided" else thisEvent._embedded.venues[0].address.line1)
        binding.eventSegmentView.setText(thisEvent.classifications[0].segment.name)
        binding.eventGenreView.setText(thisEvent.classifications[0].genre.name)
        binding.eventUrl.setText(thisEvent.url)
        val startZonedDateTime = ZonedDateTime.parse(thisEvent.sales.public.startDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                            .withZoneSameInstant(ZoneId.systemDefault())
        val endZonedDateTime = ZonedDateTime.parse(thisEvent.sales.public.endDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        binding.eventSalesStart.setText("${startZonedDateTime.dayOfMonth}-${startZonedDateTime.month.value}-${startZonedDateTime.year}")
        binding.eventSalesEnd.setText("${endZonedDateTime.dayOfMonth}-${endZonedDateTime.month.value}-${endZonedDateTime.year}")

        binding.eventStartDate.setText(thisEvent.dates.start.localDate)
        binding.eventStartTime.setText(thisEvent.dates.start.localTime)

        binding.venueName.setText(thisEvent._embedded.venues[0].name)
        binding.venueUrl.setText(thisEvent._embedded.venues[0].url)
        binding.venueCity.setText(thisEvent._embedded.venues[0].city.name)
        binding.venueCountry.setText(thisEvent._embedded.venues[0].country.name)
        binding.venueAdress.setText(thisEvent._embedded.venues[0].address.line1)


        binding.extendedFab.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(this, NavigationActivity::class.java)
                intent.putExtra(
                    "coords",
                    Json.encodeToString(
                        waypoint.serializer(),
                        waypoint(
                            thisEvent._embedded.venues[0].location.latitude.toDouble(),
                            thisEvent._embedded.venues[0].location.longitude.toDouble()
                        )
                    )
                )
                startActivity(intent)
            }
            else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        binding.submitRatingButton.setOnClickListener {
            if(auth.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }
            var rating = binding.ratingBarComment.rating.toInt()
            var comment = binding.commentField.text.toString()

            if (rating == 0) rating = 1

            eventCL.add(EventRating(auth.currentUser!!.uid, rating, comment))
            adapter.updateData(eventCL.toList())
            db.collection("events").document(thisEvent.id).update("comments", eventCL)
        }
    }

    private fun checkEvent() {
        val eventDocRef = db.collection("events").document(thisEvent.id)

        eventDocRef.get().addOnSuccessListener { document ->
            if(!document.exists()) {
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

            }
            else {
                // display comments
                val eventRC: EventCommentsList = document.toObject<EventCommentsList>()!!
                adapter = Comment_RecyclerViewAdapter(this, eventRC.comments)
                recyclerView = binding.commentsRecyclerView

                eventCL = eventRC.comments

                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
        }
        .addOnFailureListener{ e->
            Log.e("Firestore", "There has been a problem when retrieving from Firebase: ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        var userData: User = User()
        val user = auth.currentUser
        if(user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if(document != null) {
                        userData = document.toObject<User>()!!
                    }
                    else {
                        Log.e("FireStore", "no Such document for ${user.uid}")
                    }
                    if(userData.favouriteEvents.contains(thisEvent.id)) {
                        binding.iconButton.setIconResource(R.drawable.baseline_favorite_24)
                    }

                    if(userData.joinedEvents.contains(thisEvent.id)) {
                        Log.d("Details1234", "has event")
                        binding.button6.setText(R.string.leaveEvent)
                    }
                }
                .addOnFailureListener {e ->
                    Log.d("fireStore", "get failed with ", e)
                }
        }
        else {
            return
        }

        checkEvent()

        binding.iconButton.setOnClickListener{
            if(auth.currentUser == null) {
                Toast.makeText(this, "Please log in first!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }
            val favButton : MaterialButton = findViewById(R.id.iconButton)
            if (userData.favouriteEvents.contains(thisEvent.id)){
                userData.favouriteEvents.remove(thisEvent.id)
                favButton.setIconResource(R.drawable.outline_favorite_border_24)
            }
            else {
                userData.favouriteEvents.add(thisEvent.id)
                favButton.setIconResource(R.drawable.baseline_favorite_24)
            }
            // update firestore
            db.collection("users").document(user.uid).update("favouriteEvents", userData.favouriteEvents)
        }

        binding.button5.setOnClickListener {
            createReminderOnCalendar()
        }

        binding.button6.setOnClickListener{
            if(auth.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            val joinEventButton: MaterialButton = findViewById<MaterialButton>(R.id.button6)
            if (userData.joinedEvents.contains(thisEvent.id)) {
                userData.joinedEvents.remove(thisEvent.id)
                joinEventButton.setText(getString(R.string.joinEvent))
            }
            else {
                userData.joinedEvents.add(thisEvent.id)
                joinEventButton.setText(getString(R.string.leaveEvent))
            }
            db.collection("users").document(user.uid).update("joinedEvents", userData.joinedEvents)
        }
    }


    private fun createReminderOnCalendar() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CALENDAR),
                103
            )
        }
        else {
            val cr: ContentResolver = this.contentResolver
            val values = ContentValues()

            val startMillis = ZonedDateTime.parse(thisEvent.dates.start.dateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .withZoneSameInstant(ZoneId.systemDefault()).toEpochSecond() * 1000
            val endMillis = startMillis + 1860 * 1000 // 1-hour duration

            values.put(CalendarContract.Events.DTSTART, startMillis)
            values.put(CalendarContract.Events.DTEND, endMillis)
            values.put(CalendarContract.Events.TITLE, thisEvent.name)
            values.put(CalendarContract.Events.DESCRIPTION, "Added directly to calendar")
            values.put(CalendarContract.Events.CALENDAR_ID, 5) // Replace with a valid calendar ID
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)


            // Insert the event into the calendar
            val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, 30)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }

                val reminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                if (reminderUri != null) {
                    Log.d("CalendarInsert", "Reminder added successfully for Event ID: $eventId")
                } else {
                    Log.e("CalendarInsert", "Failed to add reminder for Event ID: $eventId")
                }
            } else {
                Log.e("CalendarInsert", "Failed to insert event")
            }
        }
    }
}