package com.example.schedulemanager.lisetener

import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.dialog.SearchPlaceDialog

/**
 * 위치선택 위치 아이템 선택완료 리스너
 */
interface OnPlaceDialogItemSelectedListener {
    fun onPlaceDialogItemSelectedListener(documentsVO: DocumentsVO)
}