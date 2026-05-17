package com.example.freshcut.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HolidayCalendarAdapter(
    private var days: List<LocalDate?>,
    val blockedDates: MutableSet<String>
) : RecyclerView.Adapter<HolidayCalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
    }

    fun updateDays(newDays: List<LocalDate?>) {
        this.days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
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
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val isBlocked = blockedDates.contains(dateString)

        // Prevent blocking past dates
        if (date.isBefore(LocalDate.now())) {
            holder.tvDay.setTextColor(Color.LTGRAY)
            holder.tvDay.background = null
            holder.itemView.isEnabled = false
        } else {
            holder.itemView.isEnabled = true

            if (isBlocked) {
                holder.tvDay.setBackgroundResource(R.drawable.bg_blocked_date)
                holder.tvDay.setTextColor(Color.WHITE)
            } else {
                holder.tvDay.background = null
                holder.tvDay.setTextColor(Color.BLACK)
            }

            holder.itemView.setOnClickListener {
                if (isBlocked) {
                    blockedDates.remove(dateString)
                } else {
                    blockedDates.add(dateString)
                }
                notifyItemChanged(position) // Refresh just this single square
            }
        }
    }

    override fun getItemCount(): Int = days.size
}