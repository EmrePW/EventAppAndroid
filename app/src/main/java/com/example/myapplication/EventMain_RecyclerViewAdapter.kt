package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventMain_RecyclerViewAdapter(private val context: Context, private val items: MutableList<Event>) : RecyclerView.Adapter<EventMain_RecyclerViewAdapter.MyViewHolder>() {

    // ViewHolder class to hold references to item views
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var eventName : TextView = itemView.findViewById(R.id.eventTitle)
        var eventSegment: TextView = itemView.findViewById(R.id.eventSegment)
        var eventGenre: TextView = itemView.findViewById(R.id.eventGenre)
        var eventIcon: ImageView = itemView.findViewById(R.id.eventIcon)
        var eventStatus: TextView = itemView.findViewById(R.id.statusView)
        var eventStartDate: TextView = itemView.findViewById(R.id.startDateView)
        var eventEndDate: TextView = itemView.findViewById(R.id.endDateView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.event_row, parent, false)

        return EventMain_RecyclerViewAdapter.MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.eventName.setText(
            if (items.get(position).name.length > 20) items.get(position).name.take(20) + "..." else items.get(position).name
        )
        holder.eventSegment.setText(items.get(position).classifications[0].segment.name)
        holder.eventGenre.setText(items.get(position).classifications[0].genre.name)
        //holder.eventLink.setText(items.get(position).url)

        // set Icon
        holder.eventIcon.setImageResource(
            when(items.get(position).classifications[0].segment.name.lowercase()) {
                "music" -> R.drawable.baseline_mic_24
                "sports" -> R.drawable.baseline_sports_soccer_24
                "arts & theatre" -> R.drawable.baseline_theater_comedy_24
                "film" -> R.drawable.baseline_theaters_24
                "nonticket" -> R.drawable.baseline_local_activity_24
                "miscellaneous" -> R.drawable.baseline_more_horiz_24
                else -> {
                    R.drawable.ic_launcher_foreground
                }
            }

        )
        if(items.get(position).dates.status.code == "onsale") {
            // set start and end dates
            holder.eventStatus.setText("On Sale!")
            holder.eventStartDate.setText(items.get(position).sales.public.startDateTime)
            holder.eventEndDate.setText(items.get(position).sales.public.endDateTime)

        }
        else {
            holder.eventStatus.setText("Sales have ended!")
            holder.eventStartDate.setText("")
            holder.eventEndDate.setText("")
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = items.size

    fun updateData(newItems: List<Event>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}