package com.example.myrunmain

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myrunmain.databinding.ActivityMyRunPauseBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import java.util.*
import kotlin.collections.ArrayList


class MyRunPause : AppCompatActivity() {
    lateinit var binding:ActivityMyRunPauseBinding
    lateinit var googleMap: GoogleMap
    lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    lateinit var locationRequest2: com.google.android.gms.location.LocationRequest
    lateinit var locationCallback: LocationCallback
    var loc = LatLng(37.554752, 126.970631)
    var startupdate = false
    val arrLoc = ArrayList<LatLng>()
    var pace = 0.0

    lateinit var tts:TextToSpeech
    var isTtsReady = false

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var before_pauseTime: Long = 0
    var destroy = 0

    var distance:Double = 0.0
    var calorie:Double = 0.0
    var calorie_distance:Double = 0.0
    var ticktok:Int = 0
    //변수세팅


    //권한 설정시작
    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    val gpeSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(checkGPSProvider()) {
            //getLastLocation()
            startLocationUpdate()
        }else {
            setCurrentLocation(loc)
        }
    }

    val locationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    //getLastLocation()
                    startLocationUpdate()
                }
                else -> {
                    setCurrentLocation(loc)
                }
            }
        }


    private fun checkGPSProvider():Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    private fun showGPSSetting() {
        val builder = AlertDialog.Builder(this@MyRunPause)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "위치 설정을 허용하겠습니까?"
        )
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{ dialog, id ->
            val GpsSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            gpeSettingsLauncher.launch(GpsSettingIntent)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
                setCurrentLocation(loc)
            })
        builder.create().show()
    }

    private fun showPermissionRequestDlg() {
        val builder = AlertDialog.Builder(this@MyRunPause)
        builder.setTitle("위치 서비스 제공")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "기기의 위치를 제공하도록 설정하시겠습니까?"
        )
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            locationPermissionRequest.launch(permissions)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
                setCurrentLocation(loc)
            })
        builder.create().show()
    }

    private fun checkFineLocationPermission():Boolean {
        return ActivityCompat.checkSelfPermission(this@MyRunPause,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCoarseLocationPermission():Boolean {
        return ActivityCompat.checkSelfPermission(this@MyRunPause,
            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    //여기까지 권한 설정


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRunPauseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler(Looper.getMainLooper())
        startTime = System.currentTimeMillis()
        intent = getIntent()
        before_pauseTime = intent.getLongExtra("pause_elapsedtime", 0)
        distance = intent.getDoubleExtra("pause_distance", 0.0)
        pace = intent.getDoubleExtra("pause_pace", 0.0)
        //intent값 없는경우(시작할때)

        initmap()
        initPause()
        updateTextView()
        initTTs()
    }

    fun initPause() {
        binding.apply {
            imageView5.setOnClickListener {
                destroy =1
                val intent_pause = Intent(this@MyRunPause, MyRunRunning::class.java)
                startActivity(intent_pause)
            }
            textView15.setOnClickListener {
                destroy =1
                val intent_pause_btn = Intent(this@MyRunPause, MyRunRunning::class.java)
                startActivity(intent_pause_btn)
            }

            imageView6.setOnClickListener {
                initLocation()
                Toast.makeText(this@MyRunPause, "GPS가 갱신되었습니다.", Toast.LENGTH_SHORT).show()
                //GPS 다시 세팅
            }
            textView14.setOnClickListener {
                initLocation()
                Toast.makeText(this@MyRunPause, "GPS가 갱신되었습니다.", Toast.LENGTH_SHORT).show()
                //GPS 다시 세팅
            }
        }

    }


    private fun initmap() {
        initLocation()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it

        }
    }
    //map 세팅

    fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MyRunPause)

        locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(2000).build()

        locationRequest2 = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,10000)
            .setMinUpdateIntervalMillis(2000).build()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                if(location.locations.size == 0) return
                loc = LatLng(
                    location.locations[location.locations.size - 1].latitude,
                    location.locations[location.locations.size - 1].longitude
                )
                if(location.locations.size >= 2) {
                    calorie_distance += SphericalUtil.computeDistanceBetween(
                        arrLoc[location.locations.size -1], arrLoc[location.locations.size-2]
                    )
                }
                setCurrentLocation(loc)

            }
        }
    }
    //위치 세팅

    fun auto_pause(pause_term:Int) {
        ticktok +=2
        if(ticktok == pause_term) {
            val intent_tic = Intent(this@MyRunPause, MyRunRunning::class.java)
            intent_tic.putExtra("starttime", startTime)
            intent_tic.putExtra("elapsedtime",elapsedTime)
            intent_tic.putExtra("distance", distance)
            intent_tic.putExtra("pace", pace)
            startActivity(intent_tic)
        }
    }
    //intent로 pause_term 받은뒤에 진행
    //자동 일시정지

    fun distance_alarm(distance_goal:Double, now_distance:Double) {
        if(distance_goal / 4 == now_distance) {
            tts.speak("목표 거리의 4분의 1완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }

        if(distance_goal / 2 == now_distance) {
            tts.speak("목표 거리의 2분의 1완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }

        if(distance_goal / 4 * 3 == now_distance) {
            tts.speak("목표 거리의 2분의 1완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }
    }
    //거리에 따라 알람 서비스 구현

    //tts 관련 함수 시작점
    fun initTTs() {
        tts = TextToSpeech(this, ) {
            isTtsReady = true
            tts.language = Locale.KOREA
        }
    }

    override fun onStop() {
        super.onStop()
        tts.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
    //tts 관련 함수 끝 점


    //textview 세팅 함수 시작점
    fun setText(arrLoc:ArrayList<LatLng>) {
        var temp_distance:Double = 0.0
        if(arrLoc.size >= 2) {
            temp_distance = SphericalUtil.computeDistanceBetween(
                arrLoc[arrLoc.size -1], arrLoc[arrLoc.size-2]
            )
        }
        distance += temp_distance
        binding.textView7.text = String.format("%.2f", distance / 1000) + "  km"
        //distance alarm여기 삽입
    }
    //distance 세팅

    fun setPace(distance: Double, seconds:Long) {
        pace = distance / seconds.toDouble() * 1000 / 3600
        binding.textView11.text = String.format("%.2f", pace) + "  km/h"
    }
    //pace 세팅

    fun setCalorie(seconds : Long) {
        calorie = distance * 0.07
        binding.textView13.text = String.format("%.2f", calorie) + "  kcal"
    }
    //calorie 세팅

    fun setCurrentLocation(location: LatLng) {
        arrLoc.add(location)
        setText(arrLoc)
        val option = MarkerOptions()
        val option_poly = PolylineOptions().color(Color.GRAY).addAll(arrLoc)
        option.position(loc)
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        googleMap.addMarker(option)
        googleMap.addPolyline(option_poly)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }
    //현재 위치 세팅(setText통해서 distance 세팅까지)

    private fun updateTextView() {
        runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                elapsedTime = before_pauseTime + currentTime - startTime

                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding.textView9.text = timeText
                setPace(distance, seconds)
                setCalorie(seconds)

                handler.postDelayed(this, 1000) // 1초마다 업데이트

                if(destroy == 1) {
                    handler.removeCallbacks(runnable)
                    var str_time = startTime
                    var elsp_time = elapsedTime
                    val intent_sec = Intent(this@MyRunPause, MyRunRunning::class.java)
                    intent_sec.putExtra("starttime", str_time)
                    intent_sec.putExtra("elapsedtime",elsp_time)
                    intent_sec.putExtra("distance", distance)
                    intent_sec.putExtra("pace", pace)
                    startActivity(intent_sec)
                }
            }
        }

        handler.post(runnable)
    }
    //시간,pace,calorie 함수 한번에 설정 //intent구현
    //textview 세팅 함수 끝 점

    //location 시작,종료 관련 함수들 시작점
    override fun onResume() {
        super.onResume()
        if(!startupdate)
            startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdate()
    }

    private fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        startupdate = false
    }

    private fun startLocationUpdate() {
        when {
            checkFineLocationPermission() -> {
                if(!checkGPSProvider()) {
                    showGPSSetting()
                }else {
                    startupdate = true
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest, locationCallback, Looper.getMainLooper()
                    )
                }
            }

            checkCoarseLocationPermission() -> {
                if(!checkGPSProvider()) {
                    showGPSSetting()
                }else {
                    startupdate = true
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest2, locationCallback, Looper.getMainLooper()
                    )
                    Log.i("location2", "startLocationUpdates()")
                }
            }


            ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)->{
                showPermissionRequestDlg()
            }

            else -> {
                locationPermissionRequest.launch(permissions)
            }
        }
    }
    //location 시작,종료 관련 함수들 끝점




    companion object {
        const val MAX_SIZE = 10
    }
}