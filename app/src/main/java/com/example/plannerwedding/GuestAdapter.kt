package com.example.plannerwedding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuestAdapter(private val guestList: MutableList<Guest>) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    class GuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.guestName)
        val email: TextView = view.findViewById(R.id.guestEmail)
        val accommodation: TextView = view.findViewById(R.id.guestAccommodation)
        val bedroomIcon: ImageView = view.findViewById(R.id.bedroomIcon)
        val deleteGuest: ImageView = view.findViewById(R.id.deleteGuest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_guest__card, parent, false)
        return GuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest = guestList[position]
        holder.name.text = guest.name
        holder.email.text = guest.email
        holder.accommodation.text = if (guest.accommodationNeeded) "Needs Accommodation" else "No Accommodation"
        holder.bedroomIcon.visibility = if (guest.accommodationNeeded) View.VISIBLE else View.GONE

        holder.deleteGuest.setOnClickListener {
            guestList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, guestList.size)
        }
    }

    override fun getItemCount(): Int = guestList.size
}
