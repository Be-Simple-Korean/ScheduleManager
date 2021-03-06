package com.example.schedulemanager.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.schedulemanager.CalendarFragment
import com.example.schedulemanager.viewmodel.MyViewModel

/**
 * 뷰페이저 어댑터
 */
class MyFragementStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    val FIRST_POSITION = Int.MAX_VALUE / 2

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun createFragment(position: Int): Fragment {
            val calendarFragment= CalendarFragment()
//            viewModel.pageIndex=position
            Log.e("postion in create",position.toString())
            calendarFragment.pageIndex=position
            return calendarFragment
    }
}