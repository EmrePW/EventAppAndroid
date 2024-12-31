package com.example.myapplication

import kotlinx.serialization.Serializable

data class User(
    val favouriteEvents: MutableList<String>,
    val ratedEvent: MutableList<ratedEvent>,
    val joinedEvents: MutableList<String>,
    val eventPreferences: String,
    val notificationPreference: Boolean,
    val notifyOnUpcomingEvents: Boolean,
    val notifyOnNewEvent: Boolean


){
    constructor() : this(mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        "",
                        true,
                        true,
                        true)
}

@Serializable
data class ratedEvent(
    val eventid: String,
    val body: String?,
    val rating: Int = 0
)