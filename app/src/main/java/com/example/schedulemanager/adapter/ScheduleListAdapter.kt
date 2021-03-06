package com.example.schedulemanager.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulemanager.activity.AddScheduleActivity
import com.example.schedulemanager.data.ScheduleDataVO
import com.example.schedulemanager.databinding.ItemScheduleBinding

class ScheduleListAdapter(val requestActivityResultLauncher: ActivityResultLauncher<Intent>) : RecyclerView.Adapter<ScheduleListAdapter.ItemViewHolder>() {

    companion object {
        const val DATELIST = 1
        const val FULLLUST = 2
    }

    var scheduleList = arrayListOf<ScheduleDataVO>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.tvItemScheduleTitle.text = scheduleList[position].title
        var subData = ""
        when (scheduleList[position].viewType) {
            DATELIST -> {
                subData = scheduleList[position].time
                if (scheduleList[position].place.trim().isNotEmpty()) {
                    subData = subData + ", " + scheduleList[position].place
                }
            }
            FULLLUST -> {
                subData = scheduleList[position].date + " " + scheduleList[position].time
                if (scheduleList[position].place.trim().isNotEmpty()) {
                    subData = subData + ", " + scheduleList[position].place
                }
            }
        }
        holder.binding.tvItemScheduleSubData.text = subData

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AddScheduleActivity::class.java)
            intent.putExtra("isUpdate", true)
            intent.putExtra("id", scheduleList[position].id)
            requestActivityResultLauncher.launch(intent)
        }
    }

    /**
     * ??????????????? ?????????
     */
    inner class ItemViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

}