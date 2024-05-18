package com.nevexis.backend.schoolManagement.statistics

import com.nevexis.backend.schoolManagement.users.OneRoleUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class StatisticsController {

    @Autowired
    private lateinit var statisticsService: StatisticsService

    @PostMapping("/fetch-statistics-for-student")
    fun fetchStatisticsForStudent(
        @RequestBody student: OneRoleUser,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): StudentStatistics = statisticsService.getStatisticsForStudent(student, schoolId, periodId)

    @GetMapping("/fetch-statistics-for-school")
    fun fetchStatisticsForSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): SchoolStatistics = statisticsService.getStatisticsForSchool(schoolId, periodId)

}