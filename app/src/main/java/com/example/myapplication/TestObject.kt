package com.example.myapplication

import kotlinx.serialization.Serializable

@Serializable
data class TestObject (
    val userId: Long,
    val id: Long,
    val title: String,
    val body: String
)