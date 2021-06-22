package com.example.schedulemanager

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.databinding.ActivityMainBinding
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 1000
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_ACCOUNT_PICKER = 1003
    }

    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MyViewModel
    lateinit var mCredential: GoogleAccountCredential
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            this, Arrays.asList(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())



        binding.ibMainSync.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.GET_ACCOUNTS
                ),
                REQUEST_CODE
            )
        }
    }

    /**
     * 구글 api 사용을 위한 조건 검사
     */
    private fun getGoogleConditionResult() {
        if (!viewModel.isDeviceOnline(this)) {
            Toast.makeText(this, "인터넷 연결좀", Toast.LENGTH_SHORT).show()
        } else if (!viewModel.isGooglePlayServiceAvailable(this)) {
            val sCode = viewModel.updateServiceGuide(this)
            if (sCode != 0) {
                showErrorDialog(sCode)
            } else {
                getGoogleConditionResult()
            }
        } else if (mCredential.selectedAccountName == null) {
            // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
            startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
            )
        } else {
            viewModel.requestCalendarData(mCredential)
        }
    }

    /**
     * 구글플레이서비스 업데이트 수행 대화상자
     */
    private fun showErrorDialog(sCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this, sCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGoogleConditionResult()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER ->
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    mCredential.selectedAccountName =
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    getGoogleConditionResult()
                }
            else ->
                if (resultCode == Activity.RESULT_OK) {
                    getGoogleConditionResult()
                }
        }

    }

}