package com.example.fitnessmoblie.ui.map

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentMapLocationBinding
import com.example.fitnessmoblie.models.Workout
import com.example.fitnessmoblie.database.FitnessDb
import com.example.fitnessmoblie.ui.workout.InsertInfoWorkoutFragment
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.model.MarkerOptions

import android.location.Geocoder
import com.example.fitnessmoblie.retrofit.*
import java.io.IOException
import java.lang.ClassCastException
import java.math.BigDecimal
import java.math.RoundingMode


const val TAG: String = "TAG_MAPS_FRAGMENT"

class MapLocationFragment : Fragment(), GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback,
    InsertInfoWorkoutFragment.NoticeDialogListener, androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }
    private val binding by lazy { FragmentMapLocationBinding.inflate(layoutInflater) }
    private lateinit var location: LatLng
    private lateinit var map: GoogleMap

    override fun onMapReady(mp: GoogleMap) {
        map = mp
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        permissionsResultCallback.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        map.setOnInfoWindowClickListener(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.searchLocation.setOnQueryTextListener(this)

        binding.spinnerRadius.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (!selectedItem.equals(getString(R.string.start_item_spinner)) && ::location.isInitialized) {
                    FitnessDb.executors.execute {
                        getPlacesByRadius(selectedItem.toInt() * 1000)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i(TAG, "Nada selecionado")
            }
        }
        return binding.root
    }


    private fun setUpMap() {
        try {
            val lastLocation = fusedLocationProviderClient.lastLocation
            lastLocation.addOnSuccessListener {
                if (it != null) {
                    location = LatLng(it.latitude, it.longitude)
                    map.isMyLocationEnabled = true
                    map.clear()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
//                    FitnessDb.executors.execute { markerCurrentLocation(location) }
                } else {
                    Toast.makeText(requireContext(), "Sem acesso à localização", Toast.LENGTH_SHORT).show()
                    map.isMyLocationEnabled = true
                    map.clear()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.650117827464996, -7.9812736575360255), 7f))
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "SEM_PERMISSOES EXEPCAO", Toast.LENGTH_SHORT).show()
            Log.i("TAG_MAPS_FRAGMENT", e.stackTraceToString())
        }
    }


    private fun showMarkets(geoapifyFitnessPlaces: GeoapifyFitnessPlaceResponse) {
        map.clear()
//        FitnessDb.executors.execute { markerCurrentLocation(location) }
        geoapifyFitnessPlaces.features.forEach {
            val local = it.properties.suburb ?: it.properties.city

            val addMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.properties.lat, it.properties.lon))
                    .title(local)
                    .flat(true)
                    .alpha(1f)
                    .snippet(it.properties.formatted)
                    .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_baseline_fitness_center_24))
            )
            addMarker?.tag = it.properties
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        try {
            val infoMarker = marker.tag as Properties
            val position = marker.position

            FitnessDb.executors.execute { getWeather(position, infoMarker) }

        } catch (e: ClassCastException) {
            Toast.makeText(requireActivity(), "Sem informação", Toast.LENGTH_SHORT).show()
        }
    }


    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private val permissionsResultCallback =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.any { it == false }) {
                Toast.makeText(requireActivity(), "Não tem permissoes de acesso a sua localização", Toast.LENGTH_SHORT).show()
            } else {
                setUpMap()
//                Log.i("TAG_MAPS_FRAGMENT", "PERMISSOES ACEITES")
            }
        }

    private fun markerCurrentLocation(latLng: LatLng) {
        val retrofitClient = GeoapifyAPI.getApi()
        retrofitClient.getGeocode(
            latLng.latitude.toString(),
            latLng.longitude.toString(),
        ).enqueue(object : Callback<GeoapifyGeocodeResponse> {
            override fun onResponse(call: Call<GeoapifyGeocodeResponse>, response: Response<GeoapifyGeocodeResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.results.isNotEmpty()) {
                        val local = body.results[0].city
                        map.addMarker(MarkerOptions().position(location).title(local))
                        Log.i(TAG, body.toString())
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro no pedido", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GeoapifyGeocodeResponse>, t: Throwable) {
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getPlacesByRadius(radius: Int) {
        val retrofitClient = GeoapifyAPI.getApi()
        retrofitClient.getPlaces(
            "circle:${location.longitude},${location.latitude},${radius}",
            "proximity:${location.longitude},${location.latitude}"
        ).enqueue(object : Callback<GeoapifyFitnessPlaceResponse> {
            override fun onResponse(call: Call<GeoapifyFitnessPlaceResponse>, response: Response<GeoapifyFitnessPlaceResponse>) {
                val geoapifyFitnessPlaces = response.body()

                if (response.isSuccessful && geoapifyFitnessPlaces != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, (((1 - ((radius / 1000f) / 20f)) * 10) / 2f) + 10f))
                    showMarkets(geoapifyFitnessPlaces)
                } else {
                    Toast.makeText(requireContext(), "Ocorreu um erro a obter locais", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GeoapifyFitnessPlaceResponse>, t: Throwable) {
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getWeather(latLng: LatLng, infoMarker: Properties) {
        val retrofitClient = WeatherAPI.getApi()
        retrofitClient.getGeocode(
            latLng.latitude.toString(),
            latLng.longitude.toString(),
        ).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, responses: Response<WeatherResponse>) {
                if (responses.isSuccessful) {
                    val weatherResponse = responses.body()
                    val daily = weatherResponse?.daily?.get(0)?.temp
                    val weatherMainDescription = weatherResponse?.daily?.get(0)?.weather

                    weatherResponse?.let { DetailsMarkerSnippetDialogFragment.newInstance(it, infoMarker) }
                        ?.show(childFragmentManager, "InsertInfoWorkout")

                } else {
                    Toast.makeText(requireContext(), "Erro no pedido", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.i(TAG, t.message.toString())
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val localToSearchView = binding.searchLocation.query.toString()
        val geocoder = Geocoder(requireContext())

        if (localToSearchView != "") {
            try {
                val addressList = geocoder.getFromLocationName(localToSearchView, 1)

                if (addressList.isEmpty()) {
                    Toast.makeText(requireContext(), "Local Inválido", Toast.LENGTH_LONG).show()
                } else {
                    val longitude = addressList[0].longitude
                    val latitude = addressList[0].latitude
//                    Log.i(TAG + "LAT_LNG", "$longitude --- $latitude")
//                    Log.i(TAG, addressList[0].toString())

                    val latLng = LatLng(latitude, longitude)

                    map.clear()
                    val addMarker = map.addMarker(MarkerOptions().position(latLng).title(localToSearchView))

                    val distance = calculateDistanceBetweenCoordinates(location, latLng).times(1000).toInt()


                    addMarker?.tag = Properties(city = localToSearchView, lat = latitude, lon = longitude, distance = distance)

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            } catch (e: IOException) {
                Log.i(TAG, e.stackTraceToString())
            }
        }
        return false
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onDialogPositiveClick(workout: Workout) {
//        FitnessDb.executors.execute {
//            Handler(Looper.getMainLooper()).post {
//            }
//        }
//        FitnessDb.executors.execute {
//            repositoryFitness.insert(workout)
//        }
        Toast.makeText(requireContext(), "Treino Guardado AINDA NAO ACABADO", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {}

    fun calculateDistanceBetweenCoordinates(latLng1: LatLng, latLng2: LatLng): Double {
        val dLat: Double = degreesToRadians(latLng1.latitude - latLng2.latitude)
        val dLon: Double = degreesToRadians(latLng1.longitude - latLng2.longitude)
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                    Math.cos(degreesToRadians(latLng2.latitude)) * Math.cos(
                degreesToRadians(latLng1.latitude)
            )
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return BigDecimal.valueOf(6371 * c).setScale(3, RoundingMode.HALF_UP).toString().toDouble()
    }

    private fun degreesToRadians(degrees: Double) = degrees * Math.PI / 180

}