package com.example.myapplication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventMain (
    val _embedded: MainEmbedded,
    val _links: AttractionLinks,
    val page: Page
)

@Serializable
data class MainEmbedded (
    val events: List<Event>
)

@Serializable
data class Event (
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>,
    val sales: Sales,
    val dates: Dates,
    val classifications: List<Classification>,
    val _links: EventLinks,
    val _embedded: EventEmbedded
)

@Serializable
data class Classification (
    val primary: Boolean,
    val segment: Genre,
    val genre: Genre = Genre("" , ""),
    val family: Boolean
)

@Serializable
data class Genre (
    val id: String,
    val name: String
)

@Serializable
data class Dates (
    val start: Start,
    val timezone: String,
    val status: Status,
    val spanMultipleDays: Boolean
)

@Serializable
data class Start (
    val localDate: String,
    val localTime: String,
    val dateTime: String,
    val dateTBD: Boolean,
    val dateTBA: Boolean,
    val timeTBA: Boolean,
    val noSpecificTime: Boolean
)

@Serializable
data class Status (
    val code: String
)

@Serializable
data class EventEmbedded (
    val venues: List<Venue>,
    val attractions: List<Attraction>
)

@Serializable
data class Attraction (
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val locale: String,
    val images: List<Image>,
    val classifications: List<Classification>,
    val upcomingEvents: UpcomingEvents,
    val _links: AttractionLinks
)

@Serializable
data class Image (
    val ratio: String = "16_9",
    val url: String,
    val width: Long,
    val height: Long,
    val fallback: Boolean
)

@Serializable
data class AttractionLinks (
    val self: Self
)

@Serializable
data class Self (
    val href: String
)

@Serializable
data class UpcomingEvents (
    @SerialName("wts-tr") val wts_tr: Long = 0,
    val _total: Long,
    val _filtered: Long
)

@Serializable
data class Venue (
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val timezone: String,
    val city: City,
    val country: Country,
    val address: Address = Address("No address has been provided..."),
    val location: Location,
    val markets: List<Genre>,
    val dmas: List<DMA>,
    val upcomingEvents: UpcomingEvents,
    val _links: AttractionLinks
)

@Serializable
data class Address (
    val line1: String = ""
)

@Serializable
data class City (
    val name: String
)

@Serializable
data class Country (
    val name: String,
    val countryCode: String
)

@Serializable
data class DMA (
    val id: Long
)

@Serializable
data class Location (
    val longitude: String,
    val latitude: String
)

@Serializable
data class EventLinks (
    val self: Self,
    val attractions: List<Self>,
    val venues: List<Self>
)

@Serializable
data class Sales (
    val public: Public
)

@Serializable
data class Public (
    val startDateTime: String,
    val startTBD: Boolean,
    val startTBA: Boolean,
    val endDateTime: String
)

@Serializable
data class Page (
    val size: Long,
    val totalElements: Long,
    val totalPages: Long,
    val number: Long
)

@Serializable
data class waypoint(val lat: Double,
                    val long: Double)

@Serializable
data class EventRating(val uid:String, val rating: Int, val body: String){
    constructor() : this("N/A", 2, "lorem ipsum and crazzy shit about how about this time we add things up")
}

@Serializable
data class EventRatingList(val ratingList: MutableList<EventRating>) {
    constructor() : this(mutableListOf(EventRating()))
}