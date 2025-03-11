package com.example.plannerwedding

import android.os.Parcel
import android.os.Parcelable
import com.prolificinteractive.materialcalendarview.CalendarDay

data class ScheduleItem(
    var scheduleId: String = "",
    var scheduleName: String = "",
    var scheduleDate: String = "",
    var status: String = "Pending" // "Pending", "Completed", "Expired"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "Pending"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(scheduleId)
        parcel.writeString(scheduleName)
        parcel.writeString(scheduleDate)
        parcel.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ScheduleItem> = object : Parcelable.Creator<ScheduleItem> {
            override fun createFromParcel(parcel: Parcel): ScheduleItem {
                return ScheduleItem(parcel)
            }

            override fun newArray(size: Int): Array<ScheduleItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    // Method to update the status dynamically
    fun updateStatus() {
        // If already completed, don't change status
        if (status == "Completed") return

        try {
            val currentDate = CalendarDay.today()
            val dateParts = scheduleDate.split("/")

            if (dateParts.size == 3) {
                val scheduleMonth = dateParts[0].toInt() - 1 // Calendar months are 0-based
                val scheduleDay = dateParts[1].toInt()
                val scheduleYear = dateParts[2].toInt()

                val scheduleDateObj = CalendarDay.from(scheduleYear, scheduleMonth, scheduleDay)

                // Update status based on date comparison
                status = when {
                    scheduleDateObj.isBefore(currentDate) -> "Expired"
                    else -> "Pending"
                }
            }
        } catch (e: Exception) {
            // Handle any parsing errors gracefully
            // Keep current status if there's an error
        }
    }
}