package com.example.sploottask.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

object AppPermissions {

    const val GPS_PERMISSION_CODE = 101

    val locationPermissionGranted = MutableLiveData(false)

    fun checkGpsPermission(context : Context) : Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestGpsPermission(activity : Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            GPS_PERMISSION_CODE
        )
    }

    fun afterLocationPermission() {
        locationPermissionGranted.value = false
    }

}