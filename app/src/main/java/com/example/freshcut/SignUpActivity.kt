package com.example.freshcut

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.freshcut.utilities.AuthManager
import com.example.freshcut.utilities.DatabaseManager
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameFieldView: TextInputEditText
    private lateinit var mailFieldView: TextInputEditText
    private lateinit var passFieldView: TextInputEditText
    private lateinit var signupButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // Go back to login screen if they click the text
        findViewById<TextView>(R.id.signin_text).setOnClickListener { finish() }

        signupButton.setOnClickListener {
            val name = nameFieldView.text.toString().trim()
            val email = mailFieldView.text.toString().trim()
            val password = passFieldView.text.toString().trim()

            if (name.isEmpty()) {
                nameFieldView.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                mailFieldView.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passFieldView.error = "Password is required"
                return@setOnClickListener
            }

            // Let AuthManager handle the heavy lifting!
            AuthManager.signUp(
                email = email,
                password = password,
                name = name,
                onSuccess = { uid ->

                    val userProfile = hashMapOf(
                        "uid" to uid,
                        "fullName" to name,
                        "email" to email,
                        "role" to "customer" // for admin role change in firestore
                    )

                    DatabaseManager.saveUserOnDB(userProfile as HashMap<String, String?>)  /// we want the use to be saved in firestore too
                    Toast.makeText(this, "Authentication Success.", Toast.LENGTH_SHORT).show()

                    finish()
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun initViews() {
        nameFieldView = findViewById(R.id.fullname_field)
        mailFieldView = findViewById(R.id.mail_field)
        passFieldView = findViewById(R.id.password_field)
        signupButton = findViewById(R.id.signup_button)
    }
}