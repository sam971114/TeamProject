package com.example.myrunmain

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrunmain.databinding.FragmentNearbyCourseBinding
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.location.Address
import com.example.myrunmain.MainWindowFragment
import com.example.myrunmain.NearData
import com.example.myrunmain.NearbyCourseAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.util.*

class NearbyCourseFragment : Fragment() {
    private var binding: FragmentNearbyCourseBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var googlemap: GoogleMap

    val songpaPath = arrayListOf<LatLng>(
        LatLng(37.4798, 127.1162),
        LatLng(37.5139, 127.0695),
        LatLng(37.495, 127.1011),
        LatLng(37.5139, 127.0857),
        LatLng(37.503, 127.0739),
        LatLng(37.505, 127.0718),
        LatLng(37.5066, 127.071),
        LatLng(37.5133, 127.07),
        LatLng(37.518, 127.0693),
        LatLng(37.5187, 127.0696),
        LatLng(37.5194, 127.0707),
        LatLng(37.5181, 127.078),
        LatLng(37.5175, 127.084),
        LatLng(37.5178, 127.0881),
        LatLng(37.5202, 127.0946),
        LatLng(37.5248, 127.1034),
        LatLng(37.5244, 127.1047),
        LatLng(37.5245, 127.1157),
        LatLng(37.5243, 127.1174),
        LatLng(37.5258, 127.1225),
        LatLng(37.5238, 127.1274),
        LatLng(37.5203, 127.1292),
        LatLng(37.5203, 127.1288),
        LatLng(37.5178, 127.1293),
        LatLng(37.5162, 127.1333),
        LatLng(37.513, 127.135),
        LatLng(37.5111, 127.1359),
        LatLng(37.506, 127.1364),
        LatLng(37.5017, 127.1368),
        LatLng(37.4995, 127.1413),
        LatLng(37.4955, 127.1394),
        LatLng(37.4893, 127.1364),
        LatLng(37.485, 127.1356),
        LatLng(37.4844, 127.1348),
        LatLng(37.4805, 127.1347),
        LatLng(37.4789, 127.1338),
        LatLng(37.4775, 127.1321),
        LatLng(37.4763, 127.1288),
        LatLng(37.4763, 127.1261),
        LatLng(37.4771, 127.1214),
        LatLng(37.478, 127.1175)
    )

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNearbyCourseBinding.inflate(inflater, container, false)
        return binding?.root
    }

    /*fun DrawCourse(location: LatLng){
        songpaLoc.add(location)
        val option_poly = PolylineOptions().color(Color.GRAY).addAll(songpaLoc)
        googlemap.addPolyline(option_poly)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.nearbyCoursesRecycler?.layoutManager = LinearLayoutManager(context)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.let { addresses ->
                    if (addresses.isNotEmpty()) {
                        binding?.currentLocation?.text = "현재 위치: ${addresses[0].getAddressLine(0)}"
                    }
                }

                val songpaLocation = Location("Songpa")
                songpaLocation.latitude = 37.524429
                songpaLocation.longitude = 127.115750

                val distance = location.distanceTo(songpaLocation)

                val courseList = listOf(
                    NearData("Course 1", "5km", "1km", songpaPath),
                    NearData("Course 2", "10km", "2km", songpaPath),
                    NearData("송파 둘레길", "15km", "${distance}km", songpaPath)
                )

                val adapter = NearbyCourseAdapter(courseList, object : NearbyCourseAdapter.OnCourseItemClickListener {
                    override fun onCourseClick(course: NearData) {
                        val fragment = MainWindowFragment().apply {
                            arguments = Bundle().apply {
                                putParcelableArrayList("path", ArrayList(course.path))
                            }
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                })
                binding?.nearbyCoursesRecycler?.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}