package com.example.sploottask.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sploottask.utils.Place
import com.example.sploottask.utils.RetrofitClient
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val markersToDetails : MutableLiveData<HashMap<String, Place>> = MutableLiveData(HashMap())
    val markersToDetailsLiveData : LiveData<HashMap<String, Place>>
        get() = markersToDetails

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData : LiveData<Location>
        get() = _locationLiveData

    fun getSearchPlace(location : String){
        viewModelScope.launch {
            RetrofitClient.getSearchLocation(location)
        }
    }

    fun addMarkerToDetails(markerId : String, place : Place){
        markersToDetails.value?.put(markerId, place)
    }

    fun clearMap(){
        markersToDetails.value?.clear()
    }

    fun getCurrentLocation(activity : Activity){
        viewModelScope.launch{
            if(fusedLocationClient == null)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    p0.lastLocation?.let { location ->

                        _locationLiveData.postValue(location)
                        Log.i("MainViewModel", "requestLocationUpdates: ${location.latitude} ${location.longitude}")

                        // Stop receiving location updates
                        fusedLocationClient!!.removeLocationUpdates(locationCallback)
                    }
                }
            }

            requestLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        Log.i("MainViewModel", "requestLocationUpdates: ")

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update interval in milliseconds
        }

        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    fun getNearbyPlaces(lat : Double, lng : Double, radius : Int, type : String){
        viewModelScope.launch {
            RetrofitClient.getNearbyPlaces(lat, lng, radius, type)
        }
    }

}