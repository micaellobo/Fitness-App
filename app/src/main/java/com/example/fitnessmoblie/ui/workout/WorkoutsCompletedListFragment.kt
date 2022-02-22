package com.example.fitnessmoblie.ui.workout

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.adapters.WorkoutCompletedAdapter
import com.example.fitnessmoblie.databinding.FragmentWorkoutsCompletedListBinding
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.database.WorkoutViewModel
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.color.MaterialColors.ALPHA_FULL

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.util.DisplayMetrics
import com.example.fitnessmoblie.database.FitnessDb
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class WorkoutsCompletedListFragment : Fragment(), WorkoutCompletedAdapter.OnItemClickListener {

    private val binding by lazy { FragmentWorkoutsCompletedListBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseFireStore by lazy { Firebase.firestore }
    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val workoutsAdapter = WorkoutCompletedAdapter(this)

        with(binding.workoutsRecView) {
            layoutManager = when {
                workoutViewModel.count() <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, 1)
            }
            adapter = workoutsAdapter
        }
        workoutViewModel.getAll().observe(viewLifecycleOwner, { workouts ->
            workoutsAdapter.updateWorkouts(workouts)
        })

//        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
//            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//                return false
//            }
//
//            private fun convertDpToPx(dp: Int): Int {
//                return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
//            }
//
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle("Apagar Treino")
//                    .setCancelable(false)
//                    .setMessage("Tem a certeza?")
//                    .setPositiveButton("Sim") { dialog, id ->
//                        val workoutAtPosition = workoutsAdapter.getWorkoutAtPosition(viewHolder.absoluteAdapterPosition)
//                        deleteWorkoutScore(workoutAtPosition)
//                        workoutViewModel.delete(workoutAtPosition.id)
//                        Toast.makeText(requireContext(), "Treino apagado", Toast.LENGTH_SHORT).show()
//                    }
//                    .setNegativeButton("Não") { dialog, id ->
//                        dialog.cancel()
//                    }
//                    .show()
//            }
//
//            override fun onChildDraw(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
//                    // Get RecyclerView item from the ViewHolder
//                    val itemView = viewHolder.itemView
//                    val p = Paint()
//                    val icon: Bitmap
//                    if (dX > 0) {
//                        icon = BitmapFactory.decodeResource(requireContext().resources, R.drawable.delete)
//
//                        /* Set your color for positive displacement */p.setARGB(255, 255, 0, 0)
//
//                        // Draw Rect with varying right side, equal to displacement dX
//                        c.drawRect(
//                            itemView.left.toFloat(), itemView.top.toFloat(), dX,
//                            itemView.bottom.toFloat(), p
//                        )
//
//                        // Set the image icon for Right swipe
//                        c.drawBitmap(
//                            icon,
//                            itemView.left.toFloat() + convertDpToPx(16),
//                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height) / 2,
//                            p
//                        )
//                    } else {
//                        icon = BitmapFactory.decodeResource(
//                            requireContext().resources, R.drawable.delete
//                        )
//
//                        /* Set your color for negative displacement */p.setARGB(255, 255, 0, 0)
//
//                        // Draw Rect with varying left side, equal to the item's right side
//                        // plus negative displacement dX
//                        c.drawRect(
//                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
//                            itemView.right.toFloat(), itemView.bottom.toFloat(), p
//                        )
//
//                        //Set the image icon for Left swipe
//                        c.drawBitmap(
//                            icon,
//                            itemView.right.toFloat() - convertDpToPx(16) - icon.width,
//                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height) / 2,
//                            p
//                        )
//                    }
//
//                    // Fade out the view as it is swiped out of the parent's bounds
//                    val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
//                    viewHolder.itemView.alpha = alpha
//                    viewHolder.itemView.translationX = dX
//                } else {
//                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                }
//            }
//
//        }).attachToRecyclerView(binding.workoutsRecView)


        return binding.root
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
        const val TAG = "WORKOUTS_COMPLETED_LIST"

        @JvmStatic
        fun newInstance() = WorkoutsCompletedListFragment()
    }


    override fun onItemClick(workout: Workout) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.fitnessmoblie.R.id.frameLayout, DetailsWorkoutFragment.newInstance(workout))
            .addToBackStack(null)
            .commit()
    }

}