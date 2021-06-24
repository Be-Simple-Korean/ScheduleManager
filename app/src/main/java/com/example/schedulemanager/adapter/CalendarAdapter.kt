package com.example.schedulemanager.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulemanager.R
import com.example.schedulemanager.adapter.PlaceAdapter.Companion.NO_DATA
import com.example.schedulemanager.viewmodel.MyViewModel
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.databinding.ItemCalendarBinding
import com.example.schedulemanager.lisetener.OnClickListener
import kotlinx.coroutines.selects.select
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
    var layoutList = arrayListOf<LinearLayout>()
    var tvList = arrayListOf<TextView>()

    inner class CalendarItemViewHolder(val binding: ItemCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            layoutList.add(binding.ll)
            tvList.add(binding.tvCalendarDate)
            Log.e("size", layoutList.size.toString())
        }
    }

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

        //주말표시
        when (position % WEEKS) {
            SUNDAY -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.RED)
            SATURDAY -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.BLUE)
            else -> itemViewHolder.binding.tvCalendarDate.setTextColor(Color.BLACK)
        }

        //현재 달 아닌경우 처리
        if (!curShowMonth.toString().equals(dayList.get(position).month)) {
            itemViewHolder.binding.tvCalendarDate.alpha = 0.3f
            itemViewHolder.binding.ivCalendarSchedule.alpha = 0.3f
        } else {
            itemViewHolder.binding.tvCalendarDate.alpha = 1f
            itemViewHolder.binding.ivCalendarSchedule.alpha = 1f
        }

        //셀렉터
        if (dayList.get(position).isSelect) {
            itemViewHolder.binding.ll.isSelected = true
//            itemViewHolder.binding.tvCalendarDate.isSelected=true
            itemViewHolder.binding.tvCalendarDate.setTextColor(Color.WHITE)
            itemViewHolder.binding.tvCalendarDate.setTypeface(Typeface.DEFAULT_BOLD)
        }

        //오늘 날짜
        if (checkToday(position)) {
            itemViewHolder.binding.ll.setBackgroundResource(R.drawable.shape_calendar_tod)
            itemViewHolder.binding.tvCalendarDate.setTextColor(Color.WHITE)
            itemViewHolder.binding.tvCalendarDate.setTypeface(Typeface.DEFAULT_BOLD)
        }

        //클릭 이벤트
        itemViewHolder.itemView.setOnClickListener(View.OnClickListener {
            for (i in 0 until dayList.size) {
                dayList.get(i).isSelect = i == position
            }
            notifyDataSetChanged()
//            for ((index, item) in layoutList.withIndex()) {
//                if(checkToday(index)){
//                    continue
//                }
//                item.setBackgroundResource(0)
//                tvList.get(index).setTextColor(Color.BLACK)
//                tvList.get(index).typeface= Typeface.DEFAULT
//                if (index == position) {
//                    item.setBackgroundResource(R.drawable.shape_calendar_select)
//                    tvList.get(index).setTextColor(Color.WHITE)
//                    tvList.get(index).typeface= Typeface.DEFAULT_BOLD
//                }
//            }
            viewModel.curSelectDateVO = dayList.get(position)
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

        //데이터 조회
        if (::viewModel.isInitialized) {
            val year = dayList.get(position).year
            var month = dayList.get(position).month
            if (month.length == 1) month = "0$month"
            var day = dayList.get(position).day
            if (day.length == 1) day = "0$day"
            val date = "${year}-${month}-${day}"
            val sql = "select * from calendar where date = '$date'"
            val cursor = DBManager.select(sql, viewModel)

            if (cursor.count > NO_DATA) {
                itemViewHolder.binding.ivCalendarSchedule.visibility = View.VISIBLE
            } else {
                itemViewHolder.binding.ivCalendarSchedule.visibility = View.INVISIBLE
            }

        }

    }

    fun checkToday(position: Int): Boolean {
        val calendar = Calendar.getInstance()
        if (dayList.get(position).year.toInt() == calendar.get(Calendar.YEAR)) {
            if (dayList.get(position).month.toInt() == (calendar.get(Calendar.MONTH) + 1)) {
                if (dayList.get(position).day.toInt() == calendar.get(Calendar.DATE)) {
                    return true
                }
            }
        }
        return false
    }
}