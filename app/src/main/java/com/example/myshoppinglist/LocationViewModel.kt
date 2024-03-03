package com.example.myshoppinglist

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.log

class LocationViewModel : ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private val _address = mutableStateOf(listOf<GeocodingResults>())
    val address : State<List<GeocodingResults>> = _address

    fun updateLocation(newLocation:LocationData) {
        _location.value = newLocation
    }

    fun fetchAddress(latlng:String) {
        try {
            viewModelScope.launch {
                val result = RetrofitClient.create().getAddressfromcoordinates(
                    latlng,
                    "AIzaSyDa39KNQGnjb8UFlGz8CoRP8eZnXMuI924"
                )
                _address.value = result.results
            }
        }catch (e:Exception) {
            //Exception which we need to show up in the logCat Screen
            Log.d("res1","${e.cause} ${e.message}")
        }
    }

}