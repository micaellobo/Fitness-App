package com.example.fitnessmoblie.ui.workout

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.databinding.FragmentInserInfoWorkoutBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.retrofit.GeoapifyAPI
import com.example.fitnessmoblie.retrofit.GeoapifyGeocodeResponse
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.ui.map.TAG
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

const val EXERCISE_TYPE = "EXERCISE_TYPE"

class InsertInfoWorkoutFragment : DialogFragment() {

    private val binding by lazy { FragmentInserInfoWorkoutBinding.inflate(layoutInflater) }
    private val listener by lazy { parentFragment as NoticeDialogListener }
    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    private lateinit var exerciseType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exerciseType = it.getString(EXERCISE_TYPE).toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        when (this.exerciseType) {
            ExerciseType.RUNNING_SEMI_MANUAL.toString() -> {
                binding.linearReps.visibility = View.GONE
                binding.linerDuration.visibility = View.GONE
                binding.linearSteps.visibility = View.GONE
            }
        }
        return activity?.let {
            AlertDialog.Builder(it).setView(binding.root)
                .setPositiveButton("Guardar") { dialog, id ->
                    val tvCalories = if (binding.tvCalories.text.toString().trim().isEmpty()) 0
                    else binding.tvCalories.text.toString().toInt()

                    val tvDistance = if (binding.tvDistance.text.toString().trim().isEmpty()) 0
                    else binding.tvDistance.text.toString().toInt()

                    val tvSteps = if (binding.tvSteps.text.toString().trim().isEmpty()) 0
                    else binding.tvSteps.text.toString().toInt()

                    val tvReps = if (binding.tvSteps.text.toString().trim().isEmpty()) 0
                    else binding.tvReps.text.toString().toInt()

                    val tvDuration = if (binding.tvDuration.text.toString().trim().isEmpty()) 0
                    else binding.tvDuration.text.toString().toInt()

                    val workout = Workout(
                        calories = tvCalories,
                        distance = tvDistance,
                        amount = tvReps,
                        steps = tvSteps,
                        exerciseType = ExerciseType.RUNNING_SEMI_MANUAL.toString(),
                        dateEnd = LocalDateTime.now().toString(),
                        dateStart = LocalDateTime.now()
                            .plusMinutes(tvDuration.toLong()).toString(), //data de fim é redefinida mais a frente
                        userEmail = workoutViewModel.getCurrentUserEmail()
                    )
                    listener.onDialogPositiveClick(workout)
                }
                .setNegativeButton("Descartar") { dialog, id ->
                    dialog.cancel()
                    listener.onDialogNegativeClick()
                }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun getGeocode(latLng: LatLng) {
        val retrofitClient = GeoapifyAPI.getApi()
        retrofitClient.getGeocode(
            latLng.latitude.toString(),
            latLng.longitude.toString(),
        ).enqueue(object : Callback<GeoapifyGeocodeResponse> {
            override fun onResponse(call: Call<GeoapifyGeocodeResponse>, response: Response<GeoapifyGeocodeResponse>) {
                if (response.isSuccessful) {
                    Log.i(TAG, response.body().toString())
                } else {
                    Toast.makeText(requireContext(), "Localizacao Invalida", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GeoapifyGeocodeResponse>, t: Throwable) {
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(exerciseType: String) =
            InsertInfoWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putString(EXERCISE_TYPE, exerciseType)
                }
            }
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(workout: Workout)
        fun onDialogNegativeClick()
    }

}