package com.example.schedulemanager

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

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
    fun isGooglePlayServiceAvailable(context: Context):Boolean{
        val apiAvailability= GoogleApiAvailability.getInstance()
        val sCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return sCode== ConnectionResult.SUCCESS
    }

    fun updateServiceGuide(context: Context):Int{
        val apiAvailability = GoogleApiAvailability.getInstance()
        val sCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if( apiAvailability.isUserResolvableError(sCode)){
            return sCode
        }else{
            return 0
        }
    }
}