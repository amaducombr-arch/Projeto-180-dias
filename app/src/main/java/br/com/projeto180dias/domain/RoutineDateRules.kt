package br.com.projeto180dias.domain

import java.time.DayOfWeek
import java.time.LocalDate

object RoutineDateRules {
    fun overlaps(eventStart: Long, eventEnd: Long?, rangeStart: Long, rangeEnd: Long): Boolean {
        val effectiveEnd = eventEnd ?: (eventStart + 1L)
        return eventStart < rangeEnd && effectiveEnd > rangeStart
    }

    fun recurrenceDates(start: LocalDate, endInclusive: LocalDate, rule: String): List<LocalDate> {
        if (endInclusive.isBefore(start)) return emptyList()
        return generateSequence(start) { previous -> if (previous.isBefore(endInclusive)) previous.plusDays(1) else null }
            .filter { date ->
                when (rule) {
                    "DAILY" -> true
                    "WEEKLY" -> date.dayOfWeek == start.dayOfWeek
                    "WEEKDAYS" -> date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
                    else -> date == start
                }
            }.toList()
    }
}
