package com.example.schedulemanager.lisetener

import android.view.View
import com.example.calendarapp.DocumentsVO
import com.example.calendarapp.data.DateVO


interface OnClickListener {
    fun onCalendarItemClickListener(dateVO: DateVO,week:String)
    fun onClickListener(v: View, documentsVO: DocumentsVO)
}