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

enum class MonthType {
    PREVIOUS, NOW, NEXT
}

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
        Log.e("postion set",pageIndex.toString())
        viewModel.pageIndex.value=pageIndex
        viewModel.setDate()
        return binding.root
    }

    val onClickListener = object : OnClickListener {
        override fun onCalendarItemClickListener(
            dateVO: DateVO,
            week: String,
            monthType: MonthType
        ) {
            when (monthType) {
                MonthType.PREVIOUS -> {
                    Log.e("before",viewModel.pageIndex.value.toString())
                    viewModel.pageIndex.value=viewModel.pageIndex.value!!.minus(1)
                    Log.e("after",viewModel.pageIndex.value.toString())
                    viewModel.setDate()
                }
                MonthType.NOW -> {
                    viewModel.setSelectDay(dateVO, week)
                    viewModel.setBottomList(dateVO)
                }
                MonthType.NEXT -> {
                    Log.e("before",viewModel.pageIndex.value.toString())
                    viewModel.pageIndex.value= viewModel.pageIndex.value!!.plus(1)
                    Log.e("after",viewModel.pageIndex.value.toString())
                    viewModel.setDate()
                }
            }
            viewModel.setSelectDay(dateVO, week)
            viewModel.setBottomList(dateVO)

        }

        override fun onClickListener(v: View, documentsVO: DocumentsVO) {

        }


    }

}