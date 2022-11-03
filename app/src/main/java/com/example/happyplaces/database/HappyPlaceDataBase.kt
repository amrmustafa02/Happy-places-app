package com.example.happyplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [HappyPlacesModel::class], version = 1, exportSchema = false)
public abstract class HappyPlaceDataBase : RoomDatabase() {

    abstract fun PlaceDao(): HappyPlaceDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HappyPlaceDataBase? = null

        fun getDatabase(context: Context): HappyPlaceDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HappyPlaceDataBase::class.java,
                    "HappyPlace_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}