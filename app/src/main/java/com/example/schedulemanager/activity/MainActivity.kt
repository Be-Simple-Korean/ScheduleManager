package com.example.schedulemanager.activity

import android.accounts.AccountManager
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.schedulemanager.viewmodel.MyViewModel
import com.example.schedulemanager.R
import com.example.schedulemanager.adapter.MyFragementStateAdapter
import com.example.schedulemanager.adapter.ScheduleListAdapter
import com.example.schedulemanager.databinding.ActivityMainBinding
import com.example.schedulemanager.dialog.SearchPlaceDialog
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1000
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MyViewModel
    var requestActivty: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { it ->
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.setCalendarNotify()
            viewModel.setBottomListNotify()
        }
    }
    private val scheduleListAdapter = ScheduleListAdapter(requestActivty)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)

        // Google Calendar API ???????????? ?????? ????????? ?????? ?????????
        viewModel.mCredential = GoogleAccountCredential.usingOAuth2(
            this, listOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())

        createNotificationChannel()

        val myFragementStateAdapter = MyFragementStateAdapter(this)
        binding.viewPager2.adapter = myFragementStateAdapter
        myFragementStateAdapter.apply {
            binding.viewPager2.setCurrentItem(this.FIRST_POSITION, false)
        }

        val todayDateVO = viewModel.setSelectToday()
        binding.rvMainSchedule.adapter = scheduleListAdapter

        viewModel.setBottomList(todayDateVO)
//        viewModel.pageIndex.observe(this, androidx.lifecycle.Observer {
//            Log.e("observe",it.toString())
//            binding.viewPager2.setCurrentItem(it,false)
//            Log.e("cur viewpager page1",binding.viewPager2.currentItem.toString())
//            viewModel.setDate(pageIndex)
//        })

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                Log.e("page in change",position.toString())
//                Log.e("cur viewpager page2",binding.viewPager2.currentItem.toString())
//                binding.viewPager2.setCurrentItem(position,false)
//                viewModel.pageIndex.value=position
                viewModel.setDate(position)
            }
        })

        //????????? ???????????? ??????
        binding.fabMainAddSchedule.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this,
                AddScheduleActivity::class.java
            )
            requestActivty.launch(intent)
//            startActivity(intent)
        })

        //????????? ????????????
        binding.ibMainAllList.setOnClickListener {
            val intent = Intent(this, ScheduleListActivity::class.java)
            requestActivty.launch(intent)
        }

        //????????? ??????????????? ??????
        binding.ibMainSync.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.GET_ACCOUNTS
                ),
                REQUEST_CODE
            )
        }

        //?????? ?????? ?????????
        viewModel.yearMonth.observe(this, androidx.lifecycle.Observer {
            binding.tvCalendarYearMonth.text = it
        })

        //??????-??????-???????????????
        viewModel.dayWeeks.observe(this, androidx.lifecycle.Observer {
            binding.tvMainSelectDay.text = it
        })

        //?????? ??????????????????
        viewModel.mainSchduleList.observe(this, androidx.lifecycle.Observer {
            scheduleListAdapter.scheduleList = it
            scheduleListAdapter.notifyDataSetChanged()
        })

//        val db: SQLiteDatabase = viewModel.getDatabase(DataBaseType.READ)
//        val cursor = db.rawQuery(
//            "select * from calendar ",
//            null
//        )
//        while (cursor.moveToNext()) {
//            Log.e(
//                "in DB",
//                cursor.getInt(0).toString() + "/" + cursor.getString(1)
//                    .toString() + "/" + cursor.getString(2) +
//                        "/" + cursor.getString(3) + "/" + cursor.getString(4) +
//                        "/" + cursor.getString(5) + "/" + cursor.getString(6) +
//                        "/" + cursor.getString(7) + "/" + cursor.getString(8)
//            )
//        }
    }




    /**
     * ???????????? ??????
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
     * ?????? api ????????? ?????? ?????? ??????
     */
    private fun getGoogleConditionResult() {
        if (!viewModel.isDeviceOnline(this)) {
            Toast.makeText(this, "???????????? ??????????????????!", Toast.LENGTH_SHORT).show()
        } else if (!viewModel.isGooglePlayServiceAvailable(this)) { //???????????????????????? ?????? ??????
            val sCode = viewModel.updateServiceGuide(this)
            if (sCode != 0) {
                showErrorDialog(sCode)
            } else {
                getGoogleConditionResult()
            }
        } else if (viewModel.mCredential.selectedAccountName == null) {
            // ???????????? ?????? ????????? ????????? ??? ?????? ?????????????????? ????????????.
            requestAccountActivity.launch(viewModel.mCredential.newChooseAccountIntent())
        } else {
            viewModel.requestCalendarData(this)
        }
    }

    /**
     * ???????????????????????? ?????? ????????????
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

    val requestAccountActivity:ActivityResultLauncher<Intent> =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.extras != null) {
            viewModel.mCredential.selectedAccountName =
                it.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            getGoogleConditionResult()
        }
    }


}