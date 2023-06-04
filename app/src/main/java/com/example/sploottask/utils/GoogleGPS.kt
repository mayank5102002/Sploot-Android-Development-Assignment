package com.example.sploottask.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task

object GoogleGPS {
    const val REQUEST_ENABLE_LOCATION = 1001
    private var googleApiClient: GoogleApiClient? = null

    val locationEnabled = MutableLiveData(false)

    fun isLocationEnabled(activity: Activity) : Boolean{
        val locationManager: LocationManager = activity.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            true
        } else {
            createGoogleApiClient(activity)
            false
        }
    }

    private fun createGoogleApiClient(activity: Activity) {
        googleApiClient = GoogleApiClient.Builder(activity.applicationContext)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    checkLocationSettings(activity)
                }

                override fun onConnectionSuspended(i: Int) {}
            })
            .addOnConnectionFailedListener { }
            .build()
        googleApiClient!!.connect()
    }

    private fun checkLocationSettings(activity: Activity) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
            .addLocationRequest(LocationRequest().setInterval(25000))
            .addLocationRequest(LocationRequest().setFastestInterval(15000))
        val client = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { // Location settings are satisfied, start location updates
            locationEnabled.value = true
            return@addOnSuccessListener
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, show the enable location services dialog
                try {
                    e.startResolutionForResult(activity, REQUEST_ENABLE_LOCATION)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    fun afterLocationEnabled() {
        locationEnabled.value = false
    }
}