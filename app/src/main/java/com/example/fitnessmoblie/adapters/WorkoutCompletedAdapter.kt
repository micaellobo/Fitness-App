package com.example.fitnessmoblie.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.WorkoutRecyclerItemBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.models.Workout
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WorkoutCompletedAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<WorkoutCompletedAdapter.ViewHolder>() {

    private var workouts: List<Workout> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = WorkoutRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        val distance = if (workout.distance == null) ""
        else {
            val distance = workout.distance
            val div = workout.distance!! / 1000.0
            val let = distance?.let { round(div) }

            workout.distance?.let { let }
        }

        holder.tvExercise.text = workout.exerciseType.replace("_".toRegex(), " ")
        holder.tvDate.text = LocalDateTime.parse(workout.dateStart).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        holder.tvDistance.text = "$distance km"
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(workouts.get(position)) }

        workout.exerciseType.let {
            when (it) {
                ExerciseType.RUNNING_AUTOMATIC.toString() -> {
                    holder.tvExercise.text = workout.exerciseType.dropLast(10)
                    holder.imgExercise.setImageResource(R.drawable.running)
                }
                ExerciseType.RUNNING_SEMI_MANUAL.toString() -> {
                    holder.tvExercise.text = workout.exerciseType.dropLast(12)
                    holder.imgExercise.setImageResource(R.drawable.running2)
                }
                ExerciseType.MOUNTAIN_CLIMBER.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_1)
                ExerciseType.BASIC_CRUNCHES.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_2)
                ExerciseType.BENCH_DIPS.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_3)
                ExerciseType.BICYCLE_CRUNCHES.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_4)
                ExerciseType.LEG_RAISE.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_5)
                ExerciseType.ALTERNATIVE_HELL_TOUCH.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_6)
                ExerciseType.LEG_UP_CRUNCHES.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_7)
                ExerciseType.SIT_UP.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_8)
                ExerciseType.ALTERNATIVE_V_UPS.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_9)
                ExerciseType.PLANK_ROTATION.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_10)
                ExerciseType.PLANK_WITH_LEG_LEF.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_11)
                ExerciseType.RUSSIAN_TWIST.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_12)
                ExerciseType.BRIDGE.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_13)
                ExerciseType.VERTICAL_LEG_CRUNCHES.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_14)
                ExerciseType.WIND_MILL.toString() -> holder.imgExercise.setImageResource(R.drawable.exersice_15)
                ExerciseType.CUSTOM.toString() -> holder.imgExercise.setImageResource(R.drawable.running)
            }
        }
    }

    fun updateWorkouts(workouts: List<Workout>) {
        Log.i("WORKOUTS_COMPLETED_LIST", workouts.toString())
        this.workouts = workouts
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = workouts.size

    fun getWorkoutAtPosition(int: Int): Workout = workouts[int]

    private fun round(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.UP
        val format = df.format(number).replace(",".toRegex(), ".")
        return format.toDouble()
    }

    inner class ViewHolder(binding: WorkoutRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvExercise = binding.tvExercise
        val tvDate = binding.tvDate
        val tvDistance = binding.tvDistance
        val imgExercise = binding.imgExercise
    }

    interface OnItemClickListener {
        fun onItemClick(workout: Workout)
    }
}