package com.example.schedulemanager.data


/**
 * 일정 데이터 VO
 */
data class ScheduleDataVO(
    val title: String="",
    val date: String="",
    val time: String="",
    val place:String="",
    val contents: String="",
    val alarmTime:String="",
    val viewType: Int = 1
) {}