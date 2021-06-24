package com.example.schedulemanager.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.example.schedulemanager.databinding.DialogInputAlarmTimeBinding
import com.example.schedulemanager.lisetener.OnDismissListener

/**
 * 알람 설정 다이얼로그
 */
class SetAlarmDialog(context: Context, private val data: String) : Dialog(context) {

    lateinit var onDismissListener: OnDismissListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DialogInputAlarmTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        window?.let {
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        if (data != "") {
            binding.etSetAlarmTime.setText(data)
        }

        //바깥 영역 클릭 이벤트
        binding.flInputAlarmDialog.setOnClickListener {
            dismiss()
        }

        //취소 텍스트 클릭 이벤트
        binding.tvAlarmCancel.setOnClickListener {
            dismiss()
        }

        binding.llDialogAlarm.setOnClickListener {}

        //완료 텍스트 클릭이벤트
        binding.tvAlarmSet.setOnClickListener{
            imm.hideSoftInputFromWindow(binding.etSetAlarmTime.windowToken, 0)
            if (binding.etSetAlarmTime.text.trim().isEmpty()) {
                dismiss()
            } else {
                val time = binding.etSetAlarmTime.text.toString().toInt().toString()
                onDismissListener.onDismissFromAlarm(this, time)
            }
        }
    }
}