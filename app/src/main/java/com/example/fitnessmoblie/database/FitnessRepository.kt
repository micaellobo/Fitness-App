package com.example.fitnessmoblie.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fitnessmoblie.models.Workout

class FitnessRepository(application: Application) {

    val workoutDao = FitnessDb.getInstance(application).workoutDao()

    fun getAll(userEmail: String): LiveData<List<Workout>> = workoutDao.getAll(userEmail)

    fun get(id: Int): LiveData<Workout> = workoutDao.get(id)

    fun insert(workout: Workout): Long = workoutDao.insert(workout)

    fun deleteAll(userEmail: String) = workoutDao.deleteAll(userEmail)

    fun update(workout: Workout) = workoutDao.update(workout)

    fun delete(id: Int) = workoutDao.delete(id)

}