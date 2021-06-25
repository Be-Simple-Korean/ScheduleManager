package com.example.schedulemanager.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    var requestActivty: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { it ->
        Log.e("수행","2")
        if (it.resultCode == Activity.RESULT_OK) {
            schduleListAdapter.notifyDataSetChanged()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.setDBHelper(this)
        schduleListAdapter = ScheduleListAdapter(requestActivty)
        binding.rvScheduleList.adapter = schduleListAdapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.setScheduleList(schduleListAdapter)
    }
}