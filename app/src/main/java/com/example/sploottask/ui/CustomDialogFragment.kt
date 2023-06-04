package com.example.sploottask.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.sploottask.R
import com.example.sploottask.utils.ApiKey
import com.squareup.picasso.Picasso

class CustomDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ADDRESS = "address"
        private const val ARG_RATING = "rating"
        private const val ARG_PHOTO = "photo"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"

        fun newInstance(name: String, address: String, rating: Float, photo: String, lat : Double, lng : Double): CustomDialogFragment {
            val fragment = CustomDialogFragment()
            val args = Bundle().apply {
                putString(ARG_NAME, name)
                putString(ARG_ADDRESS, address)
                putFloat(ARG_RATING, rating)
                putString(ARG_PHOTO, photo)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
            }
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = requireArguments().getString(ARG_NAME)
        val address = requireArguments().getString(ARG_ADDRESS)
        val rating = requireArguments().getFloat(ARG_RATING)
        val photo = requireArguments().getString(ARG_PHOTO)
        val latitude = requireArguments().getDouble(ARG_LAT)
        val longitude = requireArguments().getDouble(ARG_LNG)

        val dialogBuilder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_layout, null)
        val ivPhoto = dialogView.findViewById<ImageView>(R.id.ivPhoto)
        val photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photo&key=${ApiKey.API_KEY}"
        Picasso.get()
            .load(photoUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .into(ivPhoto)

        dialogView.findViewById<TextView>(R.id.tvAddress).text = address
        dialogView.findViewById<RatingBar>(R.id.ratingBar).rating = rating

        val btnDirections = dialogView.findViewById<Button>(R.id.btnDirections)
        btnDirections.setOnClickListener {
            // Get directions button clicked
            if(latitude == 0.0 && longitude == 0.0) {
                Toast.makeText(requireActivity(), "No location data available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openGoogleMapsDirections(latitude, longitude)
        }

        dialogBuilder.setView(dialogView)
            .setTitle(name)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        return dialogBuilder.create()
    }

    private fun openGoogleMapsDirections(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireActivity(), "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }
}