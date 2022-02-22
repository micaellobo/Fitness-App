package com.example.fitnessmoblie.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationUpdatesService : Service() {

    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()

        startLocationListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationListener()
//        val broadcastIntent = Intent()
//        broadcastIntent.action = "restartservice"
//        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
//        this.sendBroadcast(broadcastIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val locationRequest = LocationRequest.create()
            .setInterval(500)
            .setFastestInterval(100)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationListener() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            .addOnSuccessListener {}
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location: Location = locationResult.lastLocation
                latitude = location.latitude
                longitude = location.longitude
                Log.i("Location Service", "location update $location")
            }
        }
    }

}