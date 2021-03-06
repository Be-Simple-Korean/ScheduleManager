package com.example.schedulemanager.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulemanager.adapter.PlaceAdapter
import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.databinding.DialogSearchPlaceBinding
import com.example.schedulemanager.lisetener.OnPlaceDialogItemSelectedListener
import com.example.schedulemanager.lisetener.OnSearchPlaceDialogItemClickListener
import com.example.schedulemanager.viewmodel.MyViewModel

/**
 * 위치 검색 다이얼로그
 */
class SearchPlaceDialog(context: Context, val viewModel: MyViewModel) : Dialog(context) {

    companion object {
        const val NO_RESULT = 0
    }

    lateinit var binding: DialogSearchPlaceBinding
    lateinit var onPlaceDialogItemSelectedListener: OnPlaceDialogItemSelectedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSearchPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.let {
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val placeAdater = PlaceAdapter()
        placeAdater.onSearchPlaceDialogItemClickListener = onSearchPlaceDialogItemClickListener
        val layoutManager = LinearLayoutManager(context)
        binding.rvSearchPlace.layoutManager = layoutManager
        binding.rvSearchPlace.adapter = placeAdater
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //회색바탕(바깥) 클릭 이벤트
        binding.flSearchPlace.setOnClickListener {
            dismiss()
        }

        binding.etSearchPlaceKeyword.setOnEditorActionListener { v, actionId, event ->
            inputMethodManager.hideSoftInputFromWindow(binding.etSearchPlaceKeyword.windowToken, 0)
            val query = binding.etSearchPlaceKeyword.text.toString()
            if (query.trim().length == NO_RESULT) {
                Toast.makeText(context, "장소를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.requestPlaceData(context, query, placeAdater)
            }
            true
        }

    }

    var onSearchPlaceDialogItemClickListener = object : OnSearchPlaceDialogItemClickListener {
        override fun onPlaceDialogItemClickListener(v: View, documentsVO: DocumentsVO) {
            dismiss()
            onPlaceDialogItemSelectedListener.onPlaceDialogItemSelectedListener(documentsVO)
        }
    }
}