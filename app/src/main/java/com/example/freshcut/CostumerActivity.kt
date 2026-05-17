package com.example.freshcut

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import com.example.freshcut.fragments.AppointmentsFragment
import com.example.freshcut.fragments.TodayAppointmentsFragment
import com.example.freshcut.utilities.AuthManager

class CostumerActivity : AppCompatActivity() {
    private lateinit var appointment_layout: ConstraintLayout
    private val todayAppointmentsFragment = AppointmentsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_costumer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.btn_logout_layout).setOnClickListener {
            AuthManager.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.welcome_user_text).text = "${getString(R.string.welcome_user_text)}, ${AuthManager.getCurrentUser()?.displayName}!"

        appointment_layout = findViewById(R.id.new_appointment_layout)
        appointment_layout.setOnClickListener {
            val intent = Intent(this, CreateAppointmentActivity::class.java)
            startActivity(intent)
        }
        findViewById<FragmentContainerView>(R.id.close_appointments_container)

        supportFragmentManager.beginTransaction().replace(R.id.close_appointments_container, todayAppointmentsFragment).commit()
    }
    override fun onResume() {
        super.onResume()
        todayAppointmentsFragment.view?.let { active ->
            todayAppointmentsFragment.checkPermissionsAndLoad(active)
        }

    }
}