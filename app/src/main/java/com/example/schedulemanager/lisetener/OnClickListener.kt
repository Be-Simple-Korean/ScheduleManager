package com.example.schedulemanager.lisetener

import android.view.View
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.data.location.DocumentsVO

interface OnClickListener {
    fun onCalendarItemClickListener(dateVO: DateVO, week:String)
    fun onClickListener(v: View, documentsVO: DocumentsVO)
}