package com.example.myrunmain

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myrunmain.R
import com.example.myrunmain.databinding.ActivityWeatherBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherActivity : AppCompatActivity()  {
    lateinit var binding: ActivityWeatherBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    val url = "https://www.google.com/search?q=날씨"
    val scope = CoroutineScope(Dispatchers.IO)

    override fun onBackPressed() {
        //super.onBackPressed()
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@WeatherActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
                val intent_sec = Intent(this@WeatherActivity, DailyActivity::class.java)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this@WeatherActivity, NearbyCourseActivity::class.java)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {
                val intent_record = Intent(this@WeatherActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {

            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@WeatherActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@WeatherActivity, SettingActivity::class.java)
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
    private fun initLayout(){
        //1분마다 날씨 정보, 시간 정보 업데이트
        val timerTask = kotlin.concurrent.timer(period=60000){
            getWea()
            setDate()
        }
    }

    private fun setWeatherImage(weather:String):Int{
        //현재 날씨 표현 이미지 찾기
        if(weather.contains("구름") || weather.contains("흐림"))
            return R.drawable.cloudy
        else if(weather.contains("눈") || weather.contains("설"))
            return R.drawable.snowman
        else if(weather.contains("맑") || weather.contains("해"))
            return R.drawable.sunny
        else if(weather.contains("비") || weather.contains("소나기"))
            return R.drawable.rainy
        else if(weather.contains("천둥") || weather.contains("우")||weather.contains("번개"))
            return R.drawable.heavyrain
        else
            return R.drawable.air
    }

    private fun getWea(){
        runOnUiThread {
            scope.launch {
                var doc = Jsoup.connect(url).get()
                if(doc.select("#wob_tm").text()==""){
                    doc = Jsoup.connect("https://www.google.com/search?q=서울날씨").get()
                }
                val weather = doc.select("#wob_dc").text().toString() // 날씨
                val temp = doc.select("#wob_tm").text() // 기온
                val rainp = doc.select("#wob_pp").text() // 강수 확률
                val hum = doc.select("#wob_hm").text() // 습도
                binding.weather.text = weather
                binding.temp.text = temp + "도"
                binding.rainp.text = "강수 확률: " + rainp
                binding.hum.text = "습도: " + hum
                binding.WeatherImage.setImageResource(setWeatherImage(weather))
                if(temp.toInt()>32){
                    binding.Recommand.text = "높은 온도, 야외 달리기는 탈수를 일으킬 수 있습니다."
                }
                else if(temp.toInt()<5){
                    binding.Recommand.text = "추운 날씨, 온몸 이완을 위해 스트레칭은 필수!"
                }
                else if(rainp.replace("%", "").toInt()>70){
                    binding.Recommand.text = "높은 강수확률, 몸을 식히지 않도록 주의하세요."
                }
                else if(hum.replace("%", "").toInt()>40){
                    binding.Recommand.text = "높은 습도, 야외 달리기는 전해질 부족을 일으킬 수 있습니다."
                }
                else binding.Recommand.text = "야외 달리기 하기 좋은 날!"
            }
        }

    }
    private fun setDate(){
        runOnUiThread{
            val current: LocalDateTime = LocalDateTime.now()
            val format = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            val formatted = current.format(format)
            binding.NowTime.text = formatted
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