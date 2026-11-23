package dz.arabic.konnachetaleb.domain

import dz.arabic.konnachetaleb.data.SubjectEntity
import dz.arabic.konnachetaleb.data.TimetableSlotEntity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class ActiveClassMatch(
    val slot: TimetableSlotEntity,
    val subject: SubjectEntity,
    val isOngoing: Boolean
)

object TimetableHelper {
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Algerian university week: Sunday (7) to Thursday (4)
    val universityDays: List<Pair<Int, String>> = listOf(
        DayOfWeek.SUNDAY.value to "الأحد",
        DayOfWeek.MONDAY.value to "الإثنين",
        DayOfWeek.TUESDAY.value to "الثلاثاء",
        DayOfWeek.WEDNESDAY.value to "الأربعاء",
        DayOfWeek.THURSDAY.value to "الخميس"
    )

    fun dayName(isoDayOfWeek: Int): String =
        universityDays.firstOrNull { it.first == isoDayOfWeek }?.second ?: when (isoDayOfWeek) {
            DayOfWeek.FRIDAY.value -> "الجمعة"
            DayOfWeek.SATURDAY.value -> "السبت"
            else -> "الأحد"
        }

    fun parseTimeOrNull(hhmm: String): LocalTime? =
        runCatching { LocalTime.parse(hhmm.trim(), timeFmt) }.getOrNull()

    /**
     * Checks if there is an ongoing class right now OR starting within [lookaheadMinutes] (default 10 min).
     */
    fun findOngoingOrSoonClass(
        now: LocalDateTime,
        slots: List<TimetableSlotEntity>,
        subjectsById: Map<String, SubjectEntity>,
        lookaheadMinutes: Long = 10L
    ): ActiveClassMatch? {
        val todayIso = now.dayOfWeek.value
        val currentTime = now.toLocalTime()

        val todaySlots = slots
            .filter { it.dayOfWeek == todayIso }
            .sortedBy { it.startTime }

        for (slot in todaySlots) {
            val start = parseTimeOrNull(slot.startTime) ?: continue
            val end = parseTimeOrNull(slot.endTime) ?: continue
            val windowStart = start.minusMinutes(lookaheadMinutes)

            if (!currentTime.isBefore(windowStart) && currentTime.isBefore(end)) {
                val subject = subjectsById[slot.subjectId] ?: continue
                return ActiveClassMatch(
                    slot = slot,
                    subject = subject,
                    isOngoing = !currentTime.isBefore(start)
                )
            }
        }
        return null
    }

    /**
     * Finds the next upcoming class across the week for the Home Screen smart banner.
     */
    fun findNextUpcomingClass(
        now: LocalDateTime,
        slots: List<TimetableSlotEntity>,
        subjectsById: Map<String, SubjectEntity>
    ): ActiveClassMatch? {
        if (slots.isEmpty()) return null
        val ongoing = findOngoingOrSoonClass(now, slots, subjectsById, lookaheadMinutes = 10L)
        if (ongoing != null) return ongoing

        var bestDeltaMinutes = Long.MAX_VALUE
        var bestMatch: ActiveClassMatch? = null

        for (dayOffset in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(dayOffset.toLong())
            val isoDay = candidateDate.dayOfWeek.value
            val daySlots = slots.filter { it.dayOfWeek == isoDay }
            for (slot in daySlots) {
                val start = parseTimeOrNull(slot.startTime) ?: continue
                val startDt = LocalDateTime.of(candidateDate, start)
                if (startDt.isAfter(now)) {
                    val delta = java.time.Duration.between(now, startDt).toMinutes()
                    if (delta < bestDeltaMinutes) {
                        val subject = subjectsById[slot.subjectId] ?: continue
                        bestDeltaMinutes = delta
                        bestMatch = ActiveClassMatch(slot, subject, isOngoing = false)
                    }
                }
            }
            if (bestMatch != null && dayOffset > 0) break
        }
        return bestMatch
    }
}
