package com.example.myrunmain

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrunmain.CourseAdapter
import com.example.myrunmain.CourseData
import com.example.myrunmain.MainWindowFragment
import com.example.myrunmain.databinding.FragmentCourseRecommendBinding

class CourseRecomFragment : Fragment() {
    private var binding: FragmentCourseRecommendBinding ?= null
    private var runningList = ArrayList<CourseData>()
    private var currentList = ArrayList<CourseData>()
    private val courseItemClickListener = object : CourseAdapter.CourseItemClickListener {
        override fun onCourseItemClick(courseData: CourseData) {
            (activity as MainWindowFragment).selectedDistance = courseData.len
            (activity as MainWindowFragment).selectedPace = courseData.pace
            (activity as MainWindowFragment).selectedExp = courseData.exp
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
            }
        }
        return view
    }

    private fun initializeData() {
        runningList.add(CourseData("5km", "5km/h", "초급자", 5))
        runningList.add(CourseData("10km", "6km/h", "중급자", 10))
        runningList.add(CourseData("15km", "8km/h", "고급자", 15))
        runningList.add(CourseData("20km", "8km/h", "고급자", 15))
        runningList.add(CourseData("3km", "5km/h", "초급자", 5))
        currentList.addAll(runningList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}