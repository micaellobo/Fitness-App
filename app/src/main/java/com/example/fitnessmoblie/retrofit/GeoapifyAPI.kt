package com.example.fitnessmoblie.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface GeoapifyAPI {

    @GET("v2/places?categories=sport.fitness&apiKey=08062cfd4efe44a393a29950ced03201")
    fun getPlaces(@Query("filter") filter: String, @Query("bias") bias: String): Call<GeoapifyFitnessPlaceResponse>

    @GET("v1/geocode/reverse?format=json&apiKey=08062cfd4efe44a393a29950ced03201")
    fun getGeocode(@Query("lat") lat: String, @Query("lon") lon: String): Call<GeoapifyGeocodeResponse>

    companion object {
        const val BASE_URL = "https://api.geoapify.com/"
        fun retrofitInstance(): Retrofit {
            val client = OkHttpClient.Builder().addInterceptor {
                val newRequest: Request = it.request().newBuilder()
                    .build()
                it.proceed(newRequest)
            }.build()
            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        fun getApi(): GeoapifyAPI {
            return this.retrofitInstance().create(GeoapifyAPI::class.java)
        }
    }
}