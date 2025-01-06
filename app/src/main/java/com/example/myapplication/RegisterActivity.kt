package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        binding.registerButton.setOnClickListener{
            binding.errorMessages.removeAllViews()

            val userEmail = binding.registerEmail.text.toString()
            val userPassword = binding.registerPassword.text.toString()
            val userPasswordAgain = binding.registerPasswordAgain.text.toString()

            val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            val passwordRegex = Regex("^(?=.*[A-Z]).{8,}$")

            if(userEmail.isBlank() || userPassword.isBlank() || userPasswordAgain.isBlank()) {
                Log.e("RegisterCheck", "One of the fields is empty")
                val textView = TextView(this).apply {
                    text = getString(R.string.fillAllFields)
                    setTextColor(getColor(R.color.fieldError)) // Set text color
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                binding.errorMessages.addView(textView)
                return@setOnClickListener
            }

            if(!userEmail.matches(emailRegex)){
                Log.e("RegisterCheck", "Not a valid email!")

                val textView = TextView(this).apply {
                    text = getString(R.string.invalidEmail)
                    setTextColor(getColor(R.color.fieldError)) // Set text color
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                binding.errorMessages.addView(textView)

                binding.registerEmail.text.clear()
                binding.registerPassword.text.clear()
                binding.registerPasswordAgain.text.clear()

                return@setOnClickListener
            }

            if(!userPassword.matches(passwordRegex)){
                Log.e("RegisterCheck", "Not a valid password!")

                val textView = TextView(this).apply {
                    text = getString(R.string.passwordCriteria1)
                    setTextColor(getColor(R.color.fieldError)) // Set text color
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                val textView2 = TextView(this).apply {
                    text = getString(R.string.passwordCriteria2)
                    setTextColor(getColor(R.color.fieldError)) // Set text color
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                binding.errorMessages.addView(textView)
                binding.errorMessages.addView(textView2)

                binding.registerPassword.text.clear()
                binding.registerPasswordAgain.text.clear()
                return@setOnClickListener
            }

            if (!userPassword.equals(userPasswordAgain)){
                Log.e("RegisterCheck", "passwords do not match!")

                val textView = TextView(this).apply {
                    text = getString(R.string.passwordsDontMatch)
                    setTextColor(getColor(R.color.fieldError)) // Set text color
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }
                binding.errorMessages.addView(textView)

                binding.registerPassword.text.clear()
                binding.registerPasswordAgain.text.clear()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign up successful
                        Log.d("RegisterCheck", "createUserWithEmail:success")
                        binding.errorMessages.addView(TextView(this@RegisterActivity).apply {
                            text = getString(R.string.registerSuccess)
                            setTextColor(getColor(R.color.bgColor))
                            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        })
                        val user = auth.currentUser
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("RegisterCheck", "createUserWithEmail:failure", task.exception)
                    }
                }
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) Log.i("FirebaseAuth", currentUser.email.toString())
    }
}