package com.example.schedulemanager.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.schedulemanager.RequestTask
import com.example.schedulemanager.activity.MainActivity
import com.example.schedulemanager.adapter.CalendarAdapter
import com.example.schedulemanager.adapter.PlaceAdapter
import com.example.schedulemanager.adapter.SchduleListAdapter
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.data.ScheduleDataVO
import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.data.location.LocationDataVO
import com.example.schedulemanager.database.DBHelper
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.receiver.MyBroadCastReceiver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * 데이터베이스 반환 타입
 */
enum class DataBaseType {
    WRITE, READ
}

class MyViewModel : ViewModel() {

    companion object {
        private const val TAG = "MyViewModel"
        const val SHOW_TOAST = 1
        const val UPDATE_SERVICE = 2
        const val CHOOSE_ACCOUNT = 3
        const val GET_DATE = 4
        const val API_KEY = "KakaoAK 745896bb9c87ca7a3b0ff8976fe1e747"
        const val BASE_API_URL = "https://dapi.kakao.com/v2/local/search/keyword.json?"
        const val FIRST_VALUE_ADDRESS = "사용자 지정위치"
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002

        /**
         * 한번에 가져올수있는 문서의 최대 개수
         */
        const val ONE_RESULT_COUNT = 15

        /**
         * total_count 중 노출 가능한 최대 페이지수
         */
        const val MAX_PAGE_COUNT = 45
        const val NO_RESULT = 0
    }

    lateinit var mainActivity: MainActivity
    lateinit var calendarAdapter: CalendarAdapter
    lateinit var curSelectDateVO: DateVO
    lateinit var dbHelper: DBHelper

    /**
     * 달력 notify 처리
     */
    fun setCalendarNotify() {
        if (::calendarAdapter.isInitialized) {
            calendarAdapter.notifyDataSetChanged()
        }
    }

    /**
     * DB 초기화
     */
    fun setDBHelper(context: Context) {
        dbHelper = DBHelper(context, "calendar.db")
    }

    /**
     * 상단의 년.월 설정
     */
    fun setDate(pageIndex: Int) {
        var page = pageIndex
        page -= (Int.MAX_VALUE / 2)
        val date = java.util.Calendar.getInstance().run {
            add(java.util.Calendar.MONTH, page)
            time
        }
        setCurrentDate(date)
        var datetime = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(date.time)
        mainActivity.setDate(datetime)
    }

