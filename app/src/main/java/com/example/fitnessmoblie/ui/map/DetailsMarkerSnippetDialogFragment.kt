package com.example.fitnessmoblie.ui.map

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.fitnessmoblie.databinding.FragmentDetailsMarkerSnippetDialogBinding
import com.example.fitnessmoblie.retrofit.Properties
import com.example.fitnessmoblie.retrofit.WeatherResponse
import com.google.android.gms.maps.model.LatLng
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private const val WEATHER = "WEATHER"
private const val INFO_MARKER = "INFO_MARKER"

class DetailsMarkerSnippetDialogFragment : DialogFragment() {

    private val binding by lazy { FragmentDetailsMarkerSnippetDialogBinding.inflate(layoutInflater) }

    private lateinit var weatherResponse: WeatherResponse
    private lateinit var infoMarker: Properties

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            when (val obj = it.getSerializable(WEATHER)) {
                is WeatherResponse -> weatherResponse = obj
            }
            when (val obj = it.getSerializable(INFO_MARKER)) {
                is Properties -> infoMarker = obj
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            AlertDialog.Builder(it).setView(binding.root).create()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val temp = weatherResponse.daily[0].temp
        val weatherDescription = weatherResponse.daily[0].weather[0]
        val feelsLike = weatherResponse.daily[0].feels_like

        Log.i(TAG, infoMarker.toString())

        val local = infoMarker.suburb ?: infoMarker.city
        val distance = infoMarker.distance / 1000.0

        binding.tvValueFeelLike.text = "${feelsLike.day.toInt()}º"
        binding.tvDistance.text = "${round(distance)} km"
        binding.tvTime.text = weatherDescription.main
        binding.tvValue.text = "${temp.day.toInt()}º"
        binding.tvCity.text = local

        Glide.with(requireContext())
            .asBitmap()
            .load("http://openweathermap.org/img/wn/${weatherDescription.icon}@2x.png")
            .dontAnimate()
            .into(binding.imgIcon)

        return binding.root
    }

    private fun round(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.UP
        val format = df.format(number).replace(",".toRegex(), ".")
        return format.toDouble()
    }


    companion object {
        @JvmStatic
        fun newInstance(wheatherResponses: WeatherResponse, infoMarker: Properties) =
            DetailsMarkerSnippetDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(WEATHER, wheatherResponses)
                    putSerializable(INFO_MARKER, infoMarker)
                }
            }
    }
}