package com.example.myrunmain

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.Surface
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myrunmain.databinding.ActivityChallegeBinding
import com.example.myrunmain.databinding.ActivitySettingBinding
import com.google.android.material.navigation.NavigationView
import java.io.*
import java.util.*

@SuppressLint("SetTextI18n")
class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    var datas:ArrayList<String> = ArrayList()
    var settingArray = intArrayOf(0,0,0,0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val dir_trophy = File("/data/data/com.example.myrunmain/files/setting.txt")
        if (!dir_trophy.exists()) {
            try {
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "0")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "0")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "0")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        else {
            initSettinglData()
        }
        //runn text들고오기

        // txt
        // voice : 0 | 1
        // ap : 0 | 4 | 10 | 30
        // cd : 0 | 4 | 6 | 10
        // voice male : 0 | 1
        initButton()
        initLayout()


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@SettingActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
                val intent_sec = Intent(this@SettingActivity, DailyActivity::class.java)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this@SettingActivity, NearbyCourseActivity::class.java)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {
                val intent_record = Intent(this@SettingActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@SettingActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@SettingActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {

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

    private fun initButton() {
        val scan = try {
            Scanner(openFileInput("setting.txt"))
        }catch (e : Exception) {
            Scanner(resources.openRawResource(R.raw.setting))
        }
        while (scan.hasNextLine()) {
            datas.add(scan.nextLine())
        }
//        binding.btnVoiceTxt.text = "음성 피드백\n" + when (datas[0]) {
//            "0" -> "on"
//            else -> "off"
//        }
//        binding.btnAutoPauseTxt.text = "러닝 자동 일시 정지\n" + when (datas[1]) {
//            "0" -> "off"
//            else -> datas[1] + "초"
//        }
//        binding.btnCountDownTxt.text = "카운트 다운\n" + when (datas[2]) {
//            "0" -> "off"
//            else -> datas[2] + "초"
//        }
//        binding.btnVoiceMaleTxt.text = "음성 피드백 성별\n" + when (datas[3]) {
//            "0" -> "여성"
//            else -> "남성"
//        }
        binding.btnVoiceTxt.text = when (datas[0]) {
            "0" -> "on"
            else -> "off"
        }
        binding.btnAutoPauseTxt.text = when (datas[1]) {
            "0" -> "off"
            else -> datas[1] + "초"
        }
        binding.btnCountDownTxt.text = when (datas[2]) {
            "0" -> "off"
            else -> datas[2] + "초"
        }
        binding.btnVoiceMaleTxt.text = when (datas[3]) {
            "0" -> "여성"
            else -> "남성"
        }
}
    private fun clearFile() {
        val fileWriter = FileWriter("/data/data/com.example.myrunmain/files/setting.txt")
        fileWriter.write("") // 빈 문자열로 내용을 덮어씁니다.
        fileWriter.close()
    }

    private fun initLayout() {
        binding.apply {
            btnVoice.setOnClickListener { // 음성 피드백; off : 0, on : 1
                datas[0] = when (datas[0]) {
                    "0" -> "1"
                    else -> "0"
                }
                clearFile()
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[0]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[1]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[2]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[3]}")
//                btnVoiceTxt.text = "음성 피드백\n" + when (datas[0]) {
//                    "0" -> "on"
//                    else -> "off"
//                }
                btnVoiceTxt.text = when (datas[0]) {
                    "0" -> "on"
                    else -> "off"
                }
            }
            btnAutoPause.setOnClickListener { // 러닝 자동 일시정지; off : 0, on : 5 | 10 | 30 초
                datas[1] = when (datas[1]) {
                    "0" -> "4"
                    "4" -> "10"
                    "10" -> "30"
                    else -> "0"
                }
                clearFile()
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[0]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[1]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[2]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[3]}")
//                btnAutoPauseTxt.text = "러닝 자동 일시 정지\n" + when (datas[1]) {
//                    "0" -> "off"
//                    else -> datas[1] + "초"
//                }
                btnAutoPauseTxt.text = when (datas[1]) {
                    "0" -> "off"
                    else -> datas[1] + "초"
                }
            }
            btnCountDown.setOnClickListener { // 카운트 다운 변경; off : 0, on : 3 | 5 | 10 초
                datas[2] = when (datas[2]) {
                    "0" -> "4"
                    "4" -> "6"
                    "6" -> "10"
                    else -> "0"
                }
                clearFile()
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[0]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[1]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[2]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[3]}")
//                btnCountDownTxt.text = "카운트 다운\n" + when (datas[2]) {
//                    "0" -> "off"
//                    else -> datas[2] + "초"
//                }
                btnCountDownTxt.text = when (datas[2]) {
                    "0" -> "off"
                    else -> datas[2] + "초"
                }
            }
            btnVoiceMale.setOnClickListener { // 음성 피드백 성별 변경; 여성 : 0, 남성 : 1
                datas[3] = when (datas[3]) {
                    "0" -> "1"
                    else -> "0"
                }
                clearFile()
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[0]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[1]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[2]}")
                writeRunTextFile("/data/data/com.example.myrunmain/files", "setting.txt", "${datas[3]}")
//                btnVoiceMaleTxt.text = "음성 피드백 성별\n" + when (datas[3]) {
//                    "0" -> "여성"
//                    else -> "남성"
//                }
                btnVoiceMaleTxt.text = when (datas[3]) {
                    "0" -> "여성"
                    else -> "남성"
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val output = PrintStream(openFileOutput("setting.txt", MODE_PRIVATE))
        for (data in datas) {
            output.println(data)
        }
        output.close()
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


    fun writeRunTextFile(directory: String, filename: String, content: String) {
        /* directory가 존재하는지 검사하고 없으면 생성 */
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(directory, filename)
        val writer = FileWriter(file, true) // 이어쓰기 모드로 파일 열기

        val buffer = BufferedWriter(writer)
        buffer.write(content)
        buffer.newLine()
        buffer.close()
    }

    private fun initSettinglData() {
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
}