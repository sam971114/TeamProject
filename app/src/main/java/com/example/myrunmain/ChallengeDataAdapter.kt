package com.example.myrunmain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myrunmain.databinding.RowBinding

class ChallengeDataAdapter(val items:ArrayList<ChallengeData>): RecyclerView.Adapter<ChallengeDataAdapter.ViewHolder>() {
    interface OnItemClickListener{
        fun onItemClick(data: ChallengeData, adapterPosition: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: RowBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.textView.setOnClickListener {
                itemClickListener?.onItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textView.text = "한 달에 " + items[position].goal.toString() + "km"
        holder.binding.textView.background = when (items[position].cur) {
            0.0 -> ContextCompat.getDrawable(holder.binding.root.context, R.drawable.back_border)
            else -> ContextCompat.getDrawable(holder.binding.root.context, R.drawable.back_border_select)
        }
    }
}
