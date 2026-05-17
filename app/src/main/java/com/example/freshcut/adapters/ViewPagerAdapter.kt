package com.example.freshcut.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.freshcut.R
import com.example.freshcut.fragments.AppointmentsFragment
import com.example.freshcut.fragments.SettingsFragment
import com.example.freshcut.fragments.TodayAppointmentsFragment

// this class will be the View that connects the Fragments in  Manager Activity to tabs
class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity)
{
    private val fragments = listOf(
        AppointmentsFragment(),
        TodayAppointmentsFragment(),
        SettingsFragment()
    )

    private val titles = listOf(activity.getString(R.string.appointments), activity.getString(R.string.appo_today), activity.getString(
        R.string.settings))

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getTitle(position: Int): String = titles[position]

}