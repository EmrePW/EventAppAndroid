package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityEventDetailsBinding
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId


class EventDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
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




    }
}