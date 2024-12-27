package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import com.facebook.login.Login
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
    private val baseTicketMasterUrl: String = "https://app.ticketmaster.com/discovery/v2/"
    private val testUrl: String = "https://jsonplaceholder.typicode.com/posts"
    //private lateinit var mainEventsObject: EventType
    private lateinit var recyclerView: RecyclerView
    private lateinit var myTestObjects: MutableList<TestObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        recyclerView = binding.myRecycler
        myTestObjects = mutableListOf()

        //startActivity(Intent(this, LoginActivity::class.java))

        setContentView(binding.root)

        fetchItems(testUrl)

        binding.searchEditText.addTextChangedListener{ query ->
            Log.i("Search", myTestObjects[0].title)
            val myPredicate: (TestObject) -> Boolean = { obj -> query.toString().lowercase() in obj.title.lowercase() }
            val result: MutableList<TestObject> = myTestObjects.filter(myPredicate).toMutableList()
            val result_adapter = TestObject_RecyclerViewAdapter(this@MainActivity, result)
            recyclerView.adapter = result_adapter
        }
    }
    override fun onStart() {
        super.onStart()

    }
    override fun onResume() {
        super.onResume()

    }
    private fun fetchItems(url: String){
        var myList: MutableList<TestObject>
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
                        myList = Json.decodeFromString(response.body!!.string())
                        runOnUiThread {
                            val adapter = TestObject_RecyclerViewAdapter(this@MainActivity, myList)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                            myTestObjects.addAll(myList)
                        }
                    }
                }
            }
        })
    }
}