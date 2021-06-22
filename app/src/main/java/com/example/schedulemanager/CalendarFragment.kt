package com.example.schedulemanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.calendarapp.adapter.CalendarAdapter
import com.example.calendarapp.databinding.FragmentCalendarBinding
import com.example.calendarapp.lisetener.OnClickListener
import com.example.calendarapp.viewmodel.MyViewModel
import com.example.calendarapp.DocumentsVO
import com.example.calendarapp.data.DateVO
import com.example.schedulemanager.viewmodel.MyViewModel


class CalendarFragment : Fragment() {

    lateinit var binding: FragmentCalendarBinding
    lateinit var viewModel: MyViewModel

    var pageIndex=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("수행","!!")
        binding= FragmentCalendarBinding.bind(inflater.inflate(R.layout.fragment_calendar, container, false))
        val calendarAdapter=CalendarAdapter()
        calendarAdapter.onClickListener=onClickListener
        calendarAdapter.viewModel=viewModel
        binding.rvCalendar.adapter=calendarAdapter
        viewModel.calendarAdapter=calendarAdapter
        calendarAdapter.notifyDataSetChanged()
        viewModel.setDate(pageIndex)
        return binding.root
    }

    val onClickListener=object :OnClickListener{
        override fun onCalendarItemClickListener(dateVO: DateVO, week: String) {
            val selectDay = "${dateVO.day} $week"
            viewModel.setSelectDay(selectDay)
            viewModel.setBottomList(dateVO)
        }

        override fun onClickListener(v: View, documentsVO: DocumentsVO) {

        }


    }

}