package br.com.projeto180dias.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoutineDateRulesTest {
    @Test fun eventStartingBeforeDayAndEndingDuringDayOverlaps() {
        assertTrue(RoutineDateRules.overlaps(100L, 300L, 200L, 400L))
    }

    @Test fun eventEndingAtRangeStartDoesNotOverlap() {
        assertFalse(RoutineDateRules.overlaps(100L, 200L, 200L, 400L))
    }

    @Test fun eventStartingAtRangeEndDoesNotOverlap() {
        assertFalse(RoutineDateRules.overlaps(400L, 500L, 200L, 400L))
    }

    @Test fun eventCrossingMidnightOverlapsFollowingDay() {
        val start = LocalDate.of(2026, 11, 20).atTime(23, 0).atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 11, 21).atTime(1, 0).atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        val nextDayStart = LocalDate.of(2026, 11, 21).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        val nextDayEnd = LocalDate.of(2026, 11, 22).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        assertTrue(RoutineDateRules.overlaps(start, end, nextDayStart, nextDayEnd))
    }

    @Test fun weeklyRecurrenceKeepsWeekdayUntilJourneyEnd() {
        val start = LocalDate.of(2026, 11, 20) // Friday
        val dates = RoutineDateRules.recurrenceDates(start, start.plusDays(15), "WEEKLY")
        assertEquals(listOf(start, start.plusDays(7), start.plusDays(14)), dates)
    }

    @Test fun weekdaysRecurrenceSkipsWeekend() {
        val start = LocalDate.of(2026, 11, 20) // Friday
        val dates = RoutineDateRules.recurrenceDates(start, start.plusDays(3), "WEEKDAYS")
        assertEquals(listOf(start, start.plusDays(3)), dates)
    }

    @Test fun journeyHasExactly180CalendarDatesInclusive() {
        val start = LocalDate.of(2026, 11, 20)
        val end = LocalDate.of(2027, 5, 18)
        assertEquals(180, RoutineDateRules.recurrenceDates(start, end, "DAILY").size)
    }
}
