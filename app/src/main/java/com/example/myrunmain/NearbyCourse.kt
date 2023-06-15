package com.example.myrunmain

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrunmain.databinding.ActivityNearbyCourseBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import java.util.*

class NearbyCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNearbyCourseBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var googlemap: GoogleMap

    /*val songpaPath = arrayListOf<LatLng>(
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
    )*/

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@NearbyCourseActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
                val intent_sec = Intent(this@NearbyCourseActivity, DailyActivity::class.java)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {

            }
            R.id.nav_record -> {
                val intent_record = Intent(this@NearbyCourseActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@NearbyCourseActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@NearbyCourseActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@NearbyCourseActivity, SettingActivity::class.java)
                startActivity(intent_setting)
            }
        }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu_fin)
        }

        initLayout()
    }

    private fun initLayout() {
        val adapter = NearbyCourseAdapter(emptyList())
        binding.nearbyCoursesRecycler.adapter = adapter
        binding.nearbyCoursesRecycler.layoutManager = LinearLayoutManager(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // 현재 위치 표시
                val geocoder = Geocoder(this, Locale.getDefault())
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.let { addresses ->
                    if (addresses.isNotEmpty()) {
                        binding.currentLocation.text = "현재 위치: ${addresses[0].getAddressLine(0)}"
                    }
                }

                val gwangjinLocation = Location("GwangJin")
                gwangjinLocation.latitude = 37.5438
                gwangjinLocation.longitude = 127.1078

                val gwanakmtLocation = Location("GwanakMt")
                gwanakmtLocation.latitude = 37.4752
                gwanakmtLocation.longitude = 126.971

                val songpaLocation = Location("Songpa")
                songpaLocation.latitude = 37.524429
                songpaLocation.longitude = 127.115750

                val seoul1Location = Location("Seoul Course1")
                seoul1Location.latitude = 37.6894
                seoul1Location.longitude = 127.0471

                val seoul2Location = Location("Seoul Course2")
                seoul2Location.latitude = 37.6206
                seoul2Location.longitude = 127.0851

                val seoul3Location = Location("Seoul Course3")
                seoul3Location.latitude = 37.546
                seoul3Location.longitude = 127.109

                val seoul4Location = Location("Seoul Course4")
                seoul4Location.latitude = 37.4865
                seoul4Location.longitude = 127.1024

                val seoul5Location = Location("Seoul Course5")
                seoul5Location.latitude = 37.4695
                seoul5Location.longitude = 126.9783

                val seoul6Location = Location("Seoul Course6")
                seoul6Location.latitude = 37.4342
                seoul6Location.longitude = 126.9021

                val seoul7Location = Location("Seoul Course7")
                seoul7Location.latitude = 37.5637
                seoul7Location.longitude = 126.8565

                val seoul8Location = Location("Seoul Course8")
                seoul8Location.latitude = 37.6276
                seoul8Location.longitude = 126.937

                val distance1 = location.distanceTo(gwangjinLocation) / 1000
                val distance2 = location.distanceTo(gwanakmtLocation) / 1000
                val distance3 = location.distanceTo(songpaLocation) / 1000
                val distance4 = location.distanceTo(seoul1Location) / 1000
                val distance5 = location.distanceTo(seoul2Location) / 1000
                val distance6 = location.distanceTo(seoul3Location) / 1000
                val distance7 = location.distanceTo(seoul4Location) / 1000
                val distance8 = location.distanceTo(seoul5Location) / 1000
                val distance9 = location.distanceTo(seoul6Location) / 1000
                val distance10 = location.distanceTo(seoul7Location) / 1000
                val distance11 = location.distanceTo(seoul8Location) / 1000

                val courseList = listOf(
                    NearData("광진구 둘레길", "33km", "${distance1}km"),
                    NearData("관악산 둘레길", "31.2km", "${distance2}km"),
                    NearData("송파 둘레길", "21km", "${distance3}km"),
                    NearData("서울 둘레길 1코스 수락 불암산코스", "14.3km", "${distance4}km"),
                    NearData("서울 둘레길 2코스 용마 아차산코스", "12.6km", "${distance5}km"),
                    NearData("서울 둘레길 3코스 고덕 일자산코스", "26.1km", "${distance6}km"),
                    NearData("서울 둘레길 4코스 대모 우면산코스", "17.9km", "${distance7}km"),
                    NearData("서울 둘레길 5코스 관악산코스", "12.7km", "${distance8}km"),
                    NearData("서울 둘레길 6코스 안양천코스", "18km", "${distance9}km"),
                    NearData("서울 둘레길 7코스 봉산 앵봉산코스", "16.6km", "${distance10}km"),
                    NearData("서울 둘레길 8코스 북한산코스", "34.5km", "${distance11}km")
                )

                adapter.addData(courseList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}