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
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.math.RoundingMode
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
    var firstMarker = false

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

    var firalr = false
    var secalr = false
    var trdalr = false
    var finishalr = false
    var goalLen = 0
    var goalPace = 0
    var goalExp = 0
    var goalLev = 0

    var voice_on = 0
    var voice_pause = 0
    var countdown = 0
    var voice_male = 0
    var settingArray = intArrayOf(0,0,0,0)
    //voice_on / voice_pause / countdown / voice_male

    override fun onBackPressed() {
        //super.onBackPressed()
    }

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
        firstMarker = false




        val intent_goal = getIntent()
        goalLen = intent_goal.getIntExtra("goalLenn", 0)
//        goalPace = intent_goal.getIntExtra("goalPacee", 0)
//        goalExp = intent_goal.getIntExtra("goalExpp", 0)
//        goalLev = intent_goal.getIntExtra("goalLevv", 0)
        //intent값 없는경우(시작할때)

        initmap()
        initPause()
        updateTextView()
        initSettingData()
        voice_on = settingArray[0]
        countdown = settingArray[2]
        voice_pause = settingArray[1] //당일에 delay 생기는 만큼 여기 plus 해주시면 됩니당.
        voice_male = settingArray[3]




        if(voice_male == 0) {
            initFemaleTTs()
        }
        else {
            initMaleTTs()
        }

        if(countdown !=0) {
            Toast.makeText(this, "$countdown 초 뒤에 시작합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun initPause() {
        binding.apply {
            imageView5.setOnClickListener {
                destroy =1
//                val intent_pause = Intent(this@MyRunPause, MyRunRunning::class.java)
//                startActivity(intent_pause)
            }
            textView15.setOnClickListener {
                destroy =1
//                val intent_pause_btn = Intent(this@MyRunPause, MyRunRunning::class.java)
//                startActivity(intent_pause_btn)
            }

            imageView6.setOnClickListener {
                firstMarker = false
                initLocation()
                Toast.makeText(this@MyRunPause, "GPS가 갱신되었습니다.", Toast.LENGTH_SHORT).show()
                //GPS 다시 세팅
            }
            textView14.setOnClickListener {
                firstMarker = false
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

        locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000).build()

        locationRequest2 = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,2000)
            .setMinUpdateIntervalMillis(1000).build()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                if(location.locations.size == 0) return
                loc = LatLng(
                    location.locations[location.locations.size - 1].latitude,
                    location.locations[location.locations.size - 1].longitude
                )
                /*if(location.locations.size >= 2) {
                    calorie_distance = SphericalUtil.computeDistanceBetween(
                        arrLoc[location.locations.size -1], arrLoc[location.locations.size-2]
                    )
                    println(calorie_distance)
                    if(calorie_distance ==0.0) {
                        auto_pause(voice_pause)
                    }
                }*/
                setCurrentLocation(loc)

            }
        }
    }
    //위치 세팅


    private fun initSettingData() {
        try {
            var i =0
            val file = File("/data/data/com.example.myrunmain/files/setting.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
               settingArray[i] = line!!.toInt()
                i++
            }
            //initLayout()

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }

    fun auto_pause(pause_term:Int) {
        ticktok +=1
        if(ticktok == pause_term) {
            handler.removeCallbacks(runnable)
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
        if(((distance_goal / 4) <= now_distance) && firalr ==false) {
            firalr = true
            tts.speak("목표 거리의 4분의 1완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }

        if(((distance_goal / 2) <= now_distance) && secalr == false) {
            secalr = true
            tts.speak("목표 거리의 2분의 1완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }

        if(((distance_goal / 4 * 3) <= now_distance) && trdalr == false) {
            trdalr = true
            tts.speak("목표 거리의 4분의 3완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }
        if((distance_goal <= now_distance) && finishalr == false) {
            finishalr = true
            tts.speak("목표 거리 완료했습니다.", TextToSpeech.QUEUE_ADD, null, null)
        }
    }
    //거리에 따라 알람 서비스 구현

    //tts 관련 함수 시작점
    fun initMaleTTs() {
        tts = TextToSpeech(this, ) {
            isTtsReady = true
            tts.language = Locale.KOREA
            tts.setPitch(0.4f)
        }
    }

    fun initFemaleTTs() {
        tts = TextToSpeech(this, ) {
            isTtsReady = true
            tts.language = Locale.KOREA
            tts.setPitch(1f)
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
            /*if(temp_distance == 0.0) {
                auto_pause(voice_pause)
                println(ticktok)
            }*/
        }
        distance += temp_distance
        binding.textView7.text = String.format("%.2f", distance / 1000) + "  km"
        //distance alarm여기 삽입
    }
    //distance 세팅

    fun setPace(distance: Double, seconds:Long) {
        if(seconds.toInt() ==0) {
            pace = 0.0
        }
        else {
            pace = distance / seconds.toDouble() * 1000 / 3600
        }
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
        if(firstMarker == false) {
            firstMarker = true
            googleMap.addMarker(option)
        }
        //googleMap.addMarker(option)
        googleMap.addPolyline(option_poly)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }
    //현재 위치 세팅(setText통해서 distance 세팅까지)

    fun setFirstLocation(location:LatLng) {
        arrLoc.add(location)
        setText(arrLoc)
        val option = MarkerOptions()
        option.position(loc)
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        googleMap.addMarker(option)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }

    private fun updateTextView() {
        runnable = object : Runnable {
            override fun run() {
                var currentTime = System.currentTimeMillis()
                elapsedTime = before_pauseTime + currentTime - startTime

                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding.textView9.text = timeText
                setPace(distance, seconds)
                setCalorie(seconds)
                if(voice_on == 0 && goalLen !=0) {
                    var roundedNumber = BigDecimal(distance / 1000).setScale(2, RoundingMode.HALF_UP)
                    println(roundedNumber)
                    println(goalLen.toDouble() / 4)
                    distance_alarm(goalLen.toDouble(),roundedNumber.toDouble())
                }
                if(countdown !=0) {
                    handler.postDelayed(this, countdown.toLong() * 1000)
                    startTime = startTime + (countdown * 1000).toLong()
                    countdown = 0
                    //얘만 수정하면 countdown 해결
                }
                else {
                    handler.postDelayed(this, 1000) // 1초마다 업데이트
                    var temp_distance:Double = 0.0
                    if(arrLoc.size >= 2) {
                        temp_distance = SphericalUtil.computeDistanceBetween(
                            arrLoc[arrLoc.size -1], arrLoc[arrLoc.size-2]
                        )
                        if(temp_distance == 0.0 && voice_pause !=0) {
                            auto_pause(voice_pause)
                            println(ticktok)
                        }
                    }
                }

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