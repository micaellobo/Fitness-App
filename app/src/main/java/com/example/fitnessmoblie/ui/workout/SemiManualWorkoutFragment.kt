package com.example.fitnessmoblie.ui.workout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.databinding.FragmentSemiManualWorkoutBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.database.FitnessDb
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.services.TimerService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import kotlin.math.roundToInt

class SemiManualWorkoutFragment : Fragment(), InsertInfoWorkoutFragment.NoticeDialogListener {

    private val binding by lazy { FragmentSemiManualWorkoutBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseFireStore by lazy { Firebase.firestore }

    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    //Timer
    private val timerService by lazy { Intent(requireContext(), TimerService::class.java) }
    private var time = 0.0

    //Workout
    private lateinit var dateStart: LocalDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.buttonStartStop.setOnClickListener { startStopTimer() }

        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startStopTimer() {
        if (workoutViewModel.hasWorkoutStarted()) {
            stopTimer()
            val newInstance = InsertInfoWorkoutFragment.newInstance(workoutViewModel.getCurrentWorkout().toString())
            newInstance.setCancelable(false)
            newInstance.show(childFragmentManager, "InsertInfoWorkout")
        } else {
            startTimer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimer() {
        resetTimer()
        timerService.putExtra(TimerService.TIME_EXTRA, time)
        requireActivity().startService(timerService)
        dateStart = LocalDateTime.now()
        workoutViewModel.setStartedWorkout(true)
        workoutViewModel.setCurrentWorkout(ExerciseType.RUNNING_SEMI_MANUAL)
    }

    private fun stopTimer() {
        resetTimer()
        requireActivity().stopService(timerService)
        binding.buttonStartStop.text = "Iniciar"
        workoutViewModel.setStartedWorkout(false)
    }

    private fun resetTimer() {
        time = 0.0
        binding.buttonStartStop.text = "Iniciar"
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.buttonStartStop.text = getTimeStringFromDouble(time)
        }
    }

    fun saveWorkout(workout: Workout) {
        Log.i("WORKOUTS_COMPLETED_LIST", dateStart.toString())

        workout.dateStart = dateStart.toString()

        FitnessDb.executors.execute {
            addNewScoreFirebase(workout)
            workoutViewModel.insert(workout)

        }
        Toast.makeText(requireContext(), "Treino Guardado", Toast.LENGTH_SHORT).show()
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDialogPositiveClick(workout: Workout) {
        saveWorkout(workout)
    }

    override fun onDialogNegativeClick() {
        stopTimer()
    }

    private fun addNewScoreFirebase(workout: Workout) {
        FitnessDb.executors.execute {
            val userRef = firebaseAuth.currentUser?.let { firebaseFireStore.collection("users").document(it.uid) }

            userRef?.update("totalDistance", workout.distance?.let { FieldValue.increment(it.toLong()) })
            userRef?.update("totalBurnedCalories", FieldValue.increment(workout.calories.toLong()))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SemiManualWorkoutFragment().apply {
                arguments = Bundle().apply { }
            }
    }
}