package com.example.schedulemanager.activity

import android.accounts.AccountManager
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.schedulemanager.viewmodel.MyViewModel
import com.example.schedulemanager.R
import com.example.schedulemanager.adapter.MyFragementStateAdapter
import com.example.schedulemanager.adapter.SchduleListAdapter
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.data.ScheduleDataVO
import com.example.schedulemanager.databinding.ActivityMainBinding
import com.example.schedulemanager.viewmodel.DataBaseType
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import java.util.*

class MainActivity : AppCompatActivity() {
    //      데이터 추가시 NOTIFY 확인
    //      알림 클릭시 액티비티로 이동 - CALENDARVIEW 참조p[
    companion object {
        const val REQUEST_CODE = 1000
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_ACCOUNT_PICKER = 1003
    }

    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MyViewModel
    lateinit var mCredential: GoogleAccountCredential
    val schduleListAdapter = SchduleListAdapter()

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

        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)
        viewModel.mainActivity = this
        createNotificationChannel()
        val myFragementStateAdapter = MyFragementStateAdapter(this)
        binding.viewPager2.adapter = myFragementStateAdapter
        myFragementStateAdapter.apply {
            binding.viewPager2.setCurrentItem(this.FIRST_POSITION, false)
        }
        viewModel.setSelectToday()
        binding.rvMainSchedule.adapter = schduleListAdapter
        val calendar = Calendar.getInstance()
        viewModel.setBottomList(
            DateVO(
                calendar.get(Calendar.YEAR).toString(),
                (calendar.get(Calendar.MONTH) + 1).toString(),
                calendar.get(Calendar.DATE).toString()
            )
        )

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setDate(position)
            }
        })

        //플로팅 액션버튼 클릭
        binding.fabMainAddSchedule.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this,
                AddScheduleActivity::class.java
            )
            startActivity(intent)
        })

        //리스트 클릭버튼
        binding.ibMainAllList.setOnClickListener {
            val intent = Intent(this, ScheduleListActivity::class.java)
            startActivity(intent)
        }

        binding.ibMainSync.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.GET_ACCOUNTS
                ),
                REQUEST_CODE
            )
        }

        val db: SQLiteDatabase = viewModel.getDatabase(DataBaseType.READ)
        val cursor = db.rawQuery(
            "select * from calendar",
            null
        )
        while (cursor.moveToNext()) {
            Log.e(
                "in DB",
                cursor.getInt(0).toString() + "/" + cursor.getString(1)
                    .toString() + "/" + cursor.getString(2) +
                        "/" + cursor.getString(3) + "/" + cursor.getString(4) +
                        "/" + cursor.getString(5) + "/" + cursor.getString(6) +
                        "/" + cursor.getString(7) + "/" + cursor.getString(8)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setCalendarNotify()
        viewModel.setBottomListNotify()
    }

    /**
     * 상단의 날짜설정
     */
    fun setDate(datetime: String) {
        binding.tvCalendarYearMonth.setText(datetime)
    }

    /**
     * 하단의 날짜 설정
     */
    fun setSelectDay(selectDay: String) {
        binding.tvMainSelectDay.text = selectDay
    }

    /**
     * 하단의 리스트 데이터 세팅
     */
    fun setBottomList(scheduleList: ArrayList<ScheduleDataVO>) {
        schduleListAdapter.scheduleList = scheduleList
        schduleListAdapter.notifyDataSetChanged()
    }

    /**
     * 알림채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.noti_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.noti_id), name, importance)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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