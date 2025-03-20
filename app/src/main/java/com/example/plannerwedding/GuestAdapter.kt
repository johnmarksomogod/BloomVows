package com.example.plannerwedding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuestAdapter(
    private val guestList: MutableList<Guest>,
    private val onDeleteClick: (Guest, Int) -> Unit,
    private val onInviteClick: (Guest, Int) -> Unit  // New callback for invite action
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    // Selection mode variables
    private var selectionMode = false
    private val selectedGuests = mutableSetOf<Int>()
    private var onSelectionChangedListener: ((Int) -> Unit)? = null

    class GuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.guestName)
        val email: TextView = view.findViewById(R.id.guestEmail)
        val category: TextView = view.findViewById(R.id.guestCategory)
        val accommodation: TextView = view.findViewById(R.id.guestAccommodation)
        val bedroomIcon: ImageView = view.findViewById(R.id.bedroomIcon)
        val deleteGuest: ImageView = view.findViewById(R.id.deleteGuest)
        val inviteGuest: ImageView = view.findViewById(R.id.inviteGuest) // New invite button
        val selectCheckbox: CheckBox = view.findViewById(R.id.selectCheckbox)
        val invitedStatus: TextView = view.findViewById(R.id.invitedStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_guest__card, parent, false)
        return GuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest = guestList[position]
        holder.name.text = guest.name
        holder.email.text = guest.email
        holder.category.text = guest.category
        holder.accommodation.text = if (guest.accommodationNeeded) "Needs Accommodation" else "No Accommodation"
        holder.bedroomIcon.visibility = if (guest.accommodationNeeded) View.VISIBLE else View.GONE

        // Show invited status if applicable
        if (guest.invited) {
            holder.invitedStatus.visibility = View.VISIBLE
            holder.invitedStatus.text = "Invited"
            holder.inviteGuest.visibility = View.GONE // Hide invite button if already invited
        } else {
            holder.invitedStatus.visibility = View.GONE
            holder.inviteGuest.visibility = View.VISIBLE // Show invite button if not invited
        }

        // Selection mode handling
        holder.selectCheckbox.visibility = if (selectionMode) View.VISIBLE else View.GONE
        holder.deleteGuest.visibility = if (selectionMode) View.GONE else View.VISIBLE

        // Set invite button click listener
        holder.inviteGuest.setOnClickListener {
            onInviteClick(guest, position)
        }

        // Remove old listener before setting new one to prevent multiple listeners
        holder.selectCheckbox.setOnCheckedChangeListener(null)

        // Set checkbox state based on selection
        holder.selectCheckbox.isChecked = selectedGuests.contains(position)

        // Checkbox click listener
        holder.selectCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedGuests.add(position)
            } else {
                selectedGuests.remove(position)
            }
            onSelectionChangedListener?.invoke(selectedGuests.size)
        }

        // Delete button click listener
        holder.deleteGuest.setOnClickListener {
            onDeleteClick(guest, position)
        }

        // Click on the card to toggle selection
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                val newState = !holder.selectCheckbox.isChecked
                holder.selectCheckbox.isChecked = newState
                if (newState) {
                    selectedGuests.add(position)
                } else {
                    selectedGuests.remove(position)
                }
                onSelectionChangedListener?.invoke(selectedGuests.size)
            }
        }
    }

    override fun getItemCount(): Int = guestList.size

    // Toggle selection mode
    fun toggleSelectionMode(enabled: Boolean) {
        selectionMode = enabled
        if (!enabled) {
            selectedGuests.clear()
            onSelectionChangedListener?.invoke(0)
        }
        notifyDataSetChanged()
    }

    // Set selection change listener
    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChangedListener = listener
    }

    // Get selected guests
    fun getSelectedGuests(): List<Guest> {
        return selectedGuests.mapNotNull {
            if (it < guestList.size) guestList[it] else null
        }
    }

    // Get selected count
    fun getSelectedCount(): Int {
        return selectedGuests.size
    }

    // Select all guests
    fun selectAll() {
        selectedGuests.clear()
        for (i in guestList.indices) {
            // Only select guests that haven't been invited yet
            if (!guestList[i].invited) {
                selectedGuests.add(i)
            }
        }
        onSelectionChangedListener?.invoke(selectedGuests.size)
        notifyDataSetChanged()
    }

    // Clear selection
    fun clearSelection() {
        selectedGuests.clear()
        onSelectionChangedListener?.invoke(0)
        notifyDataSetChanged()
    }
}