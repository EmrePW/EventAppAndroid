package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LikedEvent_RecyclerViewAdapter(private val context: Context, private val items: MutableList<Event>, private val clickListener: (Int) -> Unit) : RecyclerView.Adapter<LikedEvent_RecyclerViewAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var title = itemView.findViewById<TextView>(R.id.likedEventTitle)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LikedEvent_RecyclerViewAdapter.MyViewHolder {
        var inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.liked_event_row, parent, false)

        return  LikedEvent_RecyclerViewAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.title.setText(items.get(position).name)

        holder.itemView.setOnClickListener {
            clickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}