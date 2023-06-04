package com.example.sploottask.utils

import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"

    private var apiService : ApiService? = null

    var searchPlace : SearchPlace? = null
    val searchPlaceFetched = MutableLiveData(false)

    var nearbyPlaces : List<Place>? = null
    val nearbyPlacesFetched = MutableLiveData(false)

    fun getSearchLocation(loc : String){

        @Synchronized
        if(apiService == null){
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        apiService?.searchPlace(loc, "textquery", ApiKey.API_KEY, "formatted_address,name,rating,photo,geometry")?.enqueue(object : retrofit2.Callback<SearchPlaceResult?> {
            override fun onResponse(call: retrofit2.Call<SearchPlaceResult?>, response: retrofit2.Response<SearchPlaceResult?>) {
                if (response.isSuccessful) {
                    val apiResponse: SearchPlaceResult? = response.body()
                    searchPlace = apiResponse?.candidates?.get(0)
                    searchPlaceFetched.value = true
                } else {
                    println(
                        "Error: " + response.code().toString() + " " + response.message()
                    )
                }
            }

            override fun onFailure(call: retrofit2.Call<SearchPlaceResult?>, t: Throwable) {
                // Handle network error for first API call
                t.printStackTrace()
            }
        })
    }

    fun getNearbyPlaces(lat : Double, lng : Double, radius : Int, type : String){
        @Synchronized
        if(apiService == null){
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        nearbyPlaces = null

        apiService?.getNearby("$lat,$lng", radius, type, ApiKey.API_KEY)?.enqueue(object : retrofit2.Callback<NearbyResult?> {
            override fun onResponse(call: retrofit2.Call<NearbyResult?>, response: retrofit2.Response<NearbyResult?>) {
                if (response.isSuccessful) {
                    val apiResponse: NearbyResult? = response.body()
                    Log.i("Result", "Name : ${apiResponse?.results?.get(0)?.name} " +
                            "\nAddress : ${apiResponse?.results?.get(0)?.address} " +
                            "\nRating : ${apiResponse?.results?.get(0)?.rating}" +
                            "\nPhoto : ${apiResponse?.results?.get(0)?.photos?.get(0)?.photoReference}")
                    nearbyPlaces = apiResponse?.results
                    nearbyPlacesFetched.value = true
                } else {
                    // Handle error response for second API call
                    println(
                        "Error: " + response.code().toString() + " " + response.message()
                    )
                }
            }

            override fun onFailure(call: retrofit2.Call<NearbyResult?>, t: Throwable) {
                // Handle network error for second API call
                t.printStackTrace()
            }
        })
    }

}