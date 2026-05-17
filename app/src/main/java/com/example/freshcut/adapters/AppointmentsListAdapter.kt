package com.example.freshcut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.R
import com.example.freshcut.objects.Appointment
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentAdapter(private var dataList: List<Appointment>,
                         private val onDeleteClick: (Appointment) -> Unit) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {
    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.service_text_view)
        val timeText: TextView = itemView.findViewById(R.id.service_time_view)
        val dateText: TextView = itemView.findViewById(R.id.price_view)
        val serviceText: TextView = itemView.findViewById(R.id.appo_service_view)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_appointment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_card, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val currentItem = dataList[position]


        holder.nameText.text = currentItem.userName
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.dateText.text = dateFormatter.format(currentItem.timestamp.toDate())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.timeText.text = timeFormatter.format(currentItem.timestamp.toDate())
        holder.serviceText.text = currentItem.serviceName
        holder.deleteButton.setOnClickListener {
            onDeleteClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    fun updateList(newList: List<Appointment>) {
        dataList = newList
        notifyDataSetChanged()
    }
}