package com.example.fitnessmoblie.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentHomeBinding
import com.example.fitnessmoblie.models.ExerciseType
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.ui.dashboard.DashboardFragment
import com.example.fitnessmoblie.ui.map.MapLocationFragment
import com.example.fitnessmoblie.ui.ranking.RankingFragment
import com.example.fitnessmoblie.ui.workout.AutomaticWorkout
import com.example.fitnessmoblie.ui.workout.ExerciseListFragment
import com.example.fitnessmoblie.ui.workout.SemiManualWorkoutFragment
import com.example.fitnessmoblie.ui.workout.WorkoutsCompletedListFragment


class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val powerManager by lazy { requireActivity().getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager }
    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }

    private var bateryLevel = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(batteryBroadcastReceiver, intentFilter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding.carHistoric.setOnClickListener {
            replaceFragment(WorkoutsCompletedListFragment())
        }
        binding.mapCard.setOnClickListener {
            replaceFragment(MapLocationFragment())
        }
        binding.cardWorkout.setOnClickListener {
            when {
                workoutViewModel.hasWorkoutStarted() -> {
                    when {
                        workoutViewModel.getCurrentWorkout() == ExerciseType.RUNNING_AUTOMATIC -> {
                            replaceFragment(AutomaticWorkout())
                        }
                        workoutViewModel.getCurrentWorkout() == ExerciseType.RUNNING_SEMI_MANUAL -> {
                            replaceFragment(SemiManualWorkoutFragment())
                        }
                    }
                }
                bateryLevel < 40 -> {
                    replaceFragment(SemiManualWorkoutFragment())
                }
                powerManager.isPowerSaveMode -> {
                    replaceFragment(SemiManualWorkoutFragment())
                }
                else -> replaceFragment(AutomaticWorkout())
            }
        }
        binding.cardExercices.setOnClickListener {
            replaceFragment(ExerciseListFragment())
        }

        binding.cardDashboard.setOnClickListener {
            replaceFragment(DashboardFragment())
        }

        binding.cardRanking.setOnClickListener {
            replaceFragment(RankingFragment())
        }

        return binding.root
    }


    private val batteryBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.intent.action.BATTERY_CHANGED") {
                bateryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                Log.i(TAG, "onReceive: battery level $bateryLevel")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(batteryBroadcastReceiver)
    }

    fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        const val TAG = "TAG_HOME_FRAGMENT"

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}