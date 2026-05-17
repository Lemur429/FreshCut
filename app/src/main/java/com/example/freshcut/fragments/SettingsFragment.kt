package com.example.freshcut.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.R
import com.example.freshcut.adapters.HolidayCalendarAdapter
import com.example.freshcut.objects.StoreSettings
import com.example.freshcut.utilities.DatabaseManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class SettingsFragment : Fragment() {
    private lateinit var settings : StoreSettings

    private var currentMonthDate = LocalDate.now()
    private lateinit var holidayCalendarAdapter: HolidayCalendarAdapter
    private val blockedDatesSet = mutableSetOf<String>()

    // -- views
    private lateinit var openingHourDropdown :  AutoCompleteTextView
    private lateinit var closingHourDropdown :  AutoCompleteTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openingHourDropdown = view.findViewById<AutoCompleteTextView>(R.id.opening_hour)
        closingHourDropdown = view.findViewById<AutoCompleteTextView>(R.id.closing_hour)
        setupTimeDropdowns(view)

        DatabaseManager.getStoreSettings { fetchedSettings ->
            this.settings = fetchedSettings
            blockedDatesSet.addAll(settings.blockedDates)
            if (fetchedSettings != null && isAdded) {
                updateDropdownDefaults(view)
                setupAdminCalendar(view)
                setupWorkingDays(view)
            }
        }

        view.findViewById<Button>(R.id.btn_update_holidays).setOnClickListener {
            settings.blockedDates= blockedDatesSet.toList()


            val newWorkingDays = listOf(
                view.findViewById<CheckBox>(R.id.chkSunday).isChecked,  // 0
                view.findViewById<CheckBox>(R.id.chkMonday).isChecked,  // 1
                view.findViewById<CheckBox>(R.id.chkTuesday).isChecked, // 2
                view.findViewById<CheckBox>(R.id.chkWednesday).isChecked,// 3
                view.findViewById<CheckBox>(R.id.chkThursday).isChecked,// 4
                view.findViewById<CheckBox>(R.id.chkFriday).isChecked,// 5
                view.findViewById<CheckBox>(R.id.chkSaturday).isChecked  // 6

            )

            settings.workingDays=newWorkingDays
            DatabaseManager.updateStoreSettings(settings,{count ->
                Toast.makeText(context, "${count} appointments have been canceled", Toast.LENGTH_SHORT).show()
            },{e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()

            })
        }

        view.findViewById<Button>(R.id.btn_update_work_hours).setOnClickListener {
            settings.closingTime = closingHourDropdown.text.toString()
            settings.openingTime = openingHourDropdown.text.toString()

            DatabaseManager.updateStoreSettings(settings,{count ->
                Toast.makeText(context, "${count} appointments have been canceled", Toast.LENGTH_SHORT).show()
            },{e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()

            })
        }

    }
    private fun setupTimeDropdowns(view: View) {
        val hoursList = (0..23).map { hour ->
            String.format("%02d:00", hour)
        }
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_dropdown_item_1line, hoursList)

        openingHourDropdown.setAdapter(adapter)
        closingHourDropdown.setAdapter(adapter)

    }
    private fun updateDropdownDefaults(view: View) {
        openingHourDropdown.setText(settings.openingTime, false)
        closingHourDropdown.setText(settings.closingTime, false)
    }



    private fun setupAdminCalendar(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.adminCalendarRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        holidayCalendarAdapter = HolidayCalendarAdapter(emptyList(), blockedDatesSet)
        recyclerView.adapter = holidayCalendarAdapter

        view.findViewById<Button>(R.id.btnAdminPrevMonth).setOnClickListener {
            currentMonthDate = currentMonthDate.minusMonths(1)
            refreshCalendar(view)
        }
        view.findViewById<Button>(R.id.btnAdminNextMonth).setOnClickListener {
            currentMonthDate = currentMonthDate.plusMonths(1)
            refreshCalendar(view)
        }

        refreshCalendar(view)
    }

    private fun refreshCalendar(view: View) {
        val tvMonthYear = view.findViewById<TextView>(R.id.tvAdminMonthYear)
        tvMonthYear.text = currentMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        val daysInMonth = generateMonthGrid(currentMonthDate.year, currentMonthDate.monthValue)
        holidayCalendarAdapter.updateDays(daysInMonth)
    }

    private fun generateMonthGrid(year: Int, month: Int): List<LocalDate?> {
        val firstDay = LocalDate.of(year, month, 1)
        val dayOfWeek = firstDay.dayOfWeek.value
        val grid = mutableListOf<LocalDate?>()

        for (i in 1 until dayOfWeek) grid.add(null)
        for (i in 1..firstDay.lengthOfMonth()) grid.add(LocalDate.of(year, month, i))

        return grid
    }
    private fun setupWorkingDays(view: View)
    {
        if (settings.workingDays.size == 7) {
            view.findViewById<CheckBox>(R.id.chkSunday).isChecked = settings.workingDays[0]
            view.findViewById<CheckBox>(R.id.chkMonday).isChecked = settings.workingDays[1]
            view.findViewById<CheckBox>(R.id.chkTuesday).isChecked = settings.workingDays[2]
            view.findViewById<CheckBox>(R.id.chkWednesday).isChecked = settings.workingDays[3]
            view.findViewById<CheckBox>(R.id.chkThursday).isChecked = settings.workingDays[4]
            view.findViewById<CheckBox>(R.id.chkFriday).isChecked = settings.workingDays[5]
            view.findViewById<CheckBox>(R.id.chkSaturday).isChecked = settings.workingDays[6]
        }
    }

}