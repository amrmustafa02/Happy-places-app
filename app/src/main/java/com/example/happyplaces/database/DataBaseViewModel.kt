package com.example.happyplaces.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DataBaseViewModel(app: Application) : AndroidViewModel(app) {
    var data: LiveData<List<HappyPlacesModel>>
    private var repo: HappyPlaceRepo

    init {
        val db = HappyPlaceDataBase.getDatabase(app)
        val userDao = db.PlaceDao()
        repo = HappyPlaceRepo(userDao)
        data = repo.allPLaces
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun addNewPlace(place: HappyPlacesModel) = viewModelScope.launch {
        repo.insertPlace(place)
    }
}