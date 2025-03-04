package com.example.plannerwedding

import android.graphics.Color
import android.text.style.AbsoluteSizeSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class DotDecorator(private val dates: HashSet<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day) // Only decorate the dates that are in the set
    }

    override fun decorate(view: DayViewFacade) {
        // Set the color of the dot to EDABAD
        view.addSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#EDABAD")))

        // Set the text size to make it slightly bigger (e.g., 18px)
        view.addSpan(AbsoluteSizeSpan(18, true)) // The second parameter is for scaling relative to the text size (true means scale with screen density)
    }
}
