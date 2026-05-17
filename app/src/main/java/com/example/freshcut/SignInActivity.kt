package com.example.freshcut

import android.content.Intent
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
import com.google.rpc.context.AttributeContext

class SignInActivity : AppCompatActivity() {
    private lateinit var emailField : TextInputEditText
    private lateinit var passwordField : TextInputEditText
    private lateinit var signInButton: Button
    private lateinit var signUpText : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (AuthManager.isUserLoggedIn())
        {
            DatabaseManager.isUserManager(AuthManager.getCurrentUser()?.uid ?: "",{ isManager ->
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                if (isManager)
                {
                    val intent = Intent(this, ManagerActivity::class.java)
                    startActivity(intent)

                }else
                {
                    val intent = Intent(this, CostumerActivity::class.java)
                    startActivity(intent)

                }
                finish()
            },{ e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()

            })
        }
            initViews()

            signInButton.setOnClickListener {
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()

                if (email.isEmpty()) {
                    emailField.error = "Email is required"
                    return@setOnClickListener
                }

                if (password.isEmpty()) {
                    passwordField.error = "Password is required"
                    return@setOnClickListener
                }

                AuthManager.logIn(email, password,
                    onSuccess= { uid ->
                        DatabaseManager.isUserManager(uid,{ isManager ->
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                            if (isManager)
                            {
                                val intent = Intent(this, ManagerActivity::class.java)
                                startActivity(intent)

                            }else
                            {
                                val intent = Intent(this, CostumerActivity::class.java)
                                startActivity(intent)

                            }
                            finish()
                        },{ e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()

                        })


                    },
                    onFailure ={ e ->
                        Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_LONG).show()
                    }
                )

            }

            signUpText.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }


    }
    fun initViews()
    {

        emailField = findViewById<TextInputEditText>(R.id.mail_field)
       passwordField = findViewById<TextInputEditText>(R.id.password_field)
        signInButton = findViewById<Button>(R.id.signin_button)
        signUpText = findViewById<TextView>(R.id.signup_text)

    }
}