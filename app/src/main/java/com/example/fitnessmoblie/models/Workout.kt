package com.example.fitnessmoblie.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var userEmail: String,
    var calories: Int,
    var distance: Int = 0,
    var local: String = "Sem local",
    var steps: Int = 0,
    var amount: Int = 0,
    var exerciseType: String,
    var dateStart: String,
    var dateEnd: String
) : Serializable

enum class ExerciseType {
    RUNNING_AUTOMATIC,
    RUNNING_SEMI_MANUAL,
    MOUNTAIN_CLIMBER,
    BASIC_CRUNCHES,
    BENCH_DIPS,
    BICYCLE_CRUNCHES,
    LEG_RAISE,
    ALTERNATIVE_HELL_TOUCH,
    LEG_UP_CRUNCHES,
    SIT_UP,
    ALTERNATIVE_V_UPS,
    PLANK_ROTATION,
    PLANK_WITH_LEG_LEF,
    RUSSIAN_TWIST,
    BRIDGE,
    VERTICAL_LEG_CRUNCHES,
    WIND_MILL,
    CUSTOM
}

enum class RankingFilter {
    DISTANCE_TRAVELLED,
    CALORIES_BURNED
}


