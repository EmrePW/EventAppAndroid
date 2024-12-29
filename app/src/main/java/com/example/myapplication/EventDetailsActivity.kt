package com.example.myapplication

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.databinding.ActivityEventDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId


class EventDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
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

        val thisEvent: Event = json.decodeFromString<Event>(eventJson)
        // TODO("dynamiccaly get image")
        binding.eventTitleView.setText(thisEvent.name)
        binding.eventLocationView.setText(thisEvent._embedded.venues[0].address.line1)
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

        // TODO : implement favourites logic, if already liked show the filled version
        binding.iconButton.setOnClickListener{

        }


        // TODO : setReminder
        binding.button5.setOnClickListener {

        }

        // TODO : Join event
        binding.button6.setOnClickListener{

        }

    }

    override fun onStart() {
        super.onStart()
    }
}
