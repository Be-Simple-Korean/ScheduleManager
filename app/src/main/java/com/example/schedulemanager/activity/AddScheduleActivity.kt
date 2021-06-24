package com.example.schedulemanager.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.R
import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.databinding.ActivityAddScheduleBinding
import com.example.schedulemanager.dialog.DeleteGuideDialog
import com.example.schedulemanager.dialog.SearchPlaceDialog
import com.example.schedulemanager.dialog.SetAlarmDialog
import com.example.schedulemanager.lisetener.OnDismissListener
import com.example.schedulemanager.viewmodel.MyViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

/**
 * 일정 추가 액티비티
 */
class AddScheduleActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val TIME_NOW = "일정"
        const val NO_INPUT_TIME = "0"
    }

    private lateinit var selectYear: String
    private lateinit var selectMonth: String
    private lateinit var selectDay: String
    private lateinit var binding: ActivityAddScheduleBinding
    private lateinit var viewModel: MyViewModel
    private var selectCal = Calendar.getInstance()
    private var isUpdate = false
    private var hour = 0
    private var min = 0
    private var selectDoucments = DocumentsVO()
    private var alarmTime = ""
    private var oldId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)

        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        min = calendar.get(Calendar.MINUTE)

        val intent = intent
        isUpdate = intent.getBooleanExtra("isUpdate", false)
        if (isUpdate) {
            val id = intent.getIntExtra("id", -1)
            val sql = "select id from calendar where id=$id"
            val cursor = DBManager.select(sql, viewModel)
            if (cursor.count > 0) {
                oldId = id
            }
            if (oldId != -1) {
                val sql = "select * from calendar where id=$oldId"
                val cursor = DBManager.select(sql, viewModel)
                while (cursor.moveToNext()) {
                    binding.etScheduleAddTitle.setText(cursor.getString(1))
                    val date = cursor.getString(2)
                    selectCal.set(
                        date.split("-")[0].toInt(),
                        date.split("-")[1].toInt() - 1,
                        date.split("-")[2].toInt()
                    )

                    binding.tvScheduleSelectedDate.text = cursor.getString(2)
                    if (cursor.getString(3).equals("종일")) {
                        binding.tvScheduleSelectedTime.text = cursor.getString(3)
                        hour = -1
                        min = -1
                        selectCal.set(Calendar.HOUR_OF_DAY, 0)
                        selectCal.set(Calendar.MINUTE, 0)
                        selectCal.set(Calendar.SECOND, 0)
                    } else {
                        val time = cursor.getString(3)
                        hour = time.split(":")[0].toInt()
                        min = time.split(":")[1].toInt()
                        selectCal.set(Calendar.HOUR_OF_DAY, hour)
                        selectCal.set(Calendar.MINUTE, min)
                        selectCal.set(Calendar.SECOND, 0)
                        binding.tvScheduleSelectedTime.text = viewModel.getFilterSelectTime(
                            time.split(":")[0].toInt(),
                            time.split(":")[1].toInt()
                        )
                    }

                    binding.tvScheduleSelectedPlace.text = cursor.getString(4)
                    if (cursor.getString(5).trim().isNotEmpty()) {
                        binding.llMap.visibility = View.VISIBLE
                        val mapFragment =
                            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

                        selectDoucments = DocumentsVO(
                            place_name = cursor.getString(4),
                            x = cursor.getString(6),
                            y = cursor.getString(5)
                        )
                        mapFragment.getMapAsync(this@AddScheduleActivity)
                    }

                    binding.etScheduleAddContents.setText(cursor.getString(7))
                    if (cursor.getString(8).trim().isNotEmpty()) {
                        alarmTime = cursor.getString(8)
                        if (alarmTime == NO_INPUT_TIME) {
                            binding.tvScheduleSelectedAlarmTime.text = "일정 시작 시간"
                        } else {
                            binding.tvScheduleSelectedAlarmTime.text =
                                (cursor.getString(8) + " 분 전")
                        }
                    } else {
                        binding.tvScheduleSelectedAlarmTime.text = "없음"
                    }

                    binding.llDelete.visibility = View.VISIBLE
                    val curDateTime = Calendar.getInstance()
                    curDateTime.set(Calendar.SECOND, 0)
                    if (selectCal.before(curDateTime)) {
                        binding.ibScheduleCheck.visibility = View.GONE
                        binding.btnScheduleSelectDate.visibility = View.GONE
                        binding.btnScheduleSelectTime.visibility = View.GONE
                        binding.btnSelectPlace.visibility = View.GONE
                        binding.btnScheduleSelectAlarmTime.visibility = View.GONE
                        binding.etScheduleAddTitle.isEnabled = false
                        binding.etScheduleAddTitle.setTextColor(Color.BLACK)
                        binding.etScheduleAddContents.isEnabled = false
                        binding.etScheduleAddContents.setTextColor(Color.BLACK)
                    }
                }
            }
        } else {
            //키보드 나타내기
            binding.etScheduleAddTitle.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }

        //닫기 이미지버튼 클릭
        binding.ibScheduleClose.setOnClickListener {
            finish()
        }

        //날짜 선택 버튼 클릭
        binding.btnScheduleSelectDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                dateSetlistener,
                selectCal.get(Calendar.YEAR),
                selectCal.get(Calendar.MONTH),
                selectCal.get(Calendar.DATE)
            )
            datePickerDialog.show()
        }

        //시간 선택 버튼 클릭이벤트
        binding.btnScheduleSelectTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this, timePickerListener,
                hour,
                min, false
            )
            timePickerDialog.show()
        }

        // 위치 선택 버튼 클릭이벤트
        binding.btnSelectPlace.setOnClickListener {
            val searchPlaceDialog = SearchPlaceDialog(this, viewModel)
            searchPlaceDialog.onDimissListener = onDimissListener
            searchPlaceDialog.show()
        }

        //알람 선택버튼 클릭
        binding.btnScheduleSelectAlarmTime.setOnClickListener {
            var time = binding.tvScheduleSelectedAlarmTime.text.toString()
            time = if (time.contains(TIME_NOW)) NO_INPUT_TIME else time.split(" ")[0]
            val setAlarmDialog =
                SetAlarmDialog(this, time)
            setAlarmDialog.onDismissListener = onDimissListener
            setAlarmDialog.show()
        }

        //체크 이미지버튼 클릭
        binding.ibScheduleCheck.setOnClickListener {
            val title: String = binding.etScheduleAddTitle.text.toString()
            val place = binding.tvScheduleSelectedPlace.text.toString()
            val contents = binding.etScheduleAddContents.text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val date = selectCal.time.toString()
            if (date.isEmpty()) {
                Toast.makeText(this, "날짜 데이터를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.tvScheduleSelectedTime.text.trim().isEmpty()) {
                Toast.makeText(this, "시간을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val curDateTime = Calendar.getInstance()
            curDateTime.set(Calendar.SECOND, 0)
            if (selectCal.before(curDateTime)) {
                Toast.makeText(this, "현재 시간 이후로 설정해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isUpdate) {
                viewModel.update(
                    this,
                    title,
                    "$hour:$min",
                    place,
                    selectDoucments.y,
                    selectDoucments.x,
                    contents,
                    alarmTime,
                    selectCal, oldId
                )
                finish()
            } else {
                viewModel.addSchedule(
                    this,
                    title,
                    "$hour:$min",
                    place,
                    selectDoucments.y,
                    selectDoucments.x,
                    contents,
                    alarmTime,
                    selectCal
                )
            }
            finish()
        }

        //삭제 이미지 클릭 이벤트
        binding.llDelete.setOnClickListener {
            val deleteGuideDialog = DeleteGuideDialog(this, oldId, viewModel)
            deleteGuideDialog.onDismissListener = onDimissListener
            deleteGuideDialog.show()
        }
    }

    //시간선택 다이얼로그 리스너
    private val timePickerListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        hour = hourOfDay // H
        min = minute // M
        selectCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        selectCal.set(Calendar.MINUTE, minute)
        selectCal.set(Calendar.SECOND, 0)
        binding.tvScheduleSelectedTime.text = viewModel.getFilterSelectTime(hour, min)
    }

    //날짜 선택 다이얼로그 리스너
    private val dateSetlistener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            selectYear = year.toString()
            selectMonth = (monthOfYear + 1).toString()
            selectDay = dayOfMonth.toString()
            selectCal.set(selectYear.toInt(), selectMonth.toInt() - 1, selectDay.toInt())
            binding.tvScheduleSelectedDate.text = "$selectYear.$selectMonth.$selectDay"
        }

    var onDimissListener = object : OnDismissListener {
        override fun onDismissFromAlarm(dialog: SetAlarmDialog, time: String) {
            dialog.dismiss()
            if (time == NO_INPUT_TIME) {
                binding.tvScheduleSelectedAlarmTime.text = "일정 시작 시간"
            } else {
                binding.tvScheduleSelectedAlarmTime.text = "$time 분 전"
            }
            alarmTime = time
        }

        override fun onDismissListener(
            searchPlaceDialog: SearchPlaceDialog,
            documentsVO: DocumentsVO
        ) {
            searchPlaceDialog.dismiss()
            binding.tvScheduleSelectedPlace.text = documentsVO.place_name
            if (!documentsVO.address_name.equals(MyViewModel.FIRST_VALUE_ADDRESS)) {
                binding.llMap.visibility = View.VISIBLE
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

                selectDoucments = documentsVO
                mapFragment.getMapAsync(this@AddScheduleActivity)
            }
        }

        override fun onDismissListener(dialog: DeleteGuideDialog) {
            dialog.dismiss()
            finish()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        val map = googleMap

        selectDoucments.let {
            val located = LatLng(it.y.toDouble(), it.x.toDouble())

            val markerOptions = MarkerOptions()
            markerOptions.position(located)
            markerOptions.title(it.place_name)
//            markerOptions.snippet(it.address_name)
            map.addMarker(markerOptions)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(located, 15f))
        }
    }

}