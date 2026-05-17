package com.example.freshcut.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.R
import com.example.freshcut.objects.StoreSettings
import java.time.LocalDate
import kotlin.collections.get

class CalendarAdapter(
    private var days: List<LocalDate?>,
    private val settings: StoreSettings,
    private val onDateSelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {
    var selectedDate: LocalDate? = null

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    fun updateDays(newDays: List<LocalDate?>) {
        this.days = newDays
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]

        if (date == null) {
            holder.tvDay.text = ""
            holder.tvDay.background = null
            holder.itemView.isEnabled = false
            return
        }

        holder.tvDay.text = date.dayOfMonth.toString()

        if (date == selectedDate) {
            holder.tvDay.setBackgroundResource(R.drawable.bg_selected_date) // Apply the blue circle
            holder.tvDay.setTextColor(Color.WHITE)
            holder.itemView.isEnabled = true
        }
        else if (isDateAvailable(date, settings)) {
            holder.tvDay.background = null
            holder.tvDay.setTextColor(Color.BLACK)
            holder.itemView.isEnabled = true

            holder.itemView.setOnClickListener {
                selectedDate = date
                notifyDataSetChanged()
                onDateSelected(date)
            }
        }
        else {
            holder.tvDay.background = null
            holder.tvDay.setTextColor(Color.LTGRAY)
            holder.itemView.isEnabled = false
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = days.size

    private fun isDateAvailable(targetDate: LocalDate, settings: StoreSettings): Boolean {
        if (targetDate.isBefore(LocalDate.now())) return false
        if (settings.parsedBlockedDates.contains(targetDate)) return false
        val dayIndex = targetDate.dayOfWeek.value-1
        if (dayIndex in settings.workingDays.indices) {
            return settings.workingDays[dayIndex]
        }
        return false
    }
}