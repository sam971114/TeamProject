package com.example.myrunmain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.myrunmain.databinding.ActivityMyRunRunningBinding
import kotlinx.coroutines.NonCancellable.start
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MyRunRunning : AppCompatActivity() {
    lateinit var binding:ActivityMyRunRunningBinding

    val cal = Calendar.getInstance()
    private lateinit var textView: TextView
    private lateinit var handler:Handler
    private lateinit var runnable: Runnable
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    var destroy = 0
    var pace = 0.00
    var distance = 0.00

    var goalLen = 0.0
    var goalPace = 0.0
    var goalExp = 0
    var goalLev = 0


    companion object {
        private const val KEY_START_TIME = "start_time"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRunRunningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textView = findViewById(R.id.textView)
        handler = Handler(Looper.getMainLooper())


        startTime = intent.getLongExtra("starttime", -1)
        if(startTime.toInt() == -1) {
            startTime = System.currentTimeMillis()
        }
        elapsedTime = intent.getLongExtra("elapsedtime", -1)

        distance = intent.getDoubleExtra("distance", -1.0)
        pace = intent.getDoubleExtra("pace", -1.0)

        if (savedInstanceState != null) {
        } else {
            // 새로운 액티비티 시작 시간 설정
            startTime = System.currentTimeMillis()
        }
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24

        val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.textView5.text = timeText
        binding.textView4.text = String.format("%.2f", pace) + "  km/h"
        binding.textView2.text = String.format("%.2f", distance / 1000) + "  km"
        //화면 생성될때 intent로 받은 변수들 포함해서 textview에 값 세팅
        //updateTextView()

        initGoal()
        initRunning(savedInstanceState)

    }

    fun initRunning(savedInstanceState: Bundle?) {
        binding.apply {
            button2.setOnClickListener {
                val intent_start_btn = Intent(this@MyRunRunning, MyRunPause::class.java)
                intent_start_btn.putExtra("pause_starttime", startTime)
                intent_start_btn.putExtra("pause_elapsedtime", elapsedTime)
                intent_start_btn.putExtra("pause_distance", distance)
                intent_start_btn.putExtra("pause_pace", pace)
                startActivity(intent_start_btn)
            }
            button3.setOnClickListener {
                //의진님 코드 참고해서 text에 쓰는거 구현
                val intent_stop_btn = Intent(this@MyRunRunning, SecondActivity::class.java)
                intent_stop_btn.putExtra("second_distance", distance)


                val current = LocalDateTime.now()
                val format = DateTimeFormatter.ofPattern("YYYY.MM.dd")
                val formatted = current.format(format)

                if(goalLen !=0.0 && distance >= goalLen && pace >= goalPace) {
                    try {
                        writeRunTextFile("/data/data/com.example.myrunmain/files", "runlist.txt", "$distance,$pace,$goalLev,$formatted")
                        println("success")
                    } catch ( e:Exception) {
                        Toast.makeText(this@MyRunRunning, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    try {
                        writeRunTextFile("/data/data/com.example.myrunmain/files", "runlist.txt", "$distance,$pace,0,$formatted")
                    } catch ( e:Exception) {
                        Toast.makeText(this@MyRunRunning, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                startActivity(intent_stop_btn)
            }
            imageView2.setOnClickListener {
                val intent_start = Intent(this@MyRunRunning, MyRunPause::class.java)
                intent_start.putExtra("pause_starttime", startTime)
                intent_start.putExtra("pause_elapsedtime", elapsedTime)
                intent_start.putExtra("pause_distance", distance)
                intent_start.putExtra("pause_pace", pace)
                startActivity(intent_start)
            }
            imageView3.setOnClickListener {
                //의진님 코드 참고해서 text에 쓰는거 구현
                val intent_stop = Intent(this@MyRunRunning, SecondActivity::class.java)
                intent_stop.putExtra("second_distance", distance)


                val current = LocalDateTime.now()
                val format = DateTimeFormatter.ofPattern("YYYY.MM.dd")
                val formatted = current.format(format)

                if(goalLen !=0.0 && distance >= goalLen && pace >= goalPace) {
                    try {
                        writeRunTextFile("/data/data/com.example.myrunmain/files", "runlist.txt", "$distance,$pace,$goalLev,$formatted")
                        println("success")
                    } catch ( e:Exception) {
                        Toast.makeText(this@MyRunRunning, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    }


                }
                else {
                    try {
                        writeRunTextFile("/data/data/com.example.myrunmain/files", "runlist.txt", "$distance,$pace,0,$formatted")
                    } catch ( e:Exception) {
                        Toast.makeText(this@MyRunRunning, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                startActivity(intent_stop)
            }
        }
    }

    private fun updateTextView() {
        runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val elapsedTime =  currentTime - startTime

                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding.textView5.text = timeText

                handler.postDelayed(this, 1000) // 1초마다 업데이트
            }
        }
        handler.post(runnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 시간 값 및 경과 시간 저장
        outState.putLong(KEY_START_TIME, startTime)
        outState.putLong(KEY_ELAPSED_TIME, elapsedTime)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // 저장된 시간 값 복원
        startTime = savedInstanceState.getLong(KEY_START_TIME)
        elapsedTime = savedInstanceState.getLong(KEY_ELAPSED_TIME)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
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

    private fun initGoal() {
        try {
            val file = File("/data/data/com.example.myrunmain/files/goal.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                goalLen = runarr[0].toDouble()
                goalPace = runarr[1].toDouble()
                if(runarr[2]== "초급자")
                    goalLev = 1
                else if(runarr[2]== "중급자")
                    goalLev = 2
                else if(runarr[2]== "고급자")
                    goalLev = 3
                else if(runarr[2] =="없음")
                    goalLev = 0
                println("$goalLen , $goalPace, $goalLev")
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
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



}