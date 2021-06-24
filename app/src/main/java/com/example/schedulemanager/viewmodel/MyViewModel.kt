package com.example.schedulemanager.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.schedulemanager.RequestTask
import com.example.schedulemanager.adapter.CalendarAdapter
import com.example.schedulemanager.adapter.PlaceAdapter
import com.example.schedulemanager.adapter.ScheduleListAdapter
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
import kotlin.collections.ArrayList

/**
 * 데이터베이스 반환 타입
 */
enum class DataBaseType {
    WRITE, READ
}

/**
 * ViewModel
 */
class MyViewModel : ViewModel() {

    companion object {
        const val API_KEY = "KakaoAK 745896bb9c87ca7a3b0ff8976fe1e747"
        const val BASE_API_URL = "https://dapi.kakao.com/v2/local/search/keyword.json?"
        const val FIRST_VALUE_ADDRESS = "사용자 지정위치"

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

    private lateinit var dbHelper: DBHelper
    lateinit var calendarAdapter: CalendarAdapter
    lateinit var curSelectDateVO: DateVO

    //상단의 타이틀
    var yearMonth = MutableLiveData<String>()

    //하단-상단-타이틀
    var dayWeeks = MutableLiveData<String>()

    //하단 리스트
    var mainSchduleList = MutableLiveData<ArrayList<ScheduleDataVO>>()
    var subSchduleList = arrayListOf<ScheduleDataVO>()

    /**
     * 달력 notify 처리
     */
    fun setCalendarNotify() {
        if (::calendarAdapter.isInitialized) {
            calendarAdapter.notifyDataSetChanged()
        }
    }

    /**
     * MainActivity - 하단 데이터 리스트 세팅
     */
    fun setBottomList(dateVO: DateVO) {
        subSchduleList.clear()
        val date = getDate(dateVO.year, dateVO.month, dateVO.day)
        val sql =
            "select id,title,date,time,place from calendar where date='$date' order by TIME(time) asc"
        val cursor = DBManager.select(sql, this)

        while (cursor.moveToNext()) {
            subSchduleList.add(
                ScheduleDataVO(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = cursor.getString(3),
                    place = cursor.getString(4)
                )
            )
        }
        mainSchduleList.postValue(subSchduleList)
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
        val date = Calendar.getInstance().run {
            add(Calendar.MONTH, page)
            time
        }
        setCurrentDate(date)
        val datetime = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(date.time)
        yearMonth.value = datetime
    }

    /**
     * 달력의 날짜 설정
     */
    fun setCurrentDate(date: Date) {
        val dayList = arrayListOf<DateVO>()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DATE, 1)
        val curMonth = calendar.get(Calendar.MONTH) + 1
        val nowMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) //현재 달의 마지막 날짜
        val beforDateCount = calendar.get(Calendar.DAY_OF_WEEK) - 1 //전달에 보여질 날짜 개수

        //전달 계산
        calendar.set(Calendar.MONTH, curMonth - 2) //전달로 값 변경 5->4
        val beforeLastDate = calendar.getActualMaximum(Calendar.DATE) //전달의 마지막 날짜
        var beforeStartDate = (beforeLastDate - beforDateCount) //전달의 데이터를 보여줄 시작날의 전날
        for (i in 1..beforDateCount) {
            dayList.add(
                DateVO(
                    calendar.get(Calendar.YEAR).toString(),
                    (calendar.get(Calendar.MONTH) + 1).toString(),
                    (++beforeStartDate).toString()
                )
            )
//            Log.e("data", dayList.get(dayList.size - 1).toString())
        }

