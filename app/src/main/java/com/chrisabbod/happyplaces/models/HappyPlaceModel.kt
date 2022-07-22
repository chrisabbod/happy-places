package com.chrisabbod.happyplaces.models

import java.io.Serializable

data class HappyPlaceModel(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
): Serializable //Allows us to pass this class as an intent
//Serializable will put this into a format we can pass from one class to another
//Also used to save an object if you want to store an object of a class