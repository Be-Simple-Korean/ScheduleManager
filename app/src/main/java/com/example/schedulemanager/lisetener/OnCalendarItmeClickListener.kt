package com.example.schedulemanager.lisetener

import com.example.schedulemanager.MonthType
import com.example.schedulemanager.data.DateVO

/**
 * 달력 아이템 클릭 리스너
 */
interface OnCalendarItmeClickListener{
    fun onCalendarItemClickListener(dateVO: DateVO, week:String, monthType: MonthType){}
}