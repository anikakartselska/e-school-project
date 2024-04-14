package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.subject.SubjectWithEvaluationDTO
import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.EvaluationRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.EVALUATION
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS_SUBJECT
import com.nevexis.`demo-project`.jooq.tables.references.USER
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EvaluationService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var subjectService: SubjectService

    fun getAllStudentSubjectEvaluationsFromSchoolClass(
        schoolClass: SchoolClass,
        evaluationType: EvaluationType
    ): Map<BigDecimal, Map<BigDecimal, List<Evaluation>>> {
        val students = userService.getAllStudentsInSchoolClass(
            schoolClassId = schoolClass.id!!.toBigDecimal(),
            periodId = schoolClass.schoolPeriodId.toBigDecimal()
        )

        val subjects = subjectService.getAllSubjectsBySchoolClassId(
            schoolClassId = schoolClass.id.toBigDecimal(),
            periodId = schoolClass.schoolPeriodId.toBigDecimal(), schoolId = schoolClass.schoolId.toBigDecimal()
        )
        val records = db.select(
            EVALUATION.asterisk(),
            USER.asterisk()
        )
            .from(EVALUATION)
            .leftJoin(SCHOOL_CLASS_SUBJECT)
            .on(SCHOOL_CLASS_SUBJECT.SUBJECT_ID.eq(EVALUATION.SUBJECT_ID))
            .leftJoin(USER)
            .on(USER.ID.eq(EVALUATION.CREATED_BY))
            .where(
                EVALUATION.SCHOOL_PERIOD_ID.eq(schoolClass.schoolPeriodId.toBigDecimal())
                    .and(EVALUATION.SCHOOL_ID.eq(schoolClass.schoolId.toBigDecimal()))
                    .and(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(schoolClass.id.toBigDecimal()))
                    .and(EVALUATION.EVALUATION_TYPE.eq(evaluationType.name))
            ).fetch()
        val evaluationsMap = records.groupBy { it.get(EVALUATION.SUBJECT_ID)!! }
            .mapValues { (_, values) -> values.groupBy { it.get(EVALUATION.USER_ID) } }
        return subjects.associate { subject ->
            subject.id to students.associate { student ->
                student.id to (evaluationsMap[subject.id]?.get(student.id)?.map { record ->
                    mapToEvaluationModel(record, student, subject)
                } ?: emptyList())
            }
        }
    }

    fun getAllEvaluationsForStudent(
        studentId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolClassId: BigDecimal
    ): List<SubjectWithEvaluationDTO> {
        val studentView = userService.getStudentByIdAndSchoolClass(studentId, schoolClassId, periodId) ?: error(
            SMSError(
                "NOT_FOUND",
                "STUDENT with id $studentId does not exist or is not approved yet"
            )
        )
        val subjects = subjectService.getAllSubjectsBySchoolClassId(
            schoolClassId = schoolClassId,
            periodId = periodId, schoolId = schoolId
        )
        val records = db.select(
            EVALUATION.asterisk(),
            USER.asterisk()
        )
            .from(EVALUATION)
            .leftJoin(SCHOOL_CLASS_SUBJECT)
            .on(SCHOOL_CLASS_SUBJECT.SUBJECT_ID.eq(EVALUATION.SUBJECT_ID))
            .leftJoin(USER)
            .on(USER.ID.eq(EVALUATION.CREATED_BY))
            .where(
                EVALUATION.SCHOOL_PERIOD_ID.eq(periodId)
                    .and(EVALUATION.SCHOOL_ID.eq(schoolId))
                    .and(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(schoolClassId))
                    .and(EVALUATION.USER_ID.eq(studentView.id))
            ).fetch()

        val evaluationsMap = records.groupBy { it.get(EVALUATION.SUBJECT_ID)!! }
            .mapValues { (_, values) -> values.groupBy { it.get(EVALUATION.EVALUATION_TYPE) } }

        return subjects.mapNotNull { subject ->
            evaluationsMap[subject.id]?.let { evaluationTypeToEvaluationsMap ->
                SubjectWithEvaluationDTO(
                    subject,
                    evaluationTypeToEvaluationsMap[EvaluationType.ABSENCE.name]?.map {
                        mapToEvaluationModel(it, studentView, subject)
                    } ?: emptyList(),
                    evaluationTypeToEvaluationsMap[EvaluationType.GRADE.name]?.map {
                        mapToEvaluationModel(it, studentView, subject)
                    } ?: emptyList(),
                    evaluationTypeToEvaluationsMap[EvaluationType.FEEDBACK.name]?.map {
                        mapToEvaluationModel(it, studentView, subject)
                    } ?: emptyList(),
                )
            }
        }
    }

    private fun mapToEvaluationModel(
        record: Record,
        student: StudentView,
        subject: Subject,
    ) = record.into(EvaluationRecord::class.java).let {
        Evaluation(
            id = it.id!!,
            student = student,
            subject = subject,
            schoolLessonId = it.schoolLessonId!!,
            evaluationDate = it.evaluationDate!!,
            evaluationType = EvaluationType.valueOf(it.evaluationType!!),
            evaluationValue = Json.decodeFromString(it.evaluationValue!!),
            semester = Semester.valueOf(it.semester!!),
            createdBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList())
        )
    }
}

//    fun getAllEvaluationsForSubject(
//        subjectId: BigDecimal,
//        schoolClassId: BigDecimal,
//        periodId: BigDecimal,
//        schoolId: BigDecimal
//    ) = db.selectFrom(EVALUATION).where(
//        EVALUATION.STUDENT_ROLE_ID.`in`(
//            db.select(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID).from(STUDENT_SCHOOL_CLASS).where(
//                STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId)
//            )
//        ).and(EVALUATION.SUBJECT_ID.eq(subjectId))
//            .and(EVALUATION.SCHOOL_PERIOD_ID.eq(periodId)).and(
//                EVALUATION.SCHOOL_ID.eq(schoolId)
//            )
//    ).fetch().map { it.mapToInternalModel() }


