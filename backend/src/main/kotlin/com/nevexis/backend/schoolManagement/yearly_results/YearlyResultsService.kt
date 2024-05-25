package com.nevexis.backend.schoolManagement.yearly_results

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.StudentSchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_ROLE
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class YearlyResultsService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    fun getAllYearlyResultsForSchoolClassPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal,
    ): List<StudentToYearlyResult> {
        val students = userService.getAllStudentsInSchoolClass(
            schoolClassId = schoolClassId,
            periodId = periodId
        )
        val yearlyResults =
            db.select(SCHOOL_USER_ROLE.USER_ID, STUDENT_SCHOOL_CLASS.YEARLY_RESULTS).from(STUDENT_SCHOOL_CLASS)
                .leftJoin(SCHOOL_USER_ROLE)
                .on(SCHOOL_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
                .where(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId))
                .fetch()
                .associate { record ->
                    record.get(
                        SCHOOL_USER_ROLE.USER_ID,
                        BigDecimal::class.java
                    ) to record.get(STUDENT_SCHOOL_CLASS.YEARLY_RESULTS, String::class.java)?.let {
                        Json.decodeFromString<YearlyResults>(it)
                    }
                }
        return students.map {
            StudentToYearlyResult(it, yearlyResults[it.id.toBigDecimal()])
        }
    }

    fun getYearlyResultsForStudentPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        studentId: BigDecimal,
    ): YearlyResults? {
        return db.select(STUDENT_SCHOOL_CLASS.YEARLY_RESULTS).from(STUDENT_SCHOOL_CLASS)
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
            .where(SCHOOL_USER_ROLE.USER_ID.eq(studentId))
            .fetchAny()?.let { record ->
                record.get(STUDENT_SCHOOL_CLASS.YEARLY_RESULTS, String::class.java)?.let {
                    Json.decodeFromString<YearlyResults>(it)
                }
            }
    }

    fun saveUpdateYearlyResultsForStudent(studentToYearlyResult: StudentToYearlyResult) {
        db.select(STUDENT_SCHOOL_CLASS.asterisk()).from(STUDENT_SCHOOL_CLASS)
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
            .where(SCHOOL_USER_ROLE.USER_ID.eq(studentToYearlyResult.studentView.id.toBigDecimal()))
            .fetchAnyInto(StudentSchoolClassRecord::class.java)?.let { record ->
                record.apply {
                    this.yearlyResults = studentToYearlyResult.yearlyResults?.let { Json.encodeToString(it) }
                }
            }?.update()
    }

}