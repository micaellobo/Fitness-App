package com.example.fitnessmoblie.retrofit

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.geoapify.com/"

interface WeatherAPI {

    @GET("onecall?exclude=current,minutely,hourly,alerts&units=metric&appid=4da547664e101a9f53da4773ab8c3988")
    fun getGeocode(@Query("lat") lat: String, @Query("lon") lon: String): Call<WeatherResponse>

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private fun retrofitInstance(): Retrofit {
            val client = OkHttpClient.Builder().addInterceptor {
                val newRequest = it.request().newBuilder().build()
                it.proceed(newRequest)
            }.build()
            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        fun getApi(): WeatherAPI {
            return this.retrofitInstance().create(WeatherAPI::class.java)
        }
    }
}