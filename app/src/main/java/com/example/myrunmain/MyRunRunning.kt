package com.example.myrunmain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.myrunmain.databinding.ActivityMyRunRunningBinding
import kotlinx.coroutines.NonCancellable.start

class MyRunRunning : AppCompatActivity() {
    lateinit var binding:ActivityMyRunRunningBinding

    private lateinit var textView: TextView
    private lateinit var handler:Handler
    private lateinit var runnable: Runnable
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    var destroy = 0
    var pace = 0.00
    var distance = 0.00


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
                val intent_stop_btn = Intent(this@MyRunRunning, MainActivity::class.java)
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
                val intent_stop = Intent(this@MyRunRunning, MainActivity::class.java)
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

}