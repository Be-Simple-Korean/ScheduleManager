package com.example.schedulemanager.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.adapter.SchduleListAdapter
import com.example.schedulemanager.databinding.ActivityScheduleListBinding
import com.example.schedulemanager.viewmodel.MyViewModel


/**
 * 일정 목록을 보여주는 액티비티
 */
class ScheduleListActivity : AppCompatActivity() {

    lateinit var binding: ActivityScheduleListBinding
    lateinit var viewModel: MyViewModel
    lateinit var schduleListAdapter: SchduleListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)
        schduleListAdapter = SchduleListAdapter()
        binding.rvScheduleList.adapter = schduleListAdapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.setScheduleList(schduleListAdapter)
    }
}