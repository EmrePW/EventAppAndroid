package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Comment_RecyclerViewAdapter(private val context: Context, private val items: MutableList<EventRating>) : RecyclerView.Adapter<Comment_RecyclerViewAdapter.MyViewHolder>()
{
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.commentTitle)
        var rating = itemView.findViewById<RatingBar>(R.id.commentRatingBar)
        var body = itemView.findViewById<TextView>(R.id.commentBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.rating_row, parent, false)

        return Comment_RecyclerViewAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = "Some random ass user" // get username from firebase kafama eserse
        holder.rating.rating = items.get(position).rating.toFloat()
        holder.body.text = items.get(position).body
    }

    override fun getItemCount(): Int {
        return items.size
    }

}