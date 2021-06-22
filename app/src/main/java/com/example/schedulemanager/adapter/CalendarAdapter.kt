package com.example.schedulemanager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulemanager.viewmodel.MyViewModel
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.databinding.ItemCalendarBinding
import com.example.schedulemanager.lisetener.OnClickListener
import java.util.*

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarItemViewHolder>() {

    companion object {
        const val FULL_COUNT = 42
        const val WEEKS = 7
        const val SUNDAY = 0
        const val SATURDAY = 6
    }

    lateinit var onClickListener: OnClickListener
    lateinit var viewModel: MyViewModel
    var dayList = arrayListOf<DateVO>()
    var curShowMonth = 0

    inner class CalendarItemViewHolder(val binding: ItemCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarAdapter.CalendarItemViewHolder {
        val bind = ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarItemViewHolder(bind)
    }

    override fun getItemCount(): Int = FULL_COUNT

    override fun onBindViewHolder(holder: CalendarAdapter.CalendarItemViewHolder, position: Int) {
        val itemViewHolder = holder as CalendarItemViewHolder
        val curDay = dayList.get(position).day
        itemViewHolder.binding.tvCalendarDate.text = curDay

        when (position % WEEKS) {
            SUNDAY -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.RED)
            SATURDAY -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.BLUE)
            else -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.BLACK)
        }

        if (!curShowMonth.toString().equals(dayList.get(position).month)) {
            itemViewHolder.binding.tvCalendarDate.alpha = 0.3f
        } else {
            itemViewHolder.binding.tvCalendarDate.alpha = 1f
            if (dayList.get(position).day.toInt() == Calendar.getInstance().get(Calendar.DATE)) {
                itemViewHolder.binding.rlCalendarToday.setBackgroundResource(R.drawable.shape_calendar_today)
            }
        }
        itemViewHolder.itemView.setOnClickListener(View.OnClickListener {
            viewModel.curSelectDateVO=dayList.get(position)
            onClickListener.onCalendarItemClickListener(
                dayList.get(position),
                when (position % WEEKS) {
                    0 -> "일"
                    1 -> "월"
                    2 -> "화"
                    3 -> "수"
                    4 -> "목"
                    5 -> "금"
                    else -> "토"
                }
            )
        })

        if (::viewModel.isInitialized) {
            val date =
                dayList.get(position).year + "." + dayList.get(position).month + "." + dayList.get(position).day
            val sql = "select * from calendar where date = '$date'"
            val cursor = DBManager.select(sql, viewModel)

            if (cursor.count > NO_DATA) {
                itemViewHolder.binding.ivCalendarSchedule.visibility = View.VISIBLE
            } else {
                itemViewHolder.binding.ivCalendarSchedule.visibility = View.INVISIBLE
            }

        }

    }

}