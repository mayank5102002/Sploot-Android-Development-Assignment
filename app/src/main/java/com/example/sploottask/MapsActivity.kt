package com.example.sploottask

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.sploottask.databinding.ActivityMapsBinding
import com.example.sploottask.ui.CustomDialogFragment
import com.example.sploottask.ui.MainViewModel
import com.example.sploottask.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var viewModel : MainViewModel

    private val mapLoaded = MutableLiveData(false)
    private var currMarker : Marker? = null

    private val nearbySearches = MutableLiveData(false)

    private lateinit var spinner : Spinner
    private var currFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        init()
    }

    fun init(){
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        spinner = binding.spinnerFilter

        initListeners()
        initObservers()
        initSpinner()
        starterDialog()
    }

    private fun initListeners(){
        binding.btnSearch.setOnClickListener(){
            val location = binding.etSearch.text.toString()
            if (location.isEmpty()){
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.getSearchPlace(location)
        }
        binding.btnGps.setOnClickListener {
            if(!AppPermissions.checkGpsPermission(this)) {
                AppPermissions.requestGpsPermission(this)
            } else {
                if(GoogleGPS.isLocationEnabled(this)) {
                    GoogleGPS.locationEnabled.postValue(true)
                }
            }
        }
    }

    private fun starterDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Information")
            .setMessage(R.string.alert_message)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })
        val dialog: android.app.AlertDialog = builder.create()
        dialog.show()
    }

    private fun initMapListener(){
        mMap.setOnMarkerClickListener { marker ->
            if(marker == currMarker){
                Toast.makeText(this, "Current Location", Toast.LENGTH_SHORT).show()
                return@setOnMarkerClickListener true
            }

            val place = viewModel.markersToDetailsLiveData.value?.get(marker.id)
            if (place != null) {
                val photoRef = place.photos?.get(0)?.photoReference
                val lat = place.geometry?.location?.lat
                val lng = place.geometry?.location?.lng
                val dialogFragment = CustomDialogFragment.newInstance(place.name, place.address, place.rating, photoRef?:"", lat?:0.0, lng?:0.0)
                dialogFragment.show(supportFragmentManager, "CustomDialog")
                return@setOnMarkerClickListener true
            }
            true
        }
    }

    private fun initObservers(){

        mapLoaded.observe(this){
            if (it){
                initMapListener()
            }
        }

        GoogleGPS.locationEnabled.observe(this){
            if(it) {
                locationEnabled()
                GoogleGPS.afterLocationEnabled()
            }
        }

        AppPermissions.locationPermissionGranted.observe(this) {
            if(it) {
                if(GoogleGPS.isLocationEnabled(this)) {
                    locationEnabled()
                }
                AppPermissions.afterLocationPermission()
            }
        }

        viewModel.locationLiveData.observe(this){
            if (it != null){
//                Toast.makeText(this, "Location found", Toast.LENGTH_SHORT).show()

                binding.btnGps.setImageResource(R.drawable.gps_fixed)
                currFilter = "All"
                spinner.setSelection(0)
                markCurrentLocation(it)
            }
        }

        RetrofitClient.nearbyPlacesFetched.observe(this){
            if(it){
                val results = RetrofitClient.nearbyPlaces
                for (result in results!!){
                    val lat = result.geometry?.location?.lat
                    val lng = result.geometry?.location?.lng
                    if(lat != 0.0 && lng != 0.0){
                        addMarker(result)
                    }
                }
            }
            nearbySearches.postValue(true)
        }

        RetrofitClient.searchPlaceFetched.observe(this){
            if (it){
                val result = RetrofitClient.searchPlace
                val lat = result?.geometry?.location?.lat
                val lng = result?.geometry?.location?.lng
                if(lat != 0.0 && lng != 0.0){
                    mMap.clear()
                    viewModel.clearMap()

                    val place = Place()
                    place.name = result!!.name
                    place.address = result.address
                    place.geometry = result.geometry
                    place.rating = result.rating
                    place.photos = result.photos

                    addMarkerAndMove(place)
                }
            }
        }
    }

    private fun markCurrentLocation(it : android.location.Location){
        mMap.clear()
        viewModel.clearMap()
        val location = LatLng(it.latitude, it.longitude)
        val markerOptions = MarkerOptions().position(location)
        val marker = mMap.addMarker(markerOptions)
        currMarker = marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        viewModel.getNearbyPlaces(it.latitude, it.longitude, ApiKey.radius, "")
    }

    private fun initSpinner(){
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.filter_options, android.R.layout.simple_spinner_dropdown_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Get the selected filter option
                if(nearbySearches.value == false) {
                    return
                }

                // Handle the selected filter option
                when (spinner.getItemAtPosition(position).toString()) {
                    "All" -> {
                        if(currFilter == "All" || viewModel.locationLiveData.value == null) {
                            return
                        }
                        currFilter = "All"
                        markCurrentLocation(viewModel.locationLiveData.value!!)
                        viewModel.getNearbyPlaces(viewModel.locationLiveData.value!!.latitude, viewModel.locationLiveData.value!!.longitude, ApiKey.radius, "")
                    }
                    "Restaurants" -> {
                        if(currFilter == "Restaurants" || viewModel.locationLiveData.value == null) {
                            return
                        }
                        currFilter = "Restaurants"
                        markCurrentLocation(viewModel.locationLiveData.value!!)
                        viewModel.getNearbyPlaces(viewModel.locationLiveData.value!!.latitude, viewModel.locationLiveData.value!!.longitude, ApiKey.radius, "restaurant")
                    }
                    "Parks" -> {
                        if(currFilter == "Parks" || viewModel.locationLiveData.value == null) {
                            return
                        }
                        currFilter = "Parks"
                        markCurrentLocation(viewModel.locationLiveData.value!!)
                        viewModel.getNearbyPlaces(viewModel.locationLiveData.value!!.latitude, viewModel.locationLiveData.value!!.longitude, ApiKey.radius, "park")
                    }
                    "Museums" -> {
                        if(currFilter == "Museums" || viewModel.locationLiveData.value == null) {
                            return
                        }
                        currFilter = "Museums"
                        markCurrentLocation(viewModel.locationLiveData.value!!)
                        viewModel.getNearbyPlaces(viewModel.locationLiveData.value!!.latitude, viewModel.locationLiveData.value!!.longitude, ApiKey.radius, "museum")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case when nothing is selected (optional)
            }
        }
    }

    private fun locationEnabled(){
//        Toast.makeText(this, "Getting location", Toast.LENGTH_SHORT).show()
        viewModel.getCurrentLocation(this)
    }

    private fun addMarkerAndMove(place: Place){
        val lat = place.geometry?.location?.lat
        val lng = place.geometry?.location?.lng
        val location = LatLng(lat!!, lng!!)
        val markerOptions = MarkerOptions().position(location)
        val marker = mMap.addMarker(markerOptions)
        viewModel.addMarkerToDetails(marker!!.id, place)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
    private fun addMarker(place: Place){
        val lat = place.geometry?.location?.lat
        val lng = place.geometry?.location?.lng
        val location = LatLng(lat!!, lng!!)
        val markerOptions = MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        val marker = mMap.addMarker(markerOptions)
        viewModel.addMarkerToDetails(marker!!.id, place)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val delhi = LatLng(28.614188601621887, 77.22134471808998)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 10f))

        mapLoaded.value = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == AppPermissions.GPS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppPermissions.locationPermissionGranted.value = true
            } else {
                Log.d("Sms Messages Read Permission", "Permission not granted")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleGPS.REQUEST_ENABLE_LOCATION) {
            if (resultCode == RESULT_OK) {
                // Location services are now enabled, start location updates
                GoogleGPS.locationEnabled.value = true
            } else {
                // Location services are still disabled, show an error message or take other action
            }
        }
    }
}