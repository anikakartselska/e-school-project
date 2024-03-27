package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.evaluation.EvaluationService
import com.nevexis.backend.schoolManagement.evaluation.EvaluationType
import com.nevexis.backend.schoolManagement.evaluation.StudentWithEvaluationDTO
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.SubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS_SUBJECT
import com.nevexis.`demo-project`.jooq.tables.references.SUBJECT
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SubjectService : BaseService() {

    @Autowired
    private lateinit var evaluationService: EvaluationService

    @Autowired
    private lateinit var userService: UserService

    fun getAllStudentSubjectsAndTheirEvaluations(
        schoolClassId: BigDecimal,
        studentRoleId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<SubjectWithEvaluationDTO> {
        val studentEvaluations =
            evaluationService.getAllEvaluationsForStudentAndPeriod(studentRoleId, periodId, schoolId)
                .groupBy { it.subjectId }
                .mapValues { (_, evaluations) ->
                    evaluations.groupBy { it.evaluationType }
                }

        return getAllSubjectsBySchoolClassId(schoolClassId, periodId, schoolId).map { subject ->
            SubjectWithEvaluationDTO(
                subject,
                studentEvaluations[subject.id]?.get(EvaluationType.ABSENCE) ?: emptyList(),
                studentEvaluations[subject.id]?.get(EvaluationType.GRADE) ?: emptyList(),
                studentEvaluations[subject.id]?.get(EvaluationType.FEEDBACK) ?: emptyList(),
            )
        }
    }

    fun getSubjectFromSchoolClassAndItsEvaluations(
        subjectId: BigDecimal,
        schoolClassId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        dsl: DSLContext = db
    ): List<StudentWithEvaluationDTO> {
        val studentsFromClassMap = userService.getAllStudentsInSchoolClass(schoolClassId, periodId)

        val studentIdToEvaluations =
            evaluationService.getAllEvaluationsForSubject(subjectId, schoolClassId, periodId, schoolId).groupBy {
                it.studentRoleId
            }

        return studentsFromClassMap.map { student ->
            val studentEvaluationsForCurrentSubject =
                studentIdToEvaluations[student.id]?.groupBy { it.evaluationType } ?: emptyMap()

            StudentWithEvaluationDTO(
                student = student,
                absences = studentEvaluationsForCurrentSubject[EvaluationType.ABSENCE] ?: emptyList(),
                grades = studentEvaluationsForCurrentSubject[EvaluationType.GRADE] ?: emptyList(),
                feedbacks = studentEvaluationsForCurrentSubject[EvaluationType.FEEDBACK] ?: emptyList()
            )
        }
    }

    fun getAllSubjectsTaughtByTeacher(
        teacherId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<SubjectWithSchoolClassInformation> =
        db.select(SUBJECT.asterisk(), SCHOOL_CLASS_SUBJECT.asterisk(), SCHOOL_CLASS.NAME.`as`("SCHOOL_CLASS_NAME"))
            .from(SUBJECT)
            .leftJoin(SCHOOL_CLASS_SUBJECT).on(SUBJECT.ID.eq(SCHOOL_CLASS_SUBJECT.SUBJECT_ID))
            .leftJoin(SCHOOL_CLASS).on(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(SCHOOL_CLASS.ID))
            .fetch().map { record ->
                record.into(SubjectRecord::class.java)
                    .into(SubjectWithSchoolClassInformation::class.java).copy(
                        schoolClass = record.get(DSL.field("SCHOOL_CLASS_NAME", String::class.java))
                    )
            }


    fun getAllSubjectsBySchoolClassId(
        schoolClassId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<Subject> =
        db.select(SUBJECT.asterisk(), USER.asterisk()).from(SUBJECT)
            .leftJoin(SCHOOL_CLASS_SUBJECT).on(SUBJECT.ID.eq(SCHOOL_CLASS_SUBJECT.SUBJECT_ID))
            .leftJoin(USER).on(SUBJECT.TEACHER_ID.eq(USER.ID))
            .where(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(schoolClassId))
            .and(SUBJECT.SCHOOL_ID.eq(schoolId))
            .and(SUBJECT.SCHOOL_PERIOD_ID.eq(periodId))
            .fetch()
            .map {
                mapToInternalModel(it)
            }


    fun mapToInternalModel(record: Record) = record.into(SubjectRecord::class.java).into(Subject::class.java).copy(
        teacher = userService.mapToUserView(record, listOf(SchoolRole.TEACHER))
    )
}