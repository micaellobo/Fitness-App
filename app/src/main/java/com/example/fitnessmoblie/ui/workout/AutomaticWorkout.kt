package com.example.fitnessmoblie.ui.workout

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.databinding.FragmentAutomaticWorkoutBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.database.FitnessDb
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.services.StepsCounterService
import com.example.fitnessmoblie.services.TimerService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import kotlin.math.roundToInt


class AutomaticWorkout : Fragment() {

    private val binding by lazy { FragmentAutomaticWorkoutBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseFireStore by lazy { Firebase.firestore }

    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    //Steps
    private val stepsCounterService by lazy { Intent(requireContext(), StepsCounterService::class.java) }
    private var stepsCounter = 0

    //Timer
    private val timerService by lazy { Intent(requireContext(), TimerService::class.java) }
    private var time = 0.0

    //Workout
    private lateinit var dateStart: LocalDateTime

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.buttonStartStop.setOnClickListener { startStopTimer() }

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 5
        )

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Funcionalidades limitadas, sem permissão de sensor atividade fisica", Toast.LENGTH_LONG)
                .show()
        }

        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
        requireActivity().registerReceiver(updateSteps, IntentFilter(StepsCounterService.STEPS_UPDATED))
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startStopTimer() {
        if (workoutViewModel.hasWorkoutStarted()) {
            stopTimer()
            openConfirmAlertDialog()
        } else {
            startTimer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openConfirmAlertDialog() {
        AlertDialog.Builder(binding.root.context).setTitle("Treino Terminado!")
            .setCancelable(false)
            .setMessage("Deseja guardar o treino?")
            .setPositiveButton("Sim") { dialog, id -> saveWorkout() }
            .setNegativeButton("Não") { dialog, id ->
                resetTimer()
                dialog.cancel()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimer() {
        resetTimer()
        timerService.putExtra(TimerService.TIME_EXTRA, time)
        requireActivity().startService(timerService)
        requireActivity().startService(stepsCounterService)
        workoutViewModel.setStartedWorkout(true)
        workoutViewModel.setCurrentWorkout(ExerciseType.RUNNING_AUTOMATIC)
        dateStart = LocalDateTime.now()
    }

    private fun stopTimer() {
        requireActivity().stopService(timerService)
        requireActivity().stopService(stepsCounterService)
        binding.buttonStartStop.text = "Iniciar"
        workoutViewModel.setStartedWorkout(false)
    }

    private fun resetTimer() {
        time = 0.0
        binding.buttonStartStop.text = "Iniciar"
        binding.tvSteps.text = "0"
        binding.tvCalories.text = "0"
        binding.tvDistance.text = "0"
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.buttonStartStop.text = getTimeStringFromDouble(time)
        }
    }
    private val updateSteps: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stepsCounter = intent.getIntExtra(StepsCounterService.STEPS_EXTRA, stepsCounter)

            binding.tvSteps.text = stepsCounter.toString()
            binding.tvCalories.text = (stepsCounter * 0.045).toInt().toString()
            binding.tvDistance.text = ((stepsCounter * 2.5).toInt() / 3.281).toInt().toString()
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveWorkout() {
        val workout = Workout(
            calories = binding.tvCalories.text.toString().toInt(),
            distance = binding.tvDistance.text.toString().toInt(),
            steps = binding.tvSteps.text.toString().toInt(),
            exerciseType = ExerciseType.RUNNING_AUTOMATIC.toString(),
            dateStart = dateStart.toString(),
            dateEnd = LocalDateTime.now().toString(),
            userEmail = workoutViewModel.getCurrentUserEmail()
        )

        FitnessDb.executors.execute {
            workoutViewModel.insert(workout)
            addNewScoreFirebase(workout)
        }
        Toast.makeText(requireContext(), "Treino Guardado", Toast.LENGTH_SHORT).show()
    }

    private fun addNewScoreFirebase(workout: Workout) {
        FitnessDb.executors.execute {
            val userRef = firebaseAuth.currentUser?.let { firebaseFireStore.collection("users").document(it.uid) }

            userRef?.update("totalDistance", workout.distance?.let { FieldValue.increment(it.toLong()) })
            userRef?.update("totalBurnedCalories", FieldValue.increment(workout.calories.toLong()))
        }
    }


    companion object {
        const val TAG = "AUTOMATIC_WORKOUT"

        @JvmStatic
        fun newInstance() =
            AutomaticWorkout().apply {
                arguments = Bundle().apply {}
            }
    }
}