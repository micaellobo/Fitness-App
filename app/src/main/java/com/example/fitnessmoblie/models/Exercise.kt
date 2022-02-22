package com.example.fitnessmoblie.models

data class Exercise(val exerciseType: ExerciseType, val reps: String, val interval: String) {

    companion object {
        val exercises = arrayOf(
            Exercise(ExerciseType.MOUNTAIN_CLIMBER, "15", "60"),
            Exercise(ExerciseType.BASIC_CRUNCHES, "15", "60"),
            Exercise(ExerciseType.BENCH_DIPS, "15", "60"),
            Exercise(ExerciseType.BICYCLE_CRUNCHES, "2", "60"),
            Exercise(ExerciseType.LEG_RAISE, "15", "60"),
            Exercise(ExerciseType.ALTERNATIVE_HELL_TOUCH, "15", "60"),
            Exercise(ExerciseType.LEG_UP_CRUNCHES, "15", "60"),
            Exercise(ExerciseType.SIT_UP, "15", "60"),
            Exercise(ExerciseType.ALTERNATIVE_V_UPS, "15", "60"),
            Exercise(ExerciseType.PLANK_ROTATION, "15", "60"),
            Exercise(ExerciseType.PLANK_WITH_LEG_LEF, "15", "60"),
            Exercise(ExerciseType.RUSSIAN_TWIST, "15", "60"),
            Exercise(ExerciseType.BRIDGE, "15", "60"),
            Exercise(ExerciseType.VERTICAL_LEG_CRUNCHES, "15", "60"),
            Exercise(ExerciseType.WIND_MILL, "15", "60"),
            )
    }

}