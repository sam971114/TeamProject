package com.example.myrunmain

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.renderscript.RenderScript
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myrunmain.SecondActivity
import com.example.myrunmain.WeatherActivity
import com.example.myrunmain.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import java.io.*

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var googleMap: GoogleMap
    var loc = LatLng(37.554752, 126.970631)

    lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    lateinit var locationRequest2: com.google.android.gms.location.LocationRequest
    lateinit var locationCallback: LocationCallback
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    var startupdate = false
    var level: Int = 1 // default level
    var curprogress:Int = 0 // 경험치
    var comp = arrayListOf(0,0,0,0) //초급자, 중급자, 고급자, 챌린지 완료 횟수
    //Running 끝난 후 여기에 반영해주시면 됩니다!
    //경험치 증가 값은 예전 회의 때 정해서 노션에 있습니다.
    var expo = 0
    var challengExpo = 0


    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    val gpeSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(checkGPSProvider()) {
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "위치 설정을 허용하겠습니까?"
        )
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{ dialog, id, ->
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
        val builder = AlertDialog.Builder(this)
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
        return ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCoarseLocationPermission():Boolean {
        return ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val intent_goal = getIntent()
        var goalLen = intent_goal.getIntExtra("goalLen", 0)
        var goalPace = intent_goal.getIntExtra("goalPace", 0)
        var goalExp = intent_goal.getIntExtra("goalExp", 0)
        var goalLev = intent_goal.getStringExtra("goalLev")
        var int_goalLev = 0
        if(goalLev == "초급자") {
            int_goalLev = 1
        } else if(goalLev == "중급자") {
            int_goalLev = 2
        } else if(goalLev == "고급자") {
            int_goalLev = 3
        }

        if(goalLen ==0) {
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "goal.txt", "0,0,없음,0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val dir_trophy = File("/data/data/com.example.myrunmain/files/trophy.txt")
        if (!dir_trophy.exists()) {
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "trophy.txt", "0,0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val dir_level = File("/data/data/com.example.myrunmain/files/level.txt")
        if (!dir_level.exists()) {
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "level.txt", "0,0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val dir_medal = File("/data/data/com.example.myrunmain/files/medal.txt")
        if (!dir_medal.exists()) {
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "medal.txt", "0,0,0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val dir_setting = File("/data/data/com.example.myrunmain/files/setting.txt")
        if (!dir_setting.exists()) {
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "0\n0\n0\n0\n")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        initmap()
        initMedalData()
        initLevelData()
        initTrophyData()
        initLayout()
        level = expo / 100
        curprogress = expo % 100
        setLevel()
        try {
            writeTextFile("/data/data/com.example.myrunmain/files", "trophy.txt", "${comp[3]},$challengExpo")
        } catch ( e:Exception) {
            Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            writeTextFile("/data/data/com.example.myrunmain/files", "level.txt", "$level,$expo")
        } catch ( e:Exception) {
            Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }


        if(goalLen != 0) {
            Toast.makeText(this, "현재 일일 코스 \n 목표거리 : $goalLen km , 목표 페이스 : $goalPace km/h", Toast.LENGTH_SHORT).show()
        }

        binding.button.setOnClickListener {
            val intent = Intent(this, MyRunPause::class.java )
            intent.putExtra("goalLenn", goalLen)
//            intent.putExtra("goalPacee", goalPace)
//            intent.putExtra("goalExpp", goalExp)
//            intent.putExtra("goalLevv", int_goalLev)
            startActivity(intent)
        }
        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {

            }
            R.id.nav_recom -> {
                val intent_sec = Intent(this, NavigationActivity::class.java)
                intent_sec.putExtra("number", 2)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this, NavigationActivity::class.java)
                intent_trd.putExtra("number", 3)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {
                val intent_record = Intent(this, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this, SettingActivity::class.java)
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

    }
    private fun initLayout() {
        binding.list.setOnClickListener {
            val nextIntent = Intent(this, SecondActivity::class.java)
            startActivity(nextIntent)
        }
        binding.weather.setOnClickListener {
            val nextIntent = Intent(this, WeatherActivity::class.java)
            startActivity(nextIntent)
        }
        //setLevel()
    }
    private fun setLevel(){
        //이 정보는 main에서 저장하는 걸로
        binding.level4.text = "Lv. "+level.toInt().toString()
        binding.levelProgressBar.progress = curprogress
        binding.medalnum1.text = comp[0].toString()+"회"
        binding.medalnum2.text = comp[1].toString()+"회"
        binding.medalnum3.text = comp[2].toString()+"회"
        binding.medalnum4.text = comp[3].toString()+"회"
    }


    private fun initmap() {
        initLocation()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
        }
    }

    fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000).build()

        locationRequest2 = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,10000)
            .setMinUpdateIntervalMillis(5000).build()

        locationCallback = object:LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                if(location.locations.size == 0) return
                loc = LatLng(
                    location.locations[location.locations.size - 1].latitude,
                    location.locations[location.locations.size - 1].longitude
                )
                val intent = Intent()
                setCurrentLocation(loc)
            }
        }
    }

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

    fun setCurrentLocation(location: LatLng) {
        val option = MarkerOptions()
        option.position(loc)
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        googleMap.addMarker(option)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }
    //여기까지 곽민재code //권한세팅,위치세팅
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //병성님 overriding 함수(터치x)


    /*override fun onResume() {
        super.onResume()
        initLayout()
    }*/

    fun writeTextFile(directory: String, filename: String, content: String) {
        /* directory가 존재하는지 검사하고 없으면 생성 */
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val writer = FileWriter(directory + "/" + filename)  // FileWriter 생성
        println("write dir=${directory + "/" + filename}")
        val buffer = BufferedWriter(writer)  // buffer에 담아서 속도 향상

        buffer.write(content)  // buffer로 내용 쓰고
        buffer.close()  // buffer 닫기
    }

    private fun initMedalData() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/medal.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                comp[0] = runarr[0].toInt()
                comp[1] = runarr[1].toInt()
                comp[2] = runarr[2].toInt()

            }
            initLayout()

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }

    private fun initLevelData() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/level.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                expo = runarr[1].toInt()
            }

            initLayout()

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }

    }

    private fun initTrophyData() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/trophy.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
               comp[3] = runarr[0].toInt()
                challengExpo = runarr[1].toDouble().toInt()
                println(challengExpo)
                println(expo)
                expo += challengExpo
                challengExpo = 0
                println(challengExpo)
                println(expo)
            }
            initLayout()

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }

}