package com.example.schedulemanager.lisetener

/**
 * 데이터 변경 리스너
 */
interface OnNotifyListener {
    fun onUpdateListener()
    fun onDeleteListener(position:Int)
}