package com.example.myrunmain

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrunmain.databinding.ActivityDailyBinding
import com.google.android.material.navigation.NavigationView
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class DailyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var runningList = ArrayList<CourseData>()
    private var currentList = ArrayList<CourseData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@DailyActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_daily -> {
            }
            R.id.nav_nearby -> {
                val intent_trd = Intent(this@DailyActivity, NearbyCourseActivity::class.java)
                startActivity(intent_trd)
            }
            R.id.nav_record -> {
                val intent_record = Intent(this@DailyActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@DailyActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@DailyActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@DailyActivity, SettingActivity::class.java)
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
        initializeData()
    }

    private fun initLayout() {
        val adapter = CourseAdapter(currentList, courseItemClickListener)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        ArrayAdapter.createFromResource(
            this,
            R.array.course_level,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.levelSpinner.adapter = adapter
        }

        binding.levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLevel = parent.getItemAtPosition(position).toString()
                currentList.clear()
                if (selectedLevel == "전체") {
                    currentList.addAll(runningList)
                } else {
                    currentList.addAll(runningList.filter { it.level == selectedLevel })
                }
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                try {
                    writeTextFile("/data/data/com.example.myrunmain/files", "goal.txt", "0,0,없음,0")
                } catch (e: Exception) {
                    Toast.makeText(this@DailyActivity, "파일 오류: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private val courseItemClickListener = object : CourseAdapter.CourseItemClickListener {
        override fun onCourseItemClick(courseData: CourseData) {
            val intent_main = Intent(this@DailyActivity, MainActivity::class.java)
            intent_main.putExtra("goalLen", courseData.len)
            intent_main.putExtra("goalPace", courseData.pace)
            intent_main.putExtra("goalExp", courseData.exp)
            intent_main.putExtra("goalLev", courseData.level)
            startActivity(intent_main)

            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "goal.txt", "${courseData.len},${courseData.pace},${courseData.level},${courseData.exp}")
            } catch (e: Exception) {
                Toast.makeText(this@DailyActivity, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeData() {
        runningList.add(CourseData(3, 3, "초급자", 5))
        runningList.add(CourseData(5, 3, "초급자", 5))
        runningList.add(CourseData(10, 3, "초급자", 5))
        runningList.add(CourseData(20, 3, "초급자", 5))
        runningList.add(CourseData(30, 3, "초급자", 5))

        runningList.add(CourseData(3, 6, "중급자", 10))
        runningList.add(CourseData(5, 6, "중급자", 10))
        runningList.add(CourseData(10, 6, "중급자", 10))
        runningList.add(CourseData(20, 6, "중급자", 10))
        runningList.add(CourseData(30, 6, "중급자", 10))

        runningList.add(CourseData(3, 9, "고급자", 15))
        runningList.add(CourseData(5, 9, "고급자", 15))
        runningList.add(CourseData(10, 9, "고급자", 15))
        runningList.add(CourseData(20, 9, "고급자", 15))
        runningList.add(CourseData(30, 9, "고급자", 15))
        currentList.addAll(runningList)
    }

    fun writeTextFile(directory: String, filename: String, content: String) {
        /* directory가 존재하는지 검사하고 없으면 생성 */
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val writer = FileWriter(directory + "/" + filename)  // FileWriter 생성
        Log.d("FileUtil","write dir=${directory + "/" + filename}")
        val buffer = BufferedWriter(writer)  // buffer에 담아서 속도 향상

        buffer.write(content)  // buffer로 내용 쓰고
        buffer.close()  // buffer 닫기
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