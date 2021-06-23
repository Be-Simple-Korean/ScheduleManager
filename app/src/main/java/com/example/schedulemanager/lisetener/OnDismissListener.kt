package com.example.schedulemanager.lisetener

import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.dialog.DeleteGuideDialog
import com.example.schedulemanager.dialog.SearchPlaceDialog
import com.example.schedulemanager.dialog.SetAlarmDialog


/**
 * 다이얼로그 닫은후의 변경리스너
 */
interface OnDismissListener {
    fun onDismissFromAlarm(dialog: SetAlarmDialog, time: String)
    fun onDismissListener(searchPlaceDialog: SearchPlaceDialog, documentsVO: DocumentsVO)
    //    fun onDismissListener(searchLocationDialog: SearchLocationDialog, name:String)
    fun onDismissListener(dialog: DeleteGuideDialog)
}