package com.nevexis.backend.schoolManagement.start_year

import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriodService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class StartSchoolYearController {

    @Autowired
    private lateinit var startSchoolYearService: StartSchoolYearService

    @Autowired
    private lateinit var schoolPeriodService: SchoolPeriodService

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/start-school-year")
    suspend fun startNewSchoolYear(
        @RequestBody schoolPeriod: SchoolPeriod,
        schoolId: BigDecimal
    ) {
        startSchoolYearService.startSchoolYear(schoolPeriod, schoolId)
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/check-started-school-year")
    suspend fun startedSchoolYear(
        @RequestBody schoolPeriod: SchoolPeriod,
        schoolId: BigDecimal
    ) = schoolPeriodService.checkIfSchoolPeriodIsStartedInSchool(schoolPeriod, schoolId)


}