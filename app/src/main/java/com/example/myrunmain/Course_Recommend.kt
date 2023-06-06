package com.example.myrunmain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.myrunmain.databinding.FragmentCourseRecommendBinding
import java.io.File
import java.util.*
import android.text.TextUtils
import android.util.Log
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.FileWriter


class CourseRecomFragment : Fragment() {
    private var binding: FragmentCourseRecommendBinding ?= null
    private var runningList = ArrayList<CourseData>()
    private var currentList = ArrayList<CourseData>()
    private val courseItemClickListener = object : CourseAdapter.CourseItemClickListener {
        override fun onCourseItemClick(courseData: CourseData) {
            //Toast.makeText(this, "일일 코스 선택이 완료되었습니다. \n 난이도 : ${courseData.level}", Toast.LENGTH_SHORT).show()
            //toast에서 intent받아서 메시지 띄우기
            val intent_main = Intent(activity, MainActivity::class.java)
            intent_main.putExtra("goalLen", courseData.len)
            intent_main.putExtra("goalPace", courseData.pace)
            intent_main.putExtra("goalExp", courseData.exp)
            intent_main.putExtra("goalLev", courseData.level)
            startActivity(intent_main)

            try {
                writeTextFile("/data/data/com.example.myrunmain/files", "goal.txt", "${courseData.len},${courseData.pace},${courseData.level},${courseData.exp}")
            } catch ( e:Exception) {
                Toast.makeText(activity, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCourseRecommendBinding.inflate(inflater, container, false)
        val view = binding?.root

        initializeData()

        val adapter = CourseAdapter(currentList, courseItemClickListener)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.course_level,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding?.levelSpinner?.adapter = adapter
        }

        binding?.levelSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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
                } catch ( e:Exception) {
                    Toast.makeText(activity, "파일 오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        return view
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


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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
}