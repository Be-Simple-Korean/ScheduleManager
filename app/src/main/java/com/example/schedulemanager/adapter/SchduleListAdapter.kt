package com.example.schedulemanager.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calendarapp.activity.AddScheduleActivity
import com.example.calendarapp.databinding.ItemScheduleBinding
import com.example.calendarapp.lisetener.OnClickListener
import com.example.skotiln.data.ScheduleDataVO

class SchduleListAdapter : RecyclerView.Adapter<SchduleListAdapter.ItemViewHolder>() {

    companion object {
        const val DATELIST = 1
        const val FULLLUST = 2
    }

    var scheduleList = arrayListOf<ScheduleDataVO>()
    lateinit var onClickListener: OnClickListener

    inner class ItemViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
//        ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val binding =
            ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.tvItemScheduleTitle.text = scheduleList.get(position).title
        var subData = ""
        when (scheduleList.get(position).viewType) {
            DATELIST -> {
                subData = scheduleList.get(position).time
                if (scheduleList.get(position).place.trim().isNotEmpty()) {
                    subData = subData + ", " + scheduleList.get(position).place
                }
            }
            FULLLUST -> {
                subData = scheduleList.get(position).date + " " + scheduleList.get(position).time
                if (scheduleList.get(position).place.trim().isNotEmpty()) {
                    subData = subData + ", " + scheduleList.get(position).place
                }
            }
        }
        holder.binding.tvItemScheduleSubData.text = subData

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AddScheduleActivity::class.java)
            Log.e("수행", position.toString())
            intent.putExtra("isUpdate", true)
            Log.e("send date", scheduleList.get(position).date)
            intent.putExtra("date", scheduleList.get(position).date)
            intent.putExtra("position", position)
            it.context.startActivity(intent)

        }
    }

}