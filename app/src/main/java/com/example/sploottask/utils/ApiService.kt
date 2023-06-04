package com.example.sploottask.utils

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("findplacefromtext/json")
    fun searchPlace(
        @Query("input") place: String,
        @Query("inputtype") inputType: String,
        @Query("key") key: String,
        @Query("fields") fields: String
    ): Call<SearchPlaceResult>

    @GET("nearbysearch/json")
    fun getNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String,
    ): Call<NearbyResult>?

}

class SearchPlaceResult {
    @SerializedName("candidates")
    val candidates : List<SearchPlace>? = null
}

class SearchPlace {
    @SerializedName("name")
    val name : String = ""
    @SerializedName("formatted_address")
    val address : String = ""
    @SerializedName("rating")
    val rating : Float = 0f
    @SerializedName("photos")
    val photos : List<Photo>? = null
    @SerializedName("geometry")
    val geometry : Geometry? = null
}

class NearbyResult {
    @SerializedName("results")
    val results : List<Place>? = null
}

class Place {
    @SerializedName("name")
    var name : String = ""
    @SerializedName("vicinity")
    var address : String = ""
    @SerializedName("rating")
    var rating : Float = 0f
    @SerializedName("photos")
    var photos : List<Photo>? = null
    @SerializedName("geometry")
    var geometry : Geometry? = null
}

class Geometry {
    @SerializedName("location")
    val location : Location? = null
}

class Location {
    @SerializedName("lat")
    val lat : Double = 0.0
    @SerializedName("lng")
    val lng : Double = 0.0
}

class Photo {
    @SerializedName("photo_reference")
    val photoReference : String = ""
    @SerializedName("width")
    val width : Int = 0
    @SerializedName("height")
    val height : Int = 0
}