package com.example.schedulemanager.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulemanager.databinding.LayoutScheduleInfoBinding

/**
 * 알림 상세 페이지
 */
class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutScheduleInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = getIntent()
        binding.tvInfoTitle.text = intent.getStringExtra("title")
        binding.tvInfoContents.text = intent.getStringExtra("contents")
        binding.tvInfoDate.text = intent.getStringExtra("date")
        binding.tvInfoTime.text = intent.getStringExtra("time")
        binding.tvInfoPlace.text = intent.getStringExtra("place")

        binding.flInfo.setOnClickListener(View.OnClickListener {
            finish()
        })
        binding.tvInfoContinue.setOnClickListener(View.OnClickListener {
            finish()
        })
    }
}