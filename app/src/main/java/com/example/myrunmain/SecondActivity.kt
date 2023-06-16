package com.example.myrunmain

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myrunmain.ViewPageAdapter
import com.example.myrunmain.ChallengeData
import com.example.myrunmain.R
import com.example.myrunmain.databinding.ActivitySecondBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SecondActivity : AppCompatActivity() {
    lateinit var binding: ActivitySecondBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    var imgarr = arrayListOf<Int>(R.drawable.baseline_directions_run_24, R.drawable.sapling, R.drawable.vine, R.drawable.tree)
    var txtarr = arrayListOf<String>("전체", "초급자", "중급자", "고급자")
    val cal = Calendar.getInstance()
    var datas:ArrayList<RunData> = ArrayList()
    var goalLen = 0.0
    var goalPace = 0.0

    /*지난 주 정보, 이번 주 정보, 챌린지 저장 위치*/
    var lastweek = 0.0
    var thisweek = 0.0
    var curClg: ChallengeData? = null
    /*=====================================*/

    var updateMonth = false
    var updateWeek = false
    var data:ArrayList<RunData> = ArrayList()
    var newChall = 0.0
    var total_distance = 0.00
    var trophy = 0
    var level = 0
    var expo = 0
    var plus_expo = 0
    var distance = 0.0
    var total_challExpo = 0

    override fun onBackPressed() {
        //super.onBackPressed()
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        val intent_get = getIntent()
        distance = intent_get.getDoubleExtra("second_distance", 0.0)
        println(distance)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)


        initTrophyData()
        initLevelData()
        println(total_distance)
        total_distance += distance
        println(total_distance)
        initData()


        if(total_distance / 1000 >= newChall && newChall != 0.0) {
            plus_expo = newChall.toInt()
            println(plus_expo)
            total_challExpo += plus_expo
            println(total_challExpo)
            trophy++
            binding.progressBar.setProgress(0)
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "trophy.txt", "$trophy,0")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
            initClg()
            initlayout()
        }
        else{
            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "trophy.txt", "$trophy,$total_distance")
                println("success")
            } catch ( e:Exception) {
                Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }

            initClg()
            initlayout()
            //binding.progressBar.max = curClg!!.goal.toInt()
            //binding.progressBar.setProgress(curClg!!.cur.toInt())
            //binding.progressBar.setProgress(total_distance.toInt())
        }
//        expo += plus_expo
//        println(expo)
//        if(expo >= 100) {
//            var temp = expo % 100
//            var temp_level = expo / 100
//            expo = temp
//            println(expo)
//            level += temp_level
//        }
//        println(expo)
        println("this is $total_challExpo")
        try {
            writeTextFile("/data/data/com.example.myrunmain/files", "level.txt", "$level,$expo,$total_challExpo")
            println("expo_success")
        } catch ( e:Exception) {
            Toast.makeText(this, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
//        println(expo)

        //다시
        //+expo -> level올라가는거 구현

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@SecondActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
                val intent_sec = Intent(this@SecondActivity, DailyActivity::class.java)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this@SecondActivity, NearbyCourseActivity::class.java)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {

            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@SecondActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@SecondActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@SecondActivity, SettingActivity::class.java)
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
        //initGoal()

        initCompare()
        initlayout()


    }


    private fun initlayout(){
        binding.viewpager.adapter = ViewPageAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.tablayout, binding.viewpager){
                tab, pos ->
            tab.setIcon(imgarr[pos])
            tab.setText(txtarr[pos])
        }.attach()
        val timeTask = kotlin.concurrent.timer(period=1000){
            newWeek()
            newMonth()
        }
    }
    private fun initClg(){
        //val intent = getIntent()
        //val distance = intent.getIntExtra("distance", 0) //Running 종료로부터 받은 값
        initData()
        //val newChall = intent.getDoubleExtra("goal", 0.0)//새로운 챌린지 설정 여부
        if(newChall!=0.0){ //새로운 정보를 받은 경우
            val newClg = ChallengeData(newChall, 0.0)
            curClg = newClg
        }
        if(curClg!=null){ // 챌린지를 진행 중이라면,
            curClg!!.cur = curClg!!.cur + total_distance
            binding.curChallenge.text = "현재 챌린지 : 이번 달 " + curClg!!.goal.toString() +" km"
            //binding.nowCom.text = curClg!!.cur.toString()+" km 완료"
            //binding.nowCom.text = total_distance.toString() + " km 완료"
            binding.nowCom.text = String.format("%.2f", total_distance / 1000) + "  km 완료"
            binding.progressBar.max = curClg!!.goal.toInt()
            //binding.progressBar.setProgress(curClg!!.cur.toInt())
            binding.progressBar.setProgress((total_distance/1000).toInt())
        }

    }
    private fun initCompare(){
        val current = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("YYYY-MM-dd")
        val formatted = current.format(format)
        val dateArray = formatted.split("-").toTypedArray()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()
        cal.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
        val sf = SimpleDateFormat("MM/dd")
        val startDt = sf.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        // 해당 주차의 마지막 날짜
        val endDt = sf.format(cal.time)
        binding.thisweek.text = startDt.toString() + " ~ " + endDt.toString()
        if(thisweek<lastweek){
            binding.lastweek.visibility = View.VISIBLE
            binding.lastweek.text = "지난 주보다 " + (lastweek-thisweek).toString() + " km 덜 뛰었습니다."
        }else{
            binding.lastweek.visibility = View.INVISIBLE
        }

    }
    private fun newWeek(){
        //새로운 한 주가 시작되는지 확인 - 기록 초기화
        if(updateWeek && cal.get(Calendar.DAY_OF_WEEK)==2){//월요일
            updateWeek = false
            lastweek = thisweek
            thisweek = 0.0
            //파일 초기화 필요..
            //val newFile = File(requireActivity().filesDir, "runlit.txt")
            //newFile.writeText("")

        }else if(cal.get(Calendar.DAY_OF_WEEK)==1){
            updateWeek = true
        }
    }
    private fun newMonth(){
        //새로운 한 달이 시작되는지 확인 - 챌린지 초기화
        if(updateMonth && cal.get(Calendar.DAY_OF_MONTH)==1) {
            updateMonth = false
            curClg = null
        }else if(cal.get(Calendar.DAY_OF_MONTH)==2){
            updateMonth = true
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

    private fun initData() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/challenge.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.toDouble()
                newChall = runarr
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 데이터 읽기 오류: ${e.message}")
        }
    }

    fun writeTextFile(directory: String, filename: String, content: String) {
        /* directory가 존재하는지 검사하고 없으면 생성 */
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val writer = FileWriter(directory + "/" + filename)  // FileWriter 생성
        println("write dir=${directory + "/" + filename}")
        val buffer = BufferedWriter(writer)  // buffer에 담아서 속도 향상
        //writer.write(content+ "\n")



        buffer.write(content)
        //buffer.newLine()// buffer로 내용 쓰고
        buffer.close()  // buffer 닫기
    }

    private fun initTrophyData() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/trophy.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                trophy = runarr[0].toInt()
                total_distance = runarr[1].toDouble()
                println("$total_distance")

            }

            reader.close()
        } catch (e: Exception) {
            println("파일 트로피 읽기 오류: ${e.message}")
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
                level = runarr[0].toInt()
                expo = runarr[1].toInt()
                total_challExpo = runarr[2].toInt()
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 레벨 읽기 오류: ${e.message}")
        }
    }



}