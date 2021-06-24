package com.example.schedulemanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.adapter.CalendarAdapter
import com.example.schedulemanager.data.DateVO
import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.databinding.FragmentCalendarBinding
import com.example.schedulemanager.lisetener.OnClickListener
import com.example.schedulemanager.viewmodel.MyViewModel

/**
 * 달력 프래그먼트
 */
class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var viewModel: MyViewModel
    var pageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.bind(
            inflater.inflate(
                R.layout.fragment_calendar,
                container,
                false
            )
        )
        val calendarAdapter = CalendarAdapter()
        calendarAdapter.onClickListener = onClickListener
        calendarAdapter.viewModel = viewModel
        binding.rvCalendar.adapter = calendarAdapter
        viewModel.calendarAdapter = calendarAdapter
        calendarAdapter.notifyDataSetChanged()
        viewModel.setDate(pageIndex)
        return binding.root
    }

    val onClickListener = object : OnClickListener {
        override fun onCalendarItemClickListener(dateVO: DateVO, week: String) {
            viewModel.setSelectDay(dateVO, week)
            viewModel.setBottomList(dateVO)
        }

        override fun onClickListener(v: View, documentsVO: DocumentsVO) {

        }


    }

}