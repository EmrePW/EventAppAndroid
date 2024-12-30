package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
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

        //favs
        binding.cardView6.setOnClickListener{
            Log.i("settingsActivity", "likedClick!")
        }

    }
}