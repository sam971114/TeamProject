package com.example.myrunmain

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myrunmain.NearData

class NearbyCourseAdapter(
    private val courseList: List<NearData>,
    private val listener: OnCourseItemClickListener
) : RecyclerView.Adapter<NearbyCourseAdapter.ViewHolder>() {

    interface OnCourseItemClickListener {
        fun onCourseClick(course: NearData)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.near_name)
        val courseLength: TextView = itemView.findViewById(R.id.near_length)
        val courseDistance: TextView = itemView.findViewById(R.id.near_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nearby_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCourse = courseList[position]
        holder.courseName.text = currentCourse.name
        holder.courseLength.text = currentCourse.len
        holder.courseDistance.text = currentCourse.distance

        holder.itemView.setOnClickListener {
            listener.onCourseClick(currentCourse)
        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }
}
