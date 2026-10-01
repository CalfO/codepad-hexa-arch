package fr.cbtw.interview.domain.paymentprocessing;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/** The legacy calendar: weekends plus 25 December and 1 January (not the full TARGET2 calendar). */
public final class BankingCalendar {
    private static final Set<MonthDay> HOLIDAYS = Set.of(MonthDay.of(12, 25), MonthDay.of(1, 1));

    private BankingCalendar() {
    }

    public static LocalDate firstBusinessDayFrom(LocalDate date) {
        LocalDate day = date;
        while (!isBusinessDay(day)) {
            day = day.plusDays(1);
        }
        return day;
    }

    private static boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY
            && !HOLIDAYS.contains(MonthDay.from(date));
    }
}
