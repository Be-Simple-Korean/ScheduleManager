package com.example.schedulemanager.lisetener

import android.view.View
import com.example.schedulemanager.data.location.DocumentsVO

/**
 * 위치 선택 다이얼로그 위치 아이템 클릭 리스너
 */
interface OnSearchPlaceDialogItemClickListener {
    fun onPlaceDialogItemClickListener(v: View, documentsVO: DocumentsVO)
}