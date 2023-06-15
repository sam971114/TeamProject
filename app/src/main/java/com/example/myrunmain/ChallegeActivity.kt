package com.example.myrunmain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrunmain.ChallengeData
import com.example.myrunmain.ChallengeDataAdapter
import com.example.myrunmain.databinding.ActivityChallegeBinding
import com.google.android.material.navigation.NavigationView
import java.io.PrintStream
import java.util.*

class ChallegeActivity : AppCompatActivity() {
    lateinit var binding: ActivityChallegeBinding
    lateinit var adapter: ChallengeDataAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    var datas:ArrayList<ChallengeData> = ArrayList()
    var stop = 0

    override fun onBackPressed() {
        //super.onBackPressed()
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallegeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // select -> check by dialog -> change color
        // write txt now challenge at first line
        // txt will written all challenges -> have to read and show
        initData()
        initLayout()

        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@ChallegeActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
                val intent_sec = Intent(this@ChallegeActivity, DailyActivity::class.java)
                startActivity(intent_sec)
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this@ChallegeActivity, NearbyCourseActivity::class.java)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {
                val intent_record = Intent(this@ChallegeActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@ChallegeActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {

            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@ChallegeActivity, SettingActivity::class.java)
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

    private fun initData() {
        val scan = try {
            Scanner(openFileInput("challenge_list.txt"))
        }catch (e : Exception) {
            Scanner(resources.openRawResource(R.raw.challenge_list))
        }
        while (scan.hasNext()) {
            val km = scan.next().toDouble()
            val select = scan.next().toDouble()
            datas.add(ChallengeData(km, select))
        }
    }

    private fun initLayout() {
        binding.apply {
            challengeList.layoutManager = LinearLayoutManager(this@ChallegeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = ChallengeDataAdapter(datas)
            adapter.itemClickListener = object:ChallengeDataAdapter.OnItemClickListener{
                override fun onItemClick(data: ChallengeData, adapterPosition: Int) {
                    if (data.cur == 1.0)
                        cancelAlertDialog(adapter, data, adapterPosition)
                    else
                        checkAlertDialog(adapter, data, adapterPosition)
                }
            }
            challengeList.adapter = adapter
        }
    }

    fun checkAlertDialog(adapter: ChallengeDataAdapter, data: ChallengeData, adapterPosition: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("한 달에 ${data.goal}km 챌린지를 새로 시작하겠습니까?")
            .setPositiveButton("시작") {
                    _, _ -> changeData(adapter, data, adapterPosition)
                val intent_second = Intent(this@ChallegeActivity, SecondActivity::class.java)
                intent_second.putExtra("goal", data.goal)
                startActivity(intent_second)
            }.setNegativeButton("창 닫기") {
                    dlg, _ -> dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    fun cancelAlertDialog(adapter: ChallengeDataAdapter, data: ChallengeData, adapterPosition: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("한 달에 ${data.goal}km 챌린지를 종료하겠습니까?")
            .setPositiveButton("종료") {
                    _, _ -> changeData(adapter, data, adapterPosition)
                stop =1
                val intent_stop = Intent(this@ChallegeActivity, SecondActivity::class.java)
                intent_stop.putExtra("goal", 0.0)
                startActivity(intent_stop)
            }.setNegativeButton("창 닫기") {
                    dlg, _ -> dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    private fun changeData(adapter: ChallengeDataAdapter, data: ChallengeData, adapterPosition: Int) {
        data.cur = when (data.cur) {
            0.0 -> 1.0
            else -> 0.0
        }
        // 챌린지 내에서 쓸 데이터 조작
        val output = PrintStream(openFileOutput("challenge_list.txt", MODE_PRIVATE))
        for (i in datas.indices) {
            if (i != adapterPosition) { // 다른 챌린지가 켜져있다면, 꺼버리기
                datas[i] = ChallengeData(datas[i].goal, 0.0)
                output.println("${datas[i].goal} 0")
            }else if(stop ==1) {
                stop = 0
                output.println("${data.cur}")
            }


            else
                output.println("${datas[i].goal} ${data.cur}") // 선택된 챌린지 변경 적용
        }
        output.close()
        adapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        // 선택된 챌린지 전달
        val output = PrintStream(openFileOutput("challenge.txt", MODE_PRIVATE))
        var selectSomething = false
        for (data in datas) {
            if (data.cur == 1.0) {
                selectSomething = true
                output.println(data.goal)
            }
        }
        if (!selectSomething)
            output.println("0")
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
}