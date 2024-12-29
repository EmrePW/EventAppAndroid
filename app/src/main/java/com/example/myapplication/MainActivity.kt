package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ticketMasterAPIKey: String = "NEt1CpT1sA2eQDgr5a0OXJA7nWxNc9M4"
    private val baseTicketMasterUrl: String = "https://app.ticketmaster.com/discovery/v2/events?apikey=${ticketMasterAPIKey}"
    //private val testUrl: String = "https://jsonplaceholder.typicode.com/posts"
    //private lateinit var myTestObjects: MutableList<TestObject>
    private lateinit var mainEventsObject: MutableList<Event>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventMain_RecyclerViewAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("loginTest", "mainActivity onCreate")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        //startActivity(Intent(this, EventDetails::class.java))

        binding = ActivityMainBinding.inflate(layoutInflater)
        recyclerView = binding.myRecycler
        mainEventsObject = mutableListOf()
        auth = Firebase.auth

        setContentView(binding.root)

        fetchItems("$baseTicketMasterUrl&countryCode=TR&city=Erzurum")

        binding.searchEditText.addTextChangedListener{ query ->
            val myPredicate: (Event) -> Boolean = { obj -> query.toString().lowercase() in obj.name.lowercase() }
            val result: List<Event> = mainEventsObject.filter(myPredicate).toMutableList()
            adapter.updateData(result)
        }

        binding.filterButton.setOnClickListener{
            val popup = PopupMenu(this, binding.filterButton )
            popup.menuInflater.inflate(R.menu.filters_menu, popup.menu)

            // TODO: change segment names to ids
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId) {
                    // Music
                    R.id.option_1 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "music" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Sports
                    R.id.option_2 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "sports" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Arts & Theatre
                    R.id.option_3 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "arts & theatre" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Film
                    R.id.option_4 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "film" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Non-ticket
                    R.id.option_5 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "nonticket" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    // Miscellaneous
                    R.id.option_6 -> {
                        adapter.updateData(mainEventsObject.filter { obj -> obj.classifications[0].segment.name.lowercase() == "miscellaneous" })
                        binding.removeFilterButton.visibility = View.VISIBLE
                        true
                    }
                    else -> {
                        false
                    }
                }

            }
            // Show the popup menu.
            popup.show()
        }

        // TODO : reduce opacity of outline or reduce transparency by 50%
        binding.removeFilterButton.setOnClickListener {
            adapter.updateData(mainEventsObject)
            binding.removeFilterButton.visibility = View.INVISIBLE
        }
    }
    override fun onStart() {
        Log.i("loginTest", "mainActivity onStart")
        super.onStart()
        // TODO : fix Login alignment
        if(auth.currentUser == null) {
            Log.i("loginTest", "user is null")
            binding.loginProfileButton.setImageResource(R.drawable.logindrawable)
            binding.loginProfileButton.setOnClickListener{
                startActivity(Intent(this@MainActivity, LoginActivity::class.java ))
            }
        }
        else {
            val user = auth.currentUser
            Log.i("loginTest", "mainActivity onStart ${user!!.uid}")
            binding.loginProfileButton.setImageResource(R.drawable.profile_photo)
            binding.loginProfileButton.setOnClickListener {
                Log.i("loginTest", "click!")
                val popup = PopupMenu(this, binding.loginProfileButton)
                popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when(menuItem.itemId) {
                        R.id.settings -> {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            true
                        }
                        R.id.logout -> {
                            Firebase.auth.signOut()
                            startActivity(Intent(this@MainActivity, MainActivity::class.java))
                            true
                        }
                        else -> {
                            false
                        }

                    }
                }
                popup.show()
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
        Log.i("loginTest", "mainActivity onRestart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("loginTest", "mainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("loginTest", "mainActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("loginTest", "mainActivity onStop")
    }

    private fun fetchItems(url: String){
        var myList: MutableList<EventMain>
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        Log.i("APITEST", "Starting request...")
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // Handle the error
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("APITEST", "Response received!")
                response.use {
                    if (response.isSuccessful) {
                        val json = Json{
                            ignoreUnknownKeys = true
                        }
                        val res: List<Event> = json.decodeFromString<EventMain>(response.body!!.string())._embedded.events
                        runOnUiThread {
                            adapter = EventMain_RecyclerViewAdapter(this@MainActivity, res.toMutableList()){
                                position ->
                                val intent = Intent(this@MainActivity, EventDetailsActivity::class.java)
                                intent.putExtra("event", json.encodeToString(Event.serializer(), res.get(position)))
                                startActivity(intent)
                            }
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                            mainEventsObject.addAll(res)
                        }
                    }
                }
            }
        })
    }
}