    /**
     * 달력의 날짜 설정
     */
    fun setCurrentDate(date: Date) {
        val dayList = arrayListOf<DateVO>()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.DATE, 1)
        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val nowMaxDate = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH) //현재 달의 마지막 날짜
        val beforDateCount = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1 //전달에 보여질 날짜 개수

        //전달 계산
        calendar.set(java.util.Calendar.MONTH, curMonth - 2) //전달로 값 변경 5->4
        val beforeLastDate = calendar.getActualMaximum(java.util.Calendar.DATE) //전달의 마지막 날짜
        var beforeStartDate = (beforeLastDate - beforDateCount) //전달의 데이터를 보여줄 시작날의 전날
        for (i in 1..beforDateCount) {
            dayList.add(
                DateVO(
                    calendar.get(java.util.Calendar.YEAR).toString(),
                    (calendar.get(java.util.Calendar.MONTH) + 1).toString(),
                    (++beforeStartDate).toString()
                )
            )
//            Log.e("data", dayList.get(dayList.size - 1).toString())
        }

        //현달
        calendar.set(java.util.Calendar.MONTH, curMonth - 1) //현달로 값 변경 4->5
        for (i in 1..calendar.getActualMaximum(java.util.Calendar.DATE)) { //1~31
            dayList.add(
                DateVO(
                    calendar.get(java.util.Calendar.YEAR).toString(),
                    (calendar.get(java.util.Calendar.MONTH) + 1).toString(),
                    i.toString()
                )
            )
//            Log.e("data", dayList.get(dayList.size - 1).toString())
        }
        //다음달
        calendar.set(java.util.Calendar.MONTH, curMonth) //다음달로 값 변경 5->6
        var nextMonthHeadOffset =
            7 * 6 - (beforDateCount + nowMaxDate) //달력의 총 칸수에서 현재 들어가있는 칸수를 제외한 나머지칸 계산
        for (i in 1..nextMonthHeadOffset) {
            dayList.add(
                DateVO(
                    calendar.get(java.util.Calendar.YEAR).toString(),
                    (calendar.get(java.util.Calendar.MONTH) + 1).toString(),
                    i.toString()
                )
            )
//            Log.e("data", dayList.get(dayList.size - 1).toString())
        }
        calendarAdapter.dayList = dayList
        calendarAdapter.curShowMonth = curMonth

    }

    /**
     * 하단의 날짜 설정
     */
    fun setSelectDay(selectDay: String) {
        mainActivity.setSelectDay(selectDay)
    }

    /**
     * 오늘 날짜 설정
     */
    fun setSelectToday() {
        val calendar = java.util.Calendar.getInstance()
        var date = calendar.get(java.util.Calendar.DATE).toString() + ". " +
                when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                    1 -> "일"
                    2 -> "월"
                    3 -> "화"
                    4 -> "수"
                    5 -> "목"
                    6 -> "금"
                    else -> "토"
                }
        curSelectDateVO = DateVO(
            calendar.get(java.util.Calendar.YEAR).toString(),
            (calendar.get(java.util.Calendar.MONTH) + 1).toString(),
            calendar.get(java.util.Calendar.DATE).toString()
        )

        mainActivity.setSelectDay(date)
    }

    /**
     * Database 객체 반환
     */
    fun getDatabase(type: DataBaseType): SQLiteDatabase {
        if (type == DataBaseType.WRITE) {
            return dbHelper.writableDatabase
        } else {
            return dbHelper.readableDatabase
        }
    }

    /**
     * 시간 구하기
     *
     * ex) 오후 09:30 -> 21
     */
    fun getHour(time: String): String {
        var timeFormat = time.split(" ")
        var hour = timeFormat[1].split(":")[0]
        if (timeFormat.contains("오후")) {
            hour = (hour.toInt() + 12).toString()
        }
        return hour
    }

    /**
     * 분 구하기
     *
     * ex) 오후 09:30 -> 30
     */
    fun getMin(time: String): String {
        var timeFormat = time.split(" ")
        var min = timeFormat[1].split(":")[1]
        return min
    }

    /**
     * 17,30 -> 오후 5:30
     */
    fun getFilterSelectTime(hour: Int, min: Int): String {
        var strTime = ""
        var h = ""
        var m = ""
        if (hour > 12) {
            h = (hour - 12).toString()
            if (h.length == 1) {
                h = "0$h"
            }
            strTime = "오후 $h"
        } else if (hour == 12) {
            strTime = "오후 $hour"
        } else {
            if (hour.toString().length == 1) {
                h = "0$hour"
            }
            strTime = "오전 $hour"
        }
        if (min.toString().length == 1) {
            m = "0$min"
        } else {
            m = min.toString()
        }
        strTime = "$strTime:$m"
        return strTime
    }

    /**
     * 위치정보 데이터 요청 From Kakao
     */
    fun requestPlaceData(context: Context, query: String, placeAdapter: PlaceAdapter) {
        val queue = Volley.newRequestQueue(context)
        val map = HashMap<String, String>()
        map.put(
            "Authorization",
            API_KEY
        )
        var page = 1
        var url = BASE_API_URL + "query=" + query + "&page=" + page
        val placeList = arrayListOf<DocumentsVO>()
        val documentsVO = DocumentsVO(place_name = query, address_name = FIRST_VALUE_ADDRESS)
        placeList.add(documentsVO)
        val request = object : StringRequest(
            Method.GET, url, Response.Listener { response ->
                val requestData = Gson().fromJson(response.toString(), LocationDataVO::class.java)
                if (requestData != null) {
                    val size = requestData.meta.total_count.toInt()
                    if (size != NO_RESULT) {
                        for (i in requestData.documents.indices) {
                            placeList.add(requestData.documents.get(i))
                        }
                        if (size > ONE_RESULT_COUNT) {
                            var quotient = size / ONE_RESULT_COUNT // 몫
                            val remainder = size % ONE_RESULT_COUNT // 나머지
                            if (remainder > NO_RESULT) {
                                quotient++
                            }
                            page = quotient
                            for (i in 2..page) {
                                if (i > MAX_PAGE_COUNT) {
                                    break
                                }
                                url = BASE_API_URL + "query=" + query + "&page=" + i
                                val plusRequest = object : StringRequest(
                                    Method.GET, url,
                                    Response.Listener { response ->
                                        val requestPlusData = Gson().fromJson(
                                            response.toString(),
                                            LocationDataVO::class.java
                                        )
                                        if (requestPlusData != null) {
                                            for (i in requestPlusData.documents.indices) {
                                                placeList.add(requestPlusData.documents.get(i))
                                            }
                                            placeAdapter.documentList = placeList
                                            placeAdapter.notifyDataSetChanged()
                                        } else {
                                            return@Listener
                                        }
                                    },
                                    Response.ErrorListener { }) {
                                    override fun getHeaders(): MutableMap<String, String> {
                                        return map
                                    }
                                }
                                queue.add(plusRequest)
                            }
                        }

                    }
                    placeAdapter.documentList = placeList
                    placeAdapter.notifyDataSetChanged()
                }
            }, Response.ErrorListener { }) {
            override fun getHeaders(): MutableMap<String, String> {
                return map
            }
        }
        queue.add(request)
    }

    /**
     * 일정데이터 DB에 추가
     */
    fun addSchedule(
        context: Context,
        title: String,
        time: String,
        place: String,
        placeX: String,
        placeY: String,
        contents: String,
        alarmTime: String,
        selectCal: java.util.Calendar
    ) {
        setDBHelper(context)

        val date = getDate(
            selectCal.get(Calendar.YEAR).toString(),
            (selectCal.get(Calendar.MONTH) + 1).toString(),
            selectCal.get(Calendar.DATE).toString()
        )
        var sql =
            "insert into calendar(title,date,time,place,placeX,placeY,contents,alarmTime) values('" +
                    title + "','" +
                    date + "','" +
                    time + "','" +
                    place + "','" +
                    placeX + "','" +
                    placeY + "','" +
                    contents + "','" +
                    alarmTime + "')"
        DBManager.insert(sql, this)


        val dataId = DBManager.getId(
            title, date, time, place, contents, this
        )

        if (alarmTime.trim().isNotEmpty()) {
            if (!alarmTime.equals("0") && !alarmTime.equals("")) {
                selectCal.set(
                    java.util.Calendar.MINUTE,
                    selectCal.get(java.util.Calendar.MINUTE) - alarmTime.toInt()
                )
            }
            val year = date.split("-")[0]
            val month = date.split("-")[1].toInt()
            val day = date.split("-")[2].toInt()
            val hour = time.split(":")[0]
            val min = time.split(":")[1]
            val alarmCode = year.substring(2, 4) + month.toString() + day.toString() + hour + min
            Log.e("aCode", alarmCode)
            Log.e("selCal", selectCal.time.toString())
            createAlarm(
                context,
                alarmCode,
                title,
                year + "." + month + "." + day,
                getFilterSelectTime(hour.toInt(), min.toInt()),
                dataId.toString(),
                selectCal
            )
        }
    }

    /**
     * 날짜 변환 = 2021 9 21  -> 2021-09-21
     */
    fun getDate(year: String, month: String, day: String): String {
        var m = ""
        var d = ""
        if (month.length == 1) {
            m = "0$month"
        } else {
            m = month
        }
        if (day.length == 1) {
            d = "0$day"
        } else {
            d = day
        }
        return "$year-$m-$d"
    }

    /**
     * MainActivity - 하단 데이터 리스트 세팅
     */
    fun setBottomList(dateVO: DateVO) {
        val schduleList = arrayListOf<ScheduleDataVO>()
        val date = getDate(dateVO.year, dateVO.month, dateVO.day)
        val sql =
            "select id,title,date,time,place from calendar where date='$date' order by TIME(time) asc"
        val cursor = DBManager.select(sql, this)

        while (cursor.moveToNext()) {
            schduleList.add(
                ScheduleDataVO(
                    id=cursor.getInt(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = cursor.getString(3),
                    place = cursor.getString(4)
                )
            )
        }
        mainActivity.setBottomList(schduleList)
    }

    /**
     * 하단 리스트 데이터 notify
     */
    fun setBottomListNotify() {
        if (::curSelectDateVO.isInitialized) {
            setBottomList(curSelectDateVO)
        }
    }

    /**
     * 데이터 변경
     */
    fun update(
        context: Context,
        title: String,
        time: String,
        place: String,
        x: String,
        y: String,
        contents: String,
        alarmTime: String,
        selectCal: java.util.Calendar,
        oldId: Int
    ) {
        val date = getDate(
            selectCal.get(Calendar.YEAR).toString(),
            (selectCal.get(Calendar.MONTH) + 1).toString(),
            selectCal.get(Calendar.DATE).toString()
        )
        val sql = "update calendar set title='" + title + "', date='" + date + "', " +
                "time= '" + time + "', place='" + place + "', placeX='" + x + "', placeY='" + y + "', " +
                "contents='" + contents + "', alarmTime='" + alarmTime + "' where id=" + oldId
        //cancel alarm
        cancelAlarm(context, oldId)
        DBManager.update(sql, this)
        val dataId = DBManager.getId(
            title, date, time, place, contents, this
        )
        if (alarmTime.trim().isNotEmpty()) {
            if (!alarmTime.equals("0") && !alarmTime.equals("")) {
                selectCal.set(
                    java.util.Calendar.MINUTE,
                    selectCal.get(java.util.Calendar.MINUTE) - alarmTime.toInt()
                )
            }
            val year = date.split(".")[0]
            val month = date.split(".")[1]
            val day = date.split(".")[2]
            val hour = time.split(":")[0]
            val min = time.split(":")[1]
            val alarmCode = year.substring(2, 4) + month + day + hour + min
            Log.e("aCode", alarmCode)
            Log.e("selCal", selectCal.time.toString())
            createAlarm(
                context,
                alarmCode,
                title,
                date,
                time,
                dataId.toString(),
                selectCal
            )
        }
    }

    /**
     * 알람생성
     */
    fun createAlarm(
        context: Context,
        alarmCode: String,
        title: String,
        date: String,
        time: String,
        dataID: String,
        selectDateTime: java.util.Calendar
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyBroadCastReceiver::class.java)
        Log.e("alarmCode in create", alarmCode)
        intent.putExtra("alarmCode", alarmCode)
        intent.putExtra("title", title)
        intent.putExtra("date", date)
        intent.putExtra("time", time)
        intent.putExtra("dataID", dataID)
        val pendingIntent = PendingIntent.getBroadcast(context, alarmCode.toInt(), intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("수행", "!!")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                selectDateTime.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * 알림 취소
     */
    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sql = "select date,time from calendar where id = $id"
        Log.e("sql in cancel", sql)
        val cursor = DBManager.select(sql, this)
        Log.e("cc in cancel", cursor.count.toString())
        var alarmCode = ""
        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            val year = date.split(".")[0].substring(2, 4)
            val month = date.split(".")[1]
            val day = date.split(".")[2]

            val time = cursor.getString(1)
            val hour = time.split(":")[0]
            val min = time.split(":")[1]
            alarmCode = year + month + day + hour + min
            Log.e("alarmCode in cancel", alarmCode)
        }
        if (!alarmCode.equals("")) {
            val intent = Intent(context, MyBroadCastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmCode.toInt(), intent, 0)
            alarmManager.cancel(pendingIntent)
        }

    }

    /**
     * 일정 목록 액티비티 데이터 세팅
     */
    fun setScheduleList(schduleListAdapter: SchduleListAdapter) {
        val scheduleList = arrayListOf<ScheduleDataVO>()
        val sql = "select * from calendar order by DATE(date) asc"
        val cursor = DBManager.select(sql, this)

        while (cursor.moveToNext()) {
            val scheduleDataVO: ScheduleDataVO
            if (cursor.getString(3).equals("종일")) {
                scheduleDataVO = ScheduleDataVO(
                    id=cursor.getInt(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = cursor.getString(3),
                    place = cursor.getString(4),
                    viewType = 2
                )
            } else {
                val hour = cursor.getString(3).split(":")[0]
                val min = cursor.getString(3).split(":")[1]
                getFilterSelectTime(hour.toInt(), min.toInt())
                scheduleDataVO = ScheduleDataVO(
                    id=cursor.getInt(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = getFilterSelectTime(hour.toInt(), min.toInt()),
                    place = cursor.getString(4),
                    viewType = 2
                )
            }
            scheduleList.add(scheduleDataVO)
        }
        schduleListAdapter.scheduleList = scheduleList
        schduleListAdapter.notifyDataSetChanged()

    }


    /**
     * 1. 인터넷 연결 검사
     */
    fun isDeviceOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected)
    }

    /**
     * 2. 구글플레이 서비스 설치확인
     */
    fun isGooglePlayServiceAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val sCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return sCode == ConnectionResult.SUCCESS
    }

    /**
     * 구글플레이서비스 업데이트
     */
    fun updateServiceGuide(context: Context): Int {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val sCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(sCode)) {
            return sCode
        } else {
            return 0
        }
    }

    /**
     * 데이터 요청 클래스 EXCUTE
     */
    fun requestCalendarData(mCredential: GoogleAccountCredential) {
        RequestTask(mCredential, this).execute()
    }

}