package com.example.freshcut

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcut.adapters.CalendarAdapter
import com.example.freshcut.objects.Appointment
import com.example.freshcut.objects.Service
import com.example.freshcut.objects.StoreSettings
import com.example.freshcut.utilities.AuthManager
import com.example.freshcut.utilities.DatabaseManager
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CreateAppointmentActivity : AppCompatActivity() {
    private lateinit var selectedDate: LocalDate
    private lateinit var settings: StoreSettings;
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var timeSpinner : Spinner

    private var currentMonthDate = LocalDate.now()
    private var currentlySelectedCard: MaterialCardView? = null
    private var selectedService: Service? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_appointment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.welcome_user_text).text = "${getString(R.string.welcome_user_text)}, ${AuthManager.getCurrentUser()?.displayName}!"
        findViewById<LinearLayout>(R.id.btn_back_layout).setOnClickListener { finish() }

        timeSpinner = findViewById<Spinner>(R.id.timeSpinner)


        DatabaseManager.getServicesList{ services ->
            if (services != null) {
                for (service in services) {
                    val newCard = layoutInflater.inflate(R.layout.service_card, findViewById<LinearLayout>(R.id.services_layout), false) as MaterialCardView

                    newCard.findViewById<TextView>(R.id.service_text_view).text =service.name
                    newCard.findViewById<TextView>(R.id.price_view).text = """${service.price} Shekels"""
                    newCard.findViewById<TextView>(R.id.service_time_view).text ="""${service.duration} Mins"""

                    newCard.setOnClickListener { clickedView ->
                        val clickedCard = clickedView as MaterialCardView
                        currentlySelectedCard?.isChecked = false

                        clickedCard.isChecked = true

                        currentlySelectedCard = clickedCard
                        selectedService = service
                        }

                        findViewById<LinearLayout>(R.id.services_layout).addView(newCard)
                    }

                }
            }
        DatabaseManager.getStoreSettings { settings ->
            this.settings=settings
            setupCalendar()
            findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE

        }
        findViewById<Button>(R.id.btnConfirmBooking).setOnClickListener{
            val date = calendarAdapter.selectedDate


            val time: String? = if (timeSpinner.visibility == View.VISIBLE && timeSpinner.selectedItem != null) {
                timeSpinner.selectedItem.toString()
            } else {
                null
            }


            if (selectedService == null)
            {
                Toast.makeText(this, getString(R.string.select_service_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val serviceId = selectedService!!.id
            val serviceName = selectedService!!.name
            val duration = selectedService?.duration


            if (date == null) {
                Toast.makeText(this, getString(R.string.select_date_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stops the code here
            }

            if (time == null) {
                Toast.makeText(this, getString(R.string.select_time_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


                DatabaseManager.createAppointment(date,time,serviceId,serviceName,duration,
                    {
                        finish()
                    },{e ->
                        Toast.makeText(this, "ERROR : ${e.toString()}", Toast.LENGTH_SHORT).show()

                    })
        }

    }


    private fun setupCalendar() {
        val calendarRecyclerView = findViewById<RecyclerView>(R.id.calendarRecyclerView)
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)

        // Initialize adapter with an empty list at first
        calendarAdapter = CalendarAdapter(emptyList(), settings) { selectedDate ->
            this.selectedDate =selectedDate
            setupTimeSplinner()
        }
        calendarRecyclerView.adapter = calendarAdapter

        findViewById<Button>(R.id.btnPrevMonth).setOnClickListener {
            currentMonthDate = currentMonthDate.minusMonths(1)
            refreshCalendar()
        }

        findViewById<Button>(R.id.btnNextMonth).setOnClickListener {
            currentMonthDate = currentMonthDate.plusMonths(1)
            refreshCalendar()
        }

        refreshCalendar()
    }

    private fun refreshCalendar() {
        val tvMonthYear = findViewById<TextView>(R.id.tvMonthYear)
        tvMonthYear.text = currentMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        val daysInMonth = generateMonthGrid(currentMonthDate.year, currentMonthDate.monthValue)

        // Pass the new days to the adapter
        calendarAdapter.updateDays(daysInMonth)
    }

    private fun generateMonthGrid(year: Int, month: Int): List<LocalDate?> {
        val firstDay = LocalDate.of(year, month, 1)
        val dayOfWeek = firstDay.dayOfWeek.value
        val grid = mutableListOf<LocalDate?>()

        for (i in 1 until dayOfWeek) {
            grid.add(null)
        }
        for (i in 1..firstDay.lengthOfMonth()) {
            grid.add(LocalDate.of(year, month, i))
        }
        return grid
    }
    private fun setupTimeSplinner()
    {
        val times = generateTimeSlots(settings.parsedOpeningTime, settings.parsedClosingTime)
        DatabaseManager.getBookedTimesFromFirebase(selectedDate) { bookedTimes ->

            val availableTimes = times.filter { time ->
                !bookedTimes.contains(time)
            }

            if (availableTimes.isEmpty()) {
                timeSpinner.visibility = View.GONE
                Toast.makeText(this, "Sorry, this date is fully booked!", Toast.LENGTH_LONG).show()
                return@getBookedTimesFromFirebase // Stop running the code
            }

            // 4. Convert the remaining valid times to Strings for the dropdown
            val timeStrings = availableTimes.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) }

            // 5. Build the simple dropdown adapter
            val spinnerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                timeStrings
            )

            // 6. Attach it to your Spinner and show it
            timeSpinner.adapter = spinnerAdapter
            timeSpinner.visibility = View.VISIBLE

            // Optional: Hide your loading indicator here
        }
    }
    private fun generateTimeSlots(
        openingTime: LocalTime,
        closingTime: LocalTime,
        intervalMinutes: Long = 60 // You can change this to 15, 45, 60, etc.
    ): List<LocalTime> {
        val slots = mutableListOf<LocalTime>()
        var currentTime = openingTime

        // Keep adding time slots until the current time reaches the closing time
        while (currentTime.isBefore(closingTime)) {
            slots.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes)
        }

        return slots
    }
}
