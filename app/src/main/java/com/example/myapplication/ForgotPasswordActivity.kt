package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityForgotPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        binding.button8.setOnClickListener{
            val email = binding.emailEditText.text.toString()

            if(email.isBlank()) {
                binding.emailField.error = "Please fill out this field!"
                return@setOnClickListener
            }

            if (!email.matches(emailRegex)){
                binding.emailField.error = "Please enter a valid email!"
                return@setOnClickListener
            }
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Forgot Password", "Email sent.")
                        startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
                    }
                }
        }
    }
}