        //현달
        calendar.set(Calendar.MONTH, curMonth - 1) //현달로 값 변경 4->5
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) { //1~31
            dayList.add(
                DateVO(
                    calendar.get(Calendar.YEAR).toString(),
                    (calendar.get(Calendar.MONTH) + 1).toString(),
                    i.toString()
                )
            )
//            Log.e("data", dayList.get(dayList.size - 1).toString())
        }
        //다음달
        calendar.set(Calendar.MONTH, curMonth) //다음달로 값 변경 5->6
        val nextMonthHeadOffset =
            7 * 6 - (beforDateCount + nowMaxDate) //달력의 총 칸수에서 현재 들어가있는 칸수를 제외한 나머지칸 계산
        for (i in 1..nextMonthHeadOffset) {
            dayList.add(
                DateVO(
                    calendar.get(Calendar.YEAR).toString(),
                    (calendar.get(Calendar.MONTH) + 1).toString(),
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
    fun setSelectDay(dateVO: DateVO, week: String) {
        val selectDay = "${dateVO.day}. $week"
        dayWeeks.value = selectDay
    }

    /**
     * 오늘 날짜 설정
     */
    fun setSelectToday(): DateVO {
        val calendar = Calendar.getInstance()
        val date = calendar.get(Calendar.DATE).toString() + ". " +
                when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    1 -> "일"
                    2 -> "월"
                    3 -> "화"
                    4 -> "수"
                    5 -> "목"
                    6 -> "금"
                    else -> "토"
                }
        curSelectDateVO = DateVO(
            calendar.get(Calendar.YEAR).toString(),
            (calendar.get(Calendar.MONTH) + 1).toString(),
            calendar.get(Calendar.DATE).toString()
        )
        dayWeeks.value = date
        return curSelectDateVO
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
     * 17,30 -> 오후 5:30
     */
    fun getFilterSelectTime(hour: Int, min: Int): String {
        var strTime = ""
        var h = ""
        var m = ""
        if(hour > 12) {
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
        if(min.toString().length == 1) {
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
                                                placeList.add(requestPlusData.documents[i])
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
        selectCal: Calendar
    ) {
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

        val dataId = DBManager.getId(title, date, time, place, contents, this)

        if (alarmTime.trim().isNotEmpty()) {
            if (alarmTime != "0" && alarmTime != "") {
                selectCal.set(
                 Calendar.MINUTE,
                    selectCal.get(Calendar.MINUTE) - alarmTime.toInt()
                )
            }
            val year = date.split("-")[0]
            val month = date.split("-")[1].toInt()
            val day = date.split("-")[2].toInt()
            val hour = time.split(":")[0]
            val min = time.split(":")[1]
            val alarmCode = year.substring(2, 4) + month.toString() + day.toString() + hour + min
            createAlarm(
                context,
                alarmCode,
                title,
                "$year.$month.$day",
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
        selectCal: Calendar,
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
            if (alarmTime != "0" && alarmTime != "") {
                selectCal.set(
                   Calendar.MINUTE,
                    selectCal.get(Calendar.MINUTE) - alarmTime.toInt()
                )
            }
            val year = date.split(".")[0]
            val month = date.split(".")[1]
            val day = date.split(".")[2]
            val hour = time.split(":")[0]
            val min = time.split(":")[1]
            val alarmCode = year.substring(2, 4) + month + day + hour + min
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
        selectDateTime: Calendar
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyBroadCastReceiver::class.java)
        intent.putExtra("alarmCode", alarmCode)
        intent.putExtra("title", title)
        intent.putExtra("date", date)
        intent.putExtra("time", time)
        intent.putExtra("dataID", dataID)
        val pendingIntent = PendingIntent.getBroadcast(context, alarmCode.toInt(), intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
        val cursor = DBManager.select(sql, this)

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
        }

        if (alarmCode != "") {
            val intent = Intent(context, MyBroadCastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmCode.toInt(), intent, 0)
            alarmManager.cancel(pendingIntent)
        }

    }

    /**
     * 일정 목록 액티비티 데이터 세팅
     */
    fun setScheduleList(scheduleListAdapter: ScheduleListAdapter) {
        val scheduleList = arrayListOf<ScheduleDataVO>()
        val sql = "select * from calendar order by DATE(date) asc"
        val cursor = DBManager.select(sql, this)

        while (cursor.moveToNext()) {
            val scheduleDataVO: ScheduleDataVO
            if (cursor.getString(3) == "종일") {
                scheduleDataVO = ScheduleDataVO(
                    id = cursor.getInt(0),
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
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = getFilterSelectTime(hour.toInt(), min.toInt()),
                    place = cursor.getString(4),
                    viewType = 2
                )
            }
            scheduleList.add(scheduleDataVO)
        }
        scheduleListAdapter.scheduleList = scheduleList
        scheduleListAdapter.notifyDataSetChanged()
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
        return if(apiAvailability.isUserResolvableError(sCode)) {
            sCode
        } else {
            0
        }
    }

    /**
     * 데이터 요청 클래스 EXCUTE
     */
    fun requestCalendarData(mCredential: GoogleAccountCredential) {
        RequestTask(mCredential, this).execute()
    }

}