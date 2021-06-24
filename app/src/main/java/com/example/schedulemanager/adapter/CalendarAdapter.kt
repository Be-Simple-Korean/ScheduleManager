package com.example.schedulemanager.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulemanager.R
import com.example.schedulemanager.adapter.PlaceAdapter.Companion.NO_DATA
import com.example.schedulemanager.viewmodel.MyViewModel
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.databinding.ItemCalendarBinding
import com.example.schedulemanager.lisetener.OnClickListener
import java.util.*

/**
 * 달력 RecyclerView 어댑터
 */
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

    /**
     * 달력 아이템 클래스
     */
    inner class CalendarItemViewHolder(val binding: ItemCalendarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarAdapter.CalendarItemViewHolder {
        val bind = ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarItemViewHolder(bind)
    }

    override fun getItemCount(): Int = FULL_COUNT

    override fun onBindViewHolder(holder: CalendarAdapter.CalendarItemViewHolder, position: Int) {

        holder.binding.tvCalendarDate.text = dayList[position].day

        //주말표시
        when (position % WEEKS) {
            SUNDAY -> holder.binding.tvCalendarDate.setTextColor(Color.RED)
            SATURDAY -> holder.binding.tvCalendarDate.setTextColor(Color.BLUE)
            else -> holder.binding.tvCalendarDate.setTextColor(Color.BLACK)
        }

        //현재 달 아닌경우 처리
        if (curShowMonth.toString() != dayList[position].month) {
            holder.binding.tvCalendarDate.alpha = 0.3f
            holder.binding.ivCalendarSchedule.alpha = 0.3f
        } else {
            holder.binding.tvCalendarDate.alpha = 1f
            holder.binding.ivCalendarSchedule.alpha = 1f
        }

        //셀렉터
        if (dayList[position].isSelect) {
            holder.binding.ll.isSelected = true
//            itemViewHolder.binding.tvCalendarDate.isSelected=true
            holder.binding.tvCalendarDate.setTextColor(Color.WHITE)
            holder.binding.tvCalendarDate.typeface = Typeface.DEFAULT_BOLD
        }

        //오늘 날짜
        if (checkToday(position)) {
            holder.binding.ll.setBackgroundResource(R.drawable.shape_calendar_tod)
            holder.binding.tvCalendarDate.setTextColor(Color.WHITE)
            holder.binding.tvCalendarDate.typeface = Typeface.DEFAULT_BOLD
        }

        //클릭 이벤트
        holder.itemView.setOnClickListener{
            for (i in 0 until dayList.size) {
                dayList[i].isSelect = i == position
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
            viewModel.curSelectDateVO = dayList[position]
            onClickListener.onCalendarItemClickListener(
                dayList[position],
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
        }

        //데이터 조회
        if (::viewModel.isInitialized) {
            val year = dayList[position].year
            var month = dayList[position].month
            if (month.length == 1) month = "0$month"
            var day = dayList[position].day
            if (day.length == 1) day = "0$day"
            val date = "${year}-${month}-${day}"
            val sql = "select * from calendar where date = '$date'"
            val cursor = DBManager.select(sql, viewModel)

            if (cursor.count > NO_DATA) {
                holder.binding.ivCalendarSchedule.visibility = View.VISIBLE
            } else {
                holder.binding.ivCalendarSchedule.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * 오늘날짜인지 확인
     */
    private fun checkToday(position: Int): Boolean {
        val calendar = Calendar.getInstance()
        if (dayList[position].year.toInt() == calendar.get(Calendar.YEAR)) {
            if (dayList[position].month.toInt() == (calendar.get(Calendar.MONTH) + 1)) {
                if (dayList[position].day.toInt() == calendar.get(Calendar.DATE)) {
                    return true
                }
            }
        }
        return false
    }
}