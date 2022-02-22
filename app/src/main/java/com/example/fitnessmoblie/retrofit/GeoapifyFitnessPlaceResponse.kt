package com.example.fitnessmoblie.retrofit

import java.io.Serializable

data class GeoapifyFitnessPlaceResponse(
    val features: List<Feature>,
    val type: String
) : Serializable

data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
) : Serializable

data class Geometry(
    val coordinates: List<Double>,
    val type: String
) : Serializable

data class Properties(
    val address_line1: String = "",
    val address_line2: String = "",
    val categories: List<String> = ArrayList(),
    val city: String,
    val country: String = "",
    val county: String = "",
    val details: List<String> = ArrayList(),
    val distance: Int,
    val district: String = "",
    val formatted: String = "",
    val lat: Double,
    val lon: Double,
    val municipality: String = "",
    val name: String = "",
    val postcode: String = "",
    val street: String = "",
    val suburb: String? = null,
    val town: String = "",
) : Serializable

data class Datasource(
    val attribution: String,
    val license: String,
    val sourcename: String,
    val url: String
) : Serializable