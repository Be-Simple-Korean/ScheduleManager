package com.example.schedulemanager.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.schedulemanager.CalendarFragment

class MyFragementStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    val FIRST_POSITION = Int.MAX_VALUE / 2



    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun createFragment(position: Int): Fragment {
            val calendarFragment= CalendarFragment()
            calendarFragment.pageIndex=position
            return calendarFragment
    }
}