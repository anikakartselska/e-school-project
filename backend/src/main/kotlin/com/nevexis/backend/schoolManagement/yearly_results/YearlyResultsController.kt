package com.nevexis.backend.schoolManagement.yearly_results

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class YearlyResultsController {

    @Autowired
    private lateinit var yearlyResultsService: YearlyResultsService

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/fetch-all-yearly-results-for-school-class-period-and-school")
    suspend fun fetchAllYearlyResultsForSchoolClassPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal,
    ): List<StudentToYearlyResult> =
        yearlyResultsService.getAllYearlyResultsForSchoolClassPeriodAndSchool(schoolId, periodId, schoolClassId)

    @GetMapping("/fetch-yearly-results-for-student-period-and-school")
    fun fetchYearlyResultsForStudentPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        studentId: BigDecimal,
    ): YearlyResults? = yearlyResultsService.getYearlyResultsForStudentPeriodAndSchool(schoolId, periodId, studentId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/save-yearly-results-for-student")
    suspend fun saveYearlyResultsForStudent(
        @RequestBody studentToYearlyResult: StudentToYearlyResult,
    ) {
        yearlyResultsService.saveUpdateYearlyResultsForStudent(studentToYearlyResult)
    }

}