package com.example.fitnessmoblie.retrofit

import java.io.Serializable

data class WeatherResponse(
    val daily: List<Daily>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int
) : Serializable {
    data class Daily(
        val clouds: Double,
        val dew_point: Double,
        val dt: Double,
        val feels_like: FeelsLike,
        val humidity: Double,
        val moon_phase: Double,
        val moonrise: Double,
        val moonset: Double,
        val pop: Double,
        val pressure: Double,
        val rain: Double,
        val sunrise: Double,
        val sunset: Double,
        val temp: Temp,
        val uvi: Double,
        val weather: List<Weather>,
        val wind_deg: Double,
        val wind_gust: Double,
        val wind_speed: Double
    ) : Serializable {
        data class FeelsLike(
            val day: Double,
            val eve: Double,
            val morn: Double,
            val night: Double
        ) : Serializable
        data class Temp(
            val day: Double,
            val eve: Double,
            val max: Double,
            val min: Double,
            val morn: Double,
            val night: Double
        ) : Serializable

        data class Weather(
            val description: String,
            val icon: String,
            val id: Int,
            val main: String
        ) : Serializable
    }
}