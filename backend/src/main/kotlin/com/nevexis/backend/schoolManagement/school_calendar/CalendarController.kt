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


}