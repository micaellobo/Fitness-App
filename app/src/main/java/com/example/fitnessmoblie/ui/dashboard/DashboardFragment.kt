package com.example.fitnessmoblie.ui.dashboard

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.databinding.FragmentDashboardBinding
import com.example.fitnessmoblie.models.Workout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.DayOfWeek
import java.time.LocalDateTime
import com.github.mikephil.charting.data.LineData

import com.github.mikephil.charting.data.LineDataSet

import com.github.mikephil.charting.animation.Easing
import java.util.ArrayList

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class DashboardFragment : Fragment() {


    private lateinit var lineChart: LineChart
    private var scoreList = ArrayList<Score>()

    private val binding by lazy { FragmentDashboardBinding.inflate(layoutInflater) }

    private val workoutViewModel by lazy { ViewModelProvider(requireActivity())[WorkoutViewModel::class.java] }


    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        workoutViewModel.getAll().observe(viewLifecycleOwner, { workouts ->

            pieChart(workouts)

            lineChart = binding.lineChart


            initLineChart()


            setDataToLineChart(workouts)

        })
        return binding.root
    }

    private fun pieChart(workouts: List<Workout>) {
        val pieChart: PieChart = binding.pieChart
        val total = ArrayList<PieEntry>()
        var sumCalories = 0
        var sumSteps = 0
        var sumDistance = 0

        for (i in workouts.indices) {
            sumCalories += workouts[i].calories
            sumDistance += ((workouts[i].distance) ?: 0)
            sumSteps += ((workouts[i].steps) ?: 0)
        }

        total.add(PieEntry(sumSteps.toFloat(), "Passos"))
        total.add(PieEntry(sumCalories.toFloat(), "Calorias"))
        total.add(PieEntry(sumDistance.toFloat(), "Distancia"))

        val pieDataSet = PieDataSet(total, "(Treino)")
        pieDataSet.setColors(*ColorTemplate.LIBERTY_COLORS)
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 16f
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Treino"
        pieChart.animate()
    }


    private fun initLineChart() {

        lineChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.animateX(1000, Easing.EaseInSine)

        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +90f

    }


    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < scoreList.size) {
                scoreList[index].name
            } else {
                ""
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart(workouts: List<Workout>) {
        val entries: ArrayList<Entry> = ArrayList()

        scoreList = getScoreList(workouts)

        for (i in scoreList.indices) {
            val score = scoreList[i]
            entries.add(Entry(i.toFloat(), score.calories.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineChart.invalidate()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getScoreList(workouts: List<Workout>): ArrayList<Score> {

        val workoutsByDayOfTheWeek = workouts.groupBy { LocalDateTime.parse(it.dateEnd).dayOfWeek }
        val DOM = workoutsByDayOfTheWeek.get(DayOfWeek.SUNDAY)
        val SEG = workoutsByDayOfTheWeek.get(DayOfWeek.MONDAY)
        val TER = workoutsByDayOfTheWeek.get(DayOfWeek.TUESDAY)
        val QUA = workoutsByDayOfTheWeek.get(DayOfWeek.WEDNESDAY)
        val QUI = workoutsByDayOfTheWeek.get(DayOfWeek.THURSDAY)
        val SEX = workoutsByDayOfTheWeek.get(DayOfWeek.FRIDAY)
        val SAB = workoutsByDayOfTheWeek.get(DayOfWeek.SATURDAY)


        scoreList.add(Score("DOM", (DOM?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("SEG", (SEG?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("TER", (TER?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("QUA", (QUA?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("QUI", (QUI?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("SEX", (SEX?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))
        scoreList.add(Score("SAB", (SAB?.sumOf { it.calories.toDouble() } ?: 0).toFloat()))

        return scoreList
    }


    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

data class Score(
    val name: String,
    val calories: Float,
)
