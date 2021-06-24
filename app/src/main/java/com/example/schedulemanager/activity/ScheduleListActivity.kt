package com.example.schedulemanager.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.adapter.ScheduleListAdapter
import com.example.schedulemanager.databinding.ActivityScheduleListBinding
import com.example.schedulemanager.viewmodel.MyViewModel


/**
 * 일정 목록을 보여주는 액티비티
 */
class ScheduleListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleListBinding
    private lateinit var viewModel: MyViewModel
    private  lateinit var schduleListAdapter: ScheduleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)
        schduleListAdapter = ScheduleListAdapter()
        binding.rvScheduleList.adapter = schduleListAdapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.setScheduleList(schduleListAdapter)
    }
}