package com.example.schedulemanager.lisetener

import com.example.calendarapp.DocumentsVO
import com.example.calendarapp.dialog.DeleteGuideDialog
import com.example.calendarapp.dialog.SearchPlaceDialog
import com.example.calendarapp.dialog.SetAlarmDialog

/**
 * 다이얼로그 닫은후의 변경리스너
 */
interface OnDismissListener {
    fun onDismissFromAlarm(dialog: SetAlarmDialog, time: String)
    fun onDismissListener(searchPlaceDialog: SearchPlaceDialog, documentsVO: DocumentsVO)
    //    fun onDismissListener(searchLocationDialog: SearchLocationDialog, name:String)
    fun onDismissListener(dialog: DeleteGuideDialog)
}