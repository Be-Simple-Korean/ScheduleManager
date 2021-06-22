package com.example.schedulemanager

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import java.io.IOException

class MyViewModel : ViewModel() {

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
        RequestTask(mCredential).execute()
    }

}