package com.nevexis.backend.schoolManagement.school_calendar

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class CalendarController {

    @Autowired
    private lateinit var calendarService: CalendarService

    @GetMapping("/fetch-school-calendar-for-school-and-period")
    fun fetchSchoolCalendarForSchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): Calendar? = calendarService.getSchoolCalendarForSchoolAndPeriod(schoolId, periodId)

    @GetMapping("/fetch-school-weeks-for-school-class")
    fun fetchSchoolWeeksForSchoolClass(
        @RequestParam schoolClassName: String,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = calendarService.getSchoolWeeksForSchoolClass(schoolClassName, schoolId, periodId)

    @PostMapping("/update-calendar")
    suspend fun updateCalendar(
        @RequestBody calendar: Calendar, @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = calendarService.saveUpdateCalendar(calendar, schoolId, periodId)

    @GetMapping("/fetch-weeks-for-school-class-school-and-period")
    fun fetchWeeksForSchoolClassSchoolAndPeriod(
        @RequestParam schoolClassName: String,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<Week> = calendarService.getWeeksForSchoolClassSchoolAndPeriod(schoolClassName, schoolId, periodId)

}