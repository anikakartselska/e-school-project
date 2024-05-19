package com.nevexis.backend.schoolManagement.start_year

import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class StarSchoolYearController {

    @Autowired
    private lateinit var startSchoolYearService: StartSchoolYearService

    @PostMapping("/start-school-year")
    fun startNewSchoolYear(
        @RequestBody schoolPeriod: SchoolPeriod,
        schoolId: BigDecimal
    ) {
        startSchoolYearService.startSchoolYear(schoolPeriod, schoolId)
    }

}