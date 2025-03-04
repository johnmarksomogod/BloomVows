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
        val currentDate = CalendarDay.today()
        val dateParts = scheduleDate.split("/")
        val scheduleDateObj = CalendarDay.from(
            dateParts[2].toInt(),
            dateParts[0].toInt() - 1,
            dateParts[1].toInt()
        )

        status = when {
            status == "Completed" -> "Completed"
            scheduleDateObj.isBefore(currentDate) -> "Expired"
            else -> "Pending"
        }
    }
}
