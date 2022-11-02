package com.example.happyplaces.models


data class HappyPlacesModel(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val latitude: Double,
    val longitude: Double
)
