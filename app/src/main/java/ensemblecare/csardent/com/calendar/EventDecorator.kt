package ensemblecare.csardent.com.calendar

import android.annotation.SuppressLint
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan


class EventDecorator(color: Int, dates: Collection<CalendarDay>?): DayViewDecorator {

    private var color = color
    private var dates: HashSet<CalendarDay>? = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates!!.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view!!.addSpan(DotSpan(10f, color))
    }
}