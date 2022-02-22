package com.example.fitnessmoblie.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private var currentUserEmail: String = ""
    
    //Workout
    private var workoutStarted = false
    private lateinit var currentExerciseType: ExerciseType

    private val repositoryFitness by lazy { FitnessRepository(application) }
    private val workouts: LiveData<List<Workout>> by lazy { repositoryFitness.getAll(currentUserEmail) }

    fun insert(workout: Workout) {
        FitnessDb.executors.execute { repositoryFitness.insert(workout) }
    }

    fun delete(id: Int) {
        FitnessDb.executors.execute { repositoryFitness.delete(id) }
    }

    fun update(workout: Workout) {
        FitnessDb.executors.execute { repositoryFitness.update(workout) }

    }

    fun deleteAll() {
        FitnessDb.executors.execute {
            repositoryFitness.deleteAll(currentUserEmail)
        }
    }

    fun count(): Int {
        val value = repositoryFitness.getAll(currentUserEmail).value
        return value?.size ?: 0
    }

    fun getAll(): LiveData<List<Workout>> {
        return workouts
    }


    fun setCurrentWorkout(exerciseType: ExerciseType) {
        currentExerciseType = exerciseType
    }

    fun getCurrentWorkout() = currentExerciseType

    fun setCurrentUserEmail(email: String) {
        currentUserEmail = email
    }

    fun getCurrentUserEmail() = currentUserEmail

    fun hasWorkoutStarted() = workoutStarted

    fun setStartedWorkout(value: Boolean) {
        workoutStarted = value
    }
}

