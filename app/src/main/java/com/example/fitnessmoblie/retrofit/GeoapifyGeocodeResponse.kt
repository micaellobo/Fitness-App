package com.example.fitnessmoblie.retrofit

data class GeoapifyGeocodeResponse(
    val results: List<Result>
)

data class Result(
    val address_line1: String,
    val address_line2: String,
    val bbox: Bbox,
    val city: String,
    val country: String,
    val country_code: String,
    val county: String,
    val datasource: Datasource,
    val distance: Double,
    val district: String,
    val formatted: String,
    val lat: Double,
    val lon: Double,
    val place_id: String,
    val postcode: String,
    val rank: Rank,
    val result_type: String,
    val road: String
)

data class Bbox(
    val lat1: Double,
    val lat2: Double,
    val lon1: Double,
    val lon2: Double
)

data class Rank(
    val importance: Double,
    val popularity: Double
)