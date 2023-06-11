package com.example.myrunmain

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myrunmain.RunData
import com.example.myrunmain.RunDataAdapter
import com.example.myrunmain.R
import com.example.myrunmain.databinding.AllFragmentBinding
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class AllFragment : Fragment() {
    private lateinit var binding: AllFragmentBinding

    var data:ArrayList<RunData> = ArrayList()
    var easy = 0
    var middle= 0
    var diffi = 0
    var expo = 0
    var level = 0
    var challengeExpo = 0
    var trophy = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AllFragmentBinding.inflate(inflater, container, false)


        //initLevelData()
        initData()
        //initChallengeData()
        try {
            writeTextFile("/data/data/com.example.myrunmain/files", "medal.txt", "$easy,$middle,$diffi")
        } catch ( e:Exception) {
            Toast.makeText(activity, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            writeTextFile("/data/data/com.example.myrunmain/files", "level.txt", "$level,$expo")
        } catch ( e:Exception) {
            Toast.makeText(activity, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }

        binding.allRecyclerView.adapter = RunDataAdapter(data)
        return binding.root

    }

    private fun initData(){
        try {
            val file = File("/data/data/com.example.myrunmain/files/runlist.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                if(runarr[2].toInt() == 1) {
                    easy++
                    expo +=5
                }
                if(runarr[2].toInt() == 2) {
                    middle++
                    expo += 10
                }
                if(runarr[2].toInt() == 3) {
                    diffi++
                    expo += 15
                }
                data.add(RunData(runarr[0].toDouble(),runarr[1].toDouble(),runarr[2].toInt(),runarr[3]))
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
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



        buffer.write(content) // buffer로 내용 쓰고
        buffer.close()  // buffer 닫기
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
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }


    private fun initChallengeData(){
        try {
            val file = File("/data/data/com.example.myrunmain/files/trophy.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                trophy = runarr[0].toInt()
                challengeExpo = runarr[1].toInt()
                println(challengeExpo)
                println(expo)
                expo += challengeExpo
                challengeExpo = 0
                println(challengeExpo)
                println(expo)
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }
}