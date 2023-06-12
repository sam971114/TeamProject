package com.example.myrunmain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NearbyCourseAdapter(
    private var courseList: List<NearData>
) : RecyclerView.Adapter<NearbyCourseAdapter.ViewHolder>() {
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
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    fun addData(newData: List<NearData>) {
        courseList = newData
    }
}
