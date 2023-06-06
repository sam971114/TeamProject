package com.example.myrunmain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myrunmain.CourseData
import com.example.myrunmain.databinding.CourseRowBinding

class CourseAdapter(private val runningList: List<CourseData>,
                    private val listener: CourseItemClickListener) : RecyclerView.Adapter<CourseAdapter.CourseRecomViewHolder>() {
    inner class CourseRecomViewHolder(val binding: CourseRowBinding) : RecyclerView.ViewHolder(binding.root)

    interface CourseItemClickListener {
        fun onCourseItemClick(courseData: CourseData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseRecomViewHolder {
        val binding = CourseRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return CourseRecomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseRecomViewHolder, position: Int) {
        holder.binding.distanceTextView.text = "목표 거리 : " + runningList[position].len + "km"
        holder.binding.paceTextView.text = "평균 페이스 : " + runningList[position].pace + "km/h"
        holder.binding.levelTextView.text = "난이도 : " + runningList[position].level
        holder.binding.expTextView.text = "경험치 : " + runningList[position].exp
        holder.itemView.setOnClickListener {
            listener.onCourseItemClick(runningList[position])
        }
    }

    override fun getItemCount() = runningList.size
}