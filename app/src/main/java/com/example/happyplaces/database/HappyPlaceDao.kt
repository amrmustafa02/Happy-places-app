package com.example.happyplaces.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface HappyPlaceDao {
    @Query("SELECT * FROM happy_place_model ORDER BY id ASC")
    fun getAllPlaces(): LiveData<List<HappyPlacesModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlace(place: HappyPlacesModel)

}