package com.example.schedulemanager.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.schedulemanager.database.DBManager
import com.example.schedulemanager.databinding.DialogDeleteGuideBinding
import com.example.schedulemanager.lisetener.OnDeleteDialogCheckListener
import com.example.schedulemanager.viewmodel.MyViewModel

/**
 * 일정 삭제 다이얼로그
 */
class DeleteGuideDialog(context: Context, val id: Int,val time:String, val viewModel: MyViewModel) :
    Dialog(context) {

    private lateinit var binding: DialogDeleteGuideBinding
    lateinit var onDeleteDialogCheckListener: OnDeleteDialogCheckListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDeleteGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.let {
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        //취소 클릭
        binding.flDeleteGuilde.setOnClickListener {
            dismiss()
        }

        //확인 클릭
        binding.tvDeleteGuideCheck.setOnClickListener {
            if(time!="없음"){
                viewModel.cancelAlarm(context, id)
            }
            val sql = "delete from calendar where id =$id"
            DBManager.delete(sql, viewModel)
            Log.e("수행","0")
            onDeleteDialogCheckListener.onDeleteDialogCheckListener()
            dismiss()

        }

    }
}