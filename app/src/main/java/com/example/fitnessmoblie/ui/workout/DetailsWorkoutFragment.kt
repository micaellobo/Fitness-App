package com.example.fitnessmoblie.ui.workout

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.database.FitnessDb
import com.example.fitnessmoblie.databinding.FragmentDetailsWorkoutBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.ui.map.TAG
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds


private const val WORKOUT = "WORKOUT"

class DetailsWorkoutFragment : Fragment() {

    private val binding by lazy { FragmentDetailsWorkoutBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseFireStore by lazy { Firebase.firestore }

    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    private var isEditMode = false

    private lateinit var workout: Workout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val obj = it.getSerializable(WORKOUT)
            when (obj) {
                is Workout -> workout = obj
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setValues()

        when {
            workout.exerciseType == ExerciseType.RUNNING_AUTOMATIC.toString() -> {
                binding.cardReps.visibility = View.GONE
            }
            workout.exerciseType == ExerciseType.RUNNING_SEMI_MANUAL.toString() -> {
                binding.cardReps.visibility = View.GONE
                binding.cardSteps.visibility = View.GONE
            }
        }
        binding.btnEditSaveChanges.setOnClickListener { setupButtonEditSaveChanges() }
        binding.btnDeleteCancel.setOnClickListener { setupButtonDeleteCancel() }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupButtonDeleteCancel() {
        when {
            isEditMode -> {
                cancelEdit()
            }
            else -> delete()
        }
    }

    private fun setupButtonEditSaveChanges() {
        when {
            isEditMode -> {
                saveChanges()
                goViewMode()
            }
            else -> {
                goEditMode()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setValues() {
        binding.tvDate.text = LocalDateTime.parse(workout.dateStart).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

        binding.tvNomeTreino.text = workout.exerciseType.replace("_".toRegex(), " ")

        binding.etCalories.setText(workout.calories.toString())
        workout.distance?.let { binding.etDistance.setText(it.toString()) }
        workout.steps?.let { binding.etSteps.setText(it.toString()) }
        workout.amount?.let { binding.etReps.setText(it.toString()) }

        val duration = ChronoUnit.SECONDS.between(LocalDateTime.parse(workout.dateStart), LocalDateTime.parse(workout.dateEnd))
        if (duration < 60) binding.etTime.setText(duration.seconds.toString().replace(" ".toRegex(), ":"))
        else binding.etTime.setText(duration.seconds.toString().replace(" ".toRegex(), ":").dropLast(4))

    }

    fun saveChanges() {
        isEditMode = false
        val oldWorkout = workout.copy()
        workout.calories = binding.etCalories.text.toString().toInt()
        workout.distance = binding.etDistance.text.toString().toInt()
        workout.amount = binding.etReps.text.toString().toInt()
        workout.steps = binding.etSteps.text.toString().toInt()

        workoutViewModel.update(workout)
        updateScoreFirestore(oldWorkout, workout)
    }

    fun updateScoreFirestore(oldValuesWorkout: Workout, newValuesWorkout: Workout) {
        FitnessDb.executors.execute {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val usersRef = firebaseFireStore.collection("users").document(uid)
                val newValueDistance = (newValuesWorkout.distance ?: 0).minus(oldValuesWorkout.distance ?: 0)
                val newValueCalories = newValuesWorkout.calories.minus(oldValuesWorkout.calories)

                usersRef
                    .update(
                        "totalDistance", FieldValue.increment(newValueDistance.toLong()),
                        "totalBurnedCalories", FieldValue.increment(newValueCalories.toLong())
                    )
                    .addOnSuccessListener { Log.i("DETAILS_WORKOUT", "Doc updates") }
                    .addOnFailureListener { e -> Log.i("DETAILS_WORKOUT", "Error updating document", e) }
            } else {
                Log.i("DETAILS_WORKOUT", "Error updating document, no Login")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cancelEdit() {
        goViewMode()
        setValues()
    }

    fun delete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Apagar Treino")
            .setMessage("Tem a certeza?")
            .setPositiveButton("Sim") { dialog, id ->
                workoutViewModel.delete(workout.id)
                deleteWorkoutScore(workout)
                requireActivity().onBackPressed()
            }
            .setNegativeButton("Não") { dialog, id ->
            }
            .show()
    }

    fun goEditMode() {
        isEditMode = true

        binding.btnEditSaveChanges.text = "Guardar"
        binding.btnDeleteCancel.text = "Cancelar"

        binding.etSteps.isEnabled = true
        binding.etDistance.isEnabled = true
        binding.etCalories.isEnabled = true
        binding.etReps.isEnabled = true
    }

    fun goViewMode() {
        isEditMode = false

        binding.btnEditSaveChanges.text = "Editar"
        binding.btnDeleteCancel.text = "Eliminar"

        binding.etSteps.isEnabled = false
        binding.etDistance.isEnabled = false
        binding.etCalories.isEnabled = false
        binding.etReps.isEnabled = false
    }

    private fun deleteWorkoutScore(workout: Workout) {
        FitnessDb.executors.execute {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val userRef = firebaseFireStore.collection("users").document(uid)

                userRef.update("totalDistance", FieldValue.increment(-(workout.distance ?: 0).toLong()))
                userRef.update("totalBurnedCalories", FieldValue.increment(-workout.calories.toLong()))
            } else {
                Log.i("FIREBASE__", "Error updating document, no Login")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(workout: Workout) =
            DetailsWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(WORKOUT, workout)
                }
            }
    }
}