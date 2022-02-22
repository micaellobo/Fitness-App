package com.example.fitnessmoblie.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class StepsCounterService : Service(), SensorEventListener {

    override fun onBind(intent: Intent): IBinder? = null

    private val sensorManager by lazy { this.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private var isFirstRead = true
    private var stepsFirstRead = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val newSensorRead = event.values[0].toInt()
            if (isFirstRead) {
                stepsFirstRead = newSensorRead
                isFirstRead = false
            }
            val currentSteps = newSensorRead - stepsFirstRead

            val intent = Intent(STEPS_UPDATED)
            intent.putExtra(STEPS_EXTRA, currentSteps)
            Log.i("$TAG currentSteps", currentSteps.toString())
            sendBroadcast(intent)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onCreate() {
        super.onCreate()
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    companion object {
        const val TAG = "SERVICE_STEPS"
        const val STEPS_UPDATED = "stepsUpdated"
        const val STEPS_EXTRA = "stepsExtra"
    }

}