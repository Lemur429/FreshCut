package com.example.freshcut.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.R
import com.example.freshcut.adapters.AppointmentAdapter
import com.example.freshcut.objects.Appointment
import com.example.freshcut.utilities.DatabaseManager
import com.google.firebase.auth.FirebaseAuth

class TodayAppointmentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private val appointmentsList = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_today_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup adapter with the shared list
        adapter = AppointmentAdapter(appointmentsList) { appointmentToDelete ->
            DatabaseManager.deleteAppointment(
                appointmentId = appointmentToDelete.id,
                onSuccess = {
                    val position = appointmentsList.indexOf(appointmentToDelete)
                    if (position != -1) {
                        appointmentsList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(view.context, "Appointment deleted", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { exception ->
                    Toast.makeText(view.context, "Failed to delete: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
        recyclerView.adapter = adapter

        // Start the process: Check permissions first
        checkPermissionsAndLoad(view)
    }

    fun checkPermissionsAndLoad(view: View) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            DatabaseManager.isUserManager(
                userId = currentUserId,
                onSuccess = { isManager ->
                    loadAppointments(view, isManager)
                    view.findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
                },
                onFailure = {
                    loadAppointments(view, false)
                }
            )
        } else {

        }
    }

    private fun loadAppointments(view: View, isManager: Boolean) {
        DatabaseManager.getTodayAppointments(
            isManager = isManager,
            onSuccess = { appointments ->
                appointmentsList.clear()
                appointmentsList.addAll(appointments)
                adapter.updateList(appointmentsList)

                view.findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
            },
            onFailure = { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                view.findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
            }
        )
    }
}