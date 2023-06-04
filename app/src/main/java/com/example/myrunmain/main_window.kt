package com.example.myrunmain

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class MainWindowFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    var selectedDistance: String ?= null
    var selectedPace: String ?= null
    var selectedExp: Int ?= null
    private val path: ArrayList<LatLng> by lazy {
        arguments?.getParcelableArrayList("path") ?: ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_window, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        view.findViewById<TextView>(R.id.distance_text_view).text = "목표 거리: $selectedDistance"
        view.findViewById<TextView>(R.id.pace_text_view).text = "평균 페이스: $selectedPace"
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (path.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .addAll(path)
                .color(Color.GRAY)
            googleMap.addPolyline(polylineOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}