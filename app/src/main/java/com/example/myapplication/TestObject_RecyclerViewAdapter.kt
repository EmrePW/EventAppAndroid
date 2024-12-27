package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestObject_RecyclerViewAdapter(private val context: Context, private val items: MutableList<TestObject>) : RecyclerView.Adapter<TestObject_RecyclerViewAdapter.MyViewHolder>() {

    // ViewHolder class to hold references to item views
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var testTitle : TextView = itemView.findViewById(R.id.testTitleView)
        var testBody: TextView = itemView.findViewById(R.id.testBodyView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.recycler_view_row, parent, false)

        return TestObject_RecyclerViewAdapter.MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.testTitle.setText(items.get(position).title)
        holder.testBody.setText(items.get(position).body)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = items.size
}