package com.example.myrunmain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myrunmain.databinding.ListItemBinding
import com.example.myrunmain.R
import kotlinx.coroutines.NonDisposableHandle.parent

class RunDataAdapter(private val items:ArrayList<RunData>): RecyclerView.Adapter<RunDataAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunDataAdapter.ViewHolder {
        val view = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunDataAdapter.ViewHolder, position: Int) {
        // 기록 화면 데이터 넣기
        holder.binding.CouLen.text = "코스 길이 : "+ String.format("%.2f", items[position].len / 1000) + "  km"
        holder.binding.avgVel.text = "평균 페이스 : " + String.format("%.2f", items[position].pace) + "  km/h"
        holder.binding.dif.text = when(items[position].dif){
            1 -> "난이도 : 초급자"
            2 -> "난이도 : 중급자"
            3 -> "난이도 : 고급자"
            else -> "난이도 : 없음"
        }
        holder.binding.date.text = items[position].date.replace("\"", "")
        if(holder.binding.imageView.visibility == View.INVISIBLE) {
            holder.binding.imageView.visibility = View.VISIBLE
        }
        when(items[position].dif){
            1 -> holder.binding.imageView.setImageResource(R.drawable.bronzemedal)
            2 -> holder.binding.imageView.setImageResource(R.drawable.silvermedal)
            3 -> holder.binding.imageView.setImageResource(R.drawable.goldmedal)
            else -> holder.binding.imageView.visibility = View.INVISIBLE

        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setImage(position:Int, image_: ImageView, dif:Int) {
        if(dif ==1) {
            image_.setImageResource(R.drawable.bronzemedal)
        }
        if(dif ==2) {
            image_.setImageResource(R.drawable.silvermedal)
        }
        if(dif ==3) {
            image_.setImageResource(R.drawable.goldmedal)
        }
        notifyItemChanged(position)
    }

}
