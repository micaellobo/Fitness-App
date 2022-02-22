package com.example.fitnessmoblie.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.fitnessmoblie.models.Workout

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM Workout WHERE userEmail = :userEmail")
    fun getAll(userEmail: String): LiveData<List<Workout>>

    @Transaction
    @Query("SELECT * FROM Workout WHERE id = :id")
    fun get(id: Int): LiveData<Workout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(workout: Workout): Long

    @Update(entity = Workout::class)
    fun update(workout: Workout)

    @Query("DELETE FROM Workout WHERE userEmail = :userEmail")
    fun deleteAll(userEmail: String)

    @Query("DELETE FROM Workout WHERE id = :id")
    fun delete(id: Int)

}