package com.nevexis.backend.schoolManagement.statistics

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.assignments.AssignmentType
import com.nevexis.backend.schoolManagement.assignments.AssignmentsService
import com.nevexis.backend.schoolManagement.evaluation.EvaluationService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.users.DetailsForUser
import com.nevexis.backend.schoolManagement.users.OneRoleUser
import com.nevexis.backend.schoolManagement.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class StatisticsService {

    @Autowired
    private lateinit var assignmentService: AssignmentsService

    @Autowired
    private lateinit var evaluationService: EvaluationService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    fun getStatisticsForSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): SchoolStatistics {
        val evaluationsCount = evaluationService.getEvaluationsCountForSchool(schoolId, periodId)
        val schoolClasses = schoolClassService.getAllSchoolClassesFromSchoolAndPeriod(schoolId, periodId)

        val allStudentsFromSchool = userService.getAllStudentsFromSchool(schoolId, periodId)

        val schoolClassIdToAverageGrade =
            evaluationService.calculateAverageGradeForAllSchoolClasses(schoolId, periodId).toMap()
        val studentIdToAverageGrade = evaluationService.calculateAverageGradeForAllStudents(schoolId, periodId).toMap()

        val schoolClassToAverageGrade = schoolClasses.map {
            it to schoolClassIdToAverageGrade[it.id!!.toBigDecimal()]
        }.sortedByDescending { it.second }

        val studentToToAverageGrade = allStudentsFromSchool.map {
            it to studentIdToAverageGrade[it.id.toBigDecimal()]
        }.sortedByDescending { it.second }


        return SchoolStatistics(
            success = evaluationService.calculateAverageGradeForSchoolAndPeriod(
                schoolId,
                periodId
            ),
            grades = evaluationsCount.gradesCount,
            absences = evaluationsCount.absencesCount,
            feedback = evaluationsCount.feedbackCount,
            examinations = assignmentService.getAssignmentsCountForPeriodAndSchool(
                schoolId,
                periodId,
                AssignmentType.EXAMINATION
            ),
            events = assignmentService.getAssignmentsCountForPeriodAndSchool(
                schoolId,
                periodId,
                AssignmentType.EVENT
            ),
            schoolClassToAverageGrade = schoolClassToAverageGrade,
            studentToAverageGrade = studentToToAverageGrade
        )
    }

    fun getStatisticsForStudent(
        student: OneRoleUser,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): StudentStatistics {
        val evaluationsCount =
            evaluationService.getEvaluationsCountForStudent(student.id!!.toBigDecimal(), schoolId, periodId)
        val schoolClass = when (student.role.detailsForUser!!) {
            is DetailsForUser.DetailsForParent -> throw SMSError(
                "Потребителят няма правилната роля",
                "Ролята на потребителят се очаква да бъде УЧЕНИК, а е РОДИТЕЛ"
            )

            is DetailsForUser.DetailsForTeacher -> throw SMSError(
                "Потребителят няма правилната роля",
                "Ролята на потребителят се очаква да бъде УЧЕНИК, а е УЧИТЕЛ"
            )

            is DetailsForUser.DetailsForStudent -> (student.role.detailsForUser as DetailsForUser.DetailsForStudent).schoolClass

        }
        return StudentStatistics(
            success = evaluationService.calculateAverageGradeForStudentSchoolAndPeriod(
                student.id.toBigDecimal(),
                schoolId,
                periodId
            ),
            grades = evaluationsCount.gradesCount,
            absences = evaluationsCount.absencesCount,
            feedback = evaluationsCount.feedbackCount,
            examinations = assignmentService.getAssignmentsCountForSchoolClassPeriodAndSchool(
                schoolId,
                periodId,
                schoolClass.id!!.toBigDecimal(),
                AssignmentType.EXAMINATION
            ),
            events = assignmentService.getAssignmentsCountForSchoolClassPeriodAndSchool(
                schoolId,
                periodId,
                schoolClass.id.toBigDecimal(),
                AssignmentType.EVENT
            ),
            placeInClass = evaluationService.calculateStudentsPlaceInSchoolClass(
                schoolClass.id.toBigDecimal(),
                student.id.toBigDecimal(),
                schoolId,
                periodId
            ),
            placeInGraduationClass = evaluationService.calculateStudentsPlaceInGraduationClass(
                schoolClass, student.id.toBigDecimal(),
                schoolId,
                periodId
            ),
            placeInSchool = evaluationService.calculateStudentsPlaceInSchool(
                student.id.toBigDecimal(),
                schoolId,
                periodId
            )
        )
    }
}