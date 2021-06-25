package com.example.schedulemanager

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.viewmodel.MyViewModel
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import java.io.IOException

/**
 * 비동기적으로 데이터 요청 처리
 */
class RequestGoogleCalendar(val context: Context, val viewModel: MyViewModel) :
    AsyncTask<Void, Void, Boolean>() {
    private val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
    private val jacksonFactory: JacksonFactory = JacksonFactory.getDefaultInstance()
    //구글캘린더 서비스객체
    private var mService: Calendar

    init {
        mService = Calendar.Builder(transport, jacksonFactory, viewModel.mCredential)
            .setApplicationName("Google Calendar API Android QuickStart")
            .build()
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        var id: String?
        var pageToken: String? = null
        do {
            var calendarList: CalendarList? = null
            try {
                calendarList = mService.calendarList().list().setPageToken(pageToken).execute()
            } catch (e: UserRecoverableAuthIOException) {
                return false
            } catch (e: IOException) {
                return false
            }

            val calendarItems = calendarList?.items
            calendarItems?.let {
                for (calendarListEntry in calendarItems) {
                    if (calendarListEntry.summary == "대한민국의 휴일") {
                        continue
                    }
                    id = calendarListEntry.id
                    val events = mService.events().list(id)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                    val items = events.items
                    for (event in items) {
                        val title = event.summary ?: "제목없음"
                        var date = ""
                        var time = ""
                        if (event.start.date != null) {
                            val eventDate = event.start.date.toString()
                            val year = eventDate.split("-")[0]
                            val month = eventDate.split("-")[1]
                            val day = eventDate.split("-")[2]
                            date = "$year-$month-$day"
                            time = "종일"
                        }
                        if (event.start.dateTime != null) {
                            val datetime = formatDate(event.start.dateTime)
                            date = datetime.split(" ")[0]
                            val year = date.split("-")[0]
                            val month = date.split("-")[1]
                            val day = date.split("-")[2]
                            date = "$year-$month-$day"
                            time = datetime.split(" ")[1]
                            time = time.split(":")[0] + ":" + time.split(":")[1]
                        }
                        val contents = event.description ?: ""

                        var place = ""
                        if (event.location != null) {
                            place = event.location.split(",")[0]
                        }

                        val dataId = DBManager.getId(title, date, time, place, contents, viewModel)
                        Log.e(title, id.toString())
                        if (dataId == -1) {
                            val sql =
                                "insert into calendar (title,date,time,place,contents) values('" +
                                        title + "','" + date + "','" + time + "','" + place + "','" + contents + "')"
                            DBManager.insert(sql, viewModel)
                        }
                    }
                }
            }
            pageToken = calendarList?.nextPageToken
        } while (pageToken != null)
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (result) {
            viewModel.setCalendarNotify()
            viewModel.setBottomListNotify()
        } else {
            Toast.makeText(context, "에러 발생!", Toast.LENGTH_SHORT).show()
            viewModel.mCredential.selectedAccountName = null
        }
    }

    /**
     * 구글에서 가져온 날짜 데이터 변환
     */
    private fun formatDate(dateTime: DateTime): String {
        val str = String.format("%s", dateTime)
        var data: String = ""
        for (i in str) {
            if (i == 'T') {
                data += " "
                continue
            } else if (i == '+' || i == '.') {
                break
            } else {
                data += i.toString()
            }
        }
        return data
    }

}