package com.example.fitnessmoblie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnessmoblie.models.Workout
import java.util.concurrent.Executors


@Database(entities = arrayOf(Workout::class), version = 6)
abstract class FitnessDb : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "FitnessDb"

        @Volatile
        var INSTANCE: FitnessDb? = null

        val executors = Executors.newSingleThreadExecutor()

        fun getInstance(context: Context): FitnessDb {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, FitnessDb::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    abstract fun workoutDao(): WorkoutDao


}