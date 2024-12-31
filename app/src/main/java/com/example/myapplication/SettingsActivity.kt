package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySettingsBinding
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val ticketMasterAPIKey: String = "NEt1CpT1sA2eQDgr5a0OXJA7nWxNc9M4"
    private val baseTicketMasterUrl: String = "https://app.ticketmaster.com/discovery/v2/events/"

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordRegex = Regex("^(?=.*[A-Z]).{8,}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = Firebase.firestore
        setContentView(binding.root)

        // profile
        binding.cardView3.setOnClickListener{
            Log.i("settingsActivity", "profileClick!")
            binding.profileconstraintLayout.visibility = if (binding.profileconstraintLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }


        //notifications
        binding.cardView4.setOnClickListener{
            Log.i("settingsActivity", "notificationsClick!")
            binding.notificationConstraintLayout.visibility = if (binding.notificationConstraintLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        //events
        binding.cardView5.setOnClickListener{
            Log.i("settingsActivity", "eventsClick!")
            binding.eventsConstraintLayout.visibility = if (binding.eventsConstraintLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        //favourites
        binding.cardView6.setOnClickListener{
            Log.i("settingsActivity", "likedClick!")
            binding.likedEvents.visibility = if (binding.likedEvents.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // change email if google no change
        binding.button2.setOnClickListener{
            val newEmail: EditText = binding.changeEmailEditText

            if(newEmail.text.isNullOrBlank()){
                binding.errorContainer.removeAllViews()
                val error: TextView = TextView(this).apply {
                    setTextColor(getColor(R.color.fieldError))
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    text = "Please fill out the field!"
                }
                binding.errorContainer.addView(error)
                binding.changeEmailEditText.text.clear()
                return@setOnClickListener
            }

            if (!newEmail.text.matches(emailRegex)) {
                binding.errorContainer.removeAllViews()
                val error: TextView = TextView(this).apply {
                    setTextColor(getColor(R.color.fieldError))
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    text = "Please provide a valid email!"
                }
                binding.errorContainer.addView(error)
                binding.changeEmailEditText.text.clear()
                return@setOnClickListener
            }

            auth.currentUser!!.updateEmail("user@example.com")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Settings", "User email address updated.")
                        binding.errorContainer.removeAllViews()
                        val success: TextView = TextView(this).apply {
                            setTextColor(getColor(R.color.textColorPrimary))
                            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                            text = "Email change successful!"
                        }
                        binding.errorContainer.addView(success)

                    }
                }
        }

        // change password if google do not permit
        binding.button3.setOnClickListener{
            val newPassword: String = binding.editTextText.text.toString()

            if(newPassword.isBlank()) {
                binding.errorContainer.removeAllViews()
                val error: TextView = TextView(this).apply {
                    setTextColor(getColor(R.color.fieldError))
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    text = "Please fill out the field!"
                }
                binding.errorContainer.addView(error)
                binding.editTextText.text.clear()
                return@setOnClickListener
            }

            if(!newPassword.matches(passwordRegex)) {
                binding.errorContainer.removeAllViews()
                val error: TextView = TextView(this).apply {
                    setTextColor(getColor(R.color.fieldError))
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    text = "Password must include at least 8 characters and an uppercase letter."
                }
                binding.errorContainer.addView(error)
                binding.editTextText.text.clear()
                return@setOnClickListener
            }

            auth.currentUser!!.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase Password Update", "User password updated.")
                        binding.errorContainer.removeAllViews()
                        val success: TextView = TextView(this).apply {
                            setTextColor(getColor(R.color.textColorPrimary))
                            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                            text = "Password change successful!"
                        }
                        binding.errorContainer.addView(success)
                    }
                }
        }

        // notification preferences
        binding.button7.setOnClickListener{
            val opt1: MaterialSwitch = binding.materialSwitch
            val opt2: MaterialSwitch = binding.materialSwitch2
            val opt3: MaterialSwitch = binding.materialSwitch3

            db.collection("users").document(auth.currentUser!!.uid).update("notificationPreference", opt1.isChecked)
            db.collection("users").document(auth.currentUser!!.uid).update("notifyOnUpcomingEvent", opt2.isChecked)
            db.collection("users").document(auth.currentUser!!.uid).update("notifyOnNewEvent", opt3.isChecked)
        }

        // event preferences
        binding.button4.setOnClickListener{
            val sports: String = if (binding.checkBox2.isChecked) "A" else "N"
            val films: String =  if (binding.checkBox.isChecked) "B" else "N"
            val arts: String = if (binding.checkBox3.isChecked) "C" else "N"
            val music: String = if (binding.checkBox4.isChecked) "D" else "N"
            val nonticket: String = if (binding.checkBox5.isChecked) "E" else "N"
            val misc: String = if (binding.checkBox6.isChecked) "F" else "N"

            val resultStr = sports + films + arts + music + nonticket + misc

            db.collection("users").document(auth.currentUser!!.uid).update("eventPreferences", resultStr)
        }

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            return
        }
        val user = auth.currentUser!!
        var userData: User
        db.collection("users").document(user.uid).get().addOnSuccessListener {
            document ->
                if(document.exists()) {
                    userData = document.toObject<User>()!!
                    Log.i("userData", "data inside exists : ${userData}")

                }
                else {
                    throw Exception("userData error")
                }
            Log.d("userData", " 1 $userData")

            binding.materialSwitch2.isChecked = userData.notifyOnUpcomingEvent
            binding.materialSwitch.isChecked = userData.notificationPreference
            binding.materialSwitch3.isChecked = userData.notifyOnNewEvent

            binding.checkBox.isChecked = userData.eventPreferences[1] != 'N'
            binding.checkBox2.isChecked = userData.eventPreferences[0] != 'N'
            binding.checkBox3.isChecked = userData.eventPreferences[2] != 'N'
            binding.checkBox4.isChecked = userData.eventPreferences[3] != 'N'
            binding.checkBox5.isChecked = userData.eventPreferences[4] != 'N'
            binding.checkBox6.isChecked = userData.eventPreferences[5] != 'N'

            Log.d("userData", "2 $userData")

            // liked events
            val client = OkHttpClient()
            for (event in userData.favouriteEvents){
                val request = Request.Builder().url("$baseTicketMasterUrl/$event?apikey=${ticketMasterAPIKey}").build()

                client.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace() // Handle the error
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.i("APITEST", "Response for $event received!")
                        response.use{
                            if (response.isSuccessful) {
                                val json = Json{
                                    ignoreUnknownKeys = true
                                }
                                val res: Event = json.decodeFromString<Event>(response.body!!.string())
                                val eventView: TextView = TextView(this@SettingsActivity).apply {
                                    setTextColor(getColor(R.color.textColorPrimary))
                                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                                    setText("${res.name} ${res.dates.start.localDate}")
                                    setTextSize(18f)
                                }
                                runOnUiThread{
                                    binding.likedEvents.addView(eventView)
                                }

                            }
                        }
                    }

                })
            }


        }

    }
}