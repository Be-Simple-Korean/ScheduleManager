package com.example.schedulemanager

import android.os.AsyncTask
import android.util.Log
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
    class RequestTask(mCredential: GoogleAccountCredential) : AsyncTask<Void, Void, Unit>() {
        val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        var mService: Calendar

        init {
            mService = Calendar.Builder(transport, jacksonFactory, mCredential)
                .setApplicationName("Google Calendar API Android QuickStart")
                .build()
        }

        override fun doInBackground(vararg params: Void?): Unit {
            var id: String? = null
            var pageToken: String? = null
            Log.e("수행", "!!12")
            do {
                var calendarList: CalendarList? = null
                try {
                    calendarList = mService.calendarList().list().setPageToken(pageToken).execute()
                } catch (e: UserRecoverableAuthIOException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                var items = calendarList?.items
                Log.e("items size", items?.size.toString())
                items?.let {
                    for (calendarListEntry in items) {
                        Log.e("summary", calendarListEntry.getSummary())
                        if (calendarListEntry.getSummary().equals("대한민국의 휴일")) {
                            continue
                        }
                        id = calendarListEntry.id
                        var events = mService.events().list(id)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()
                        var items = events.items
                        for (event in items) {

                            val title = event.summary ?: "제목없음"
                            var date = ""
                            var time = ""
                            if (event.start.date != null) {
                                Log.e(title, event.start.date.toString())
                                var eventDate = event.start.date.toString()
                                var year = eventDate.split("-")[0].toInt()
                                var month = eventDate.split("-")[1].toInt()
                                var day = eventDate.split("-")[2].toInt()
                                date =
                                    year.toString() + "." + month.toString() + "." + day.toString()
                                time = "종일"
                            }
                            if (event.start.dateTime != null) {
                                var datetime = formatDate(event.start.dateTime)
                                date = datetime.split(" ")[0]
                                var year = date.split("-")[0].toInt()
                                var month = date.split("-")[1].toInt()
                                var day = date.split("-")[2].toInt()
                                date =
                                    year.toString() + "." + month.toString() + "." + day.toString()
                                time = datetime.split(" ")[1]
                                time = time.split(":")[0] + ":" + time.split(":")[1]
                            }
                            val contents = event.description ?: ""

                            var place = ""
                            if (event.location != null) {
                                place = event.location.split(",")[0]
                            }
                            Log.e(
                                "request data",
                                title + "/" + date + "/" + time + "/" + contents + "/" + place
                            )
//TODO
//                            val id = DBManager.getId(title, date, time, place, contents, viewModel)
//                            Log.e(title, id.toString())
//                            if (id == -1) {
//                                val sql =
//                                    "insert into calendar (title,date,time,place,contents) values('" +
//                                            title + "','" + date + "','" + time + "','" + place + "','" + contents + "')"
//                                DBManager.insert(sql, viewModel)
//                            }
                        }
                    }
                }
                pageToken = calendarList?.nextPageToken
            } while (pageToken != null)
        }

        /**
         * 구글에서 가져온 날짜 데이터 변환
         */
        fun formatDate(dateTime: DateTime): String {
            var str = String.format("%s", dateTime)
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