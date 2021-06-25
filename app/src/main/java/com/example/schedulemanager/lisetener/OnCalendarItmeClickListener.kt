package com.example.schedulemanager.lisetener

import com.example.schedulemanager.MonthType
import com.example.schedulemanager.data.DateVO

interface OnCalendarItmeClickListener{
    fun onCalendarItemClickListener(dateVO: DateVO, week:String, monthType: MonthType){}
}