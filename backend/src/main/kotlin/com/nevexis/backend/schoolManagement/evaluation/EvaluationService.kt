package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.compareToByMatchingPk
import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.EvaluationRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.EVALUATION
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS_SUBJECT
import com.nevexis.`demo-project`.jooq.tables.references.USER
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class EvaluationService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired private lateinit var evaluationNotificationService: EvaluationNotificationService

    @Autowired
    private lateinit var subjectService: SubjectService

    fun getAllStudentSubjectEvaluationsFromSchoolClass(
        schoolClass: SchoolClass,
        evaluationType: EvaluationType
    ): Map<Int, Map<Int, List<Evaluation>>> {
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
        val evaluationsMap = records.groupBy { it.get(EVALUATION.SUBJECT_ID)!!.toInt() }
            .mapValues { (_, values) -> values.groupBy { it.get(EVALUATION.USER_ID)?.toInt() } }
        return subjects.associate { subject ->
            subject.id to students.associate { student ->
                student.id to (evaluationsMap[subject.id]?.get(student.id)?.map { record ->
                    mapToEvaluationModel(record, student, subject)
                } ?: emptyList())
            }
        }
    }

    fun getAllEvaluationsForSubjectAnSchoolClass(
        subjectId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolClassId: BigDecimal
    ): List<StudentWithEvaluationDTO> {
        val subject = subjectService.getSubjectsById(
            subjectId,
            periodId,
            schoolId
        ) ?: error(
            SMSError(
                "NOT_FOUND",
                "Subject with id $subjectId does not exist"
            )
        )
        val students = userService.getAllStudentsInSchoolClass(
            schoolClassId = schoolClassId,
            periodId = periodId
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
                    .and(EVALUATION.SUBJECT_ID.eq(subject.id.toBigDecimal()))
            ).fetch()

        val evaluationsMap = records.groupBy { it.get(EVALUATION.USER_ID)!! }
            .mapValues { (_, values) -> values.groupBy { it.get(EVALUATION.EVALUATION_TYPE) } }

        return students.map { student ->
            (evaluationsMap[student.id.toBigDecimal()] ?: emptyMap()).let { evaluationTypeToEvaluationsMap ->
                StudentWithEvaluationDTO(
                    student,
                    evaluationTypeToEvaluationsMap[EvaluationType.ABSENCE.name]?.map {
                        mapToEvaluationModel(it, student, subject)
                    } ?: emptyList(),
                    evaluationTypeToEvaluationsMap[EvaluationType.GRADE.name]?.map {
                        mapToEvaluationModel(it, student, subject)
                    } ?: emptyList(),
                    evaluationTypeToEvaluationsMap[EvaluationType.FEEDBACK.name]?.map {
                        mapToEvaluationModel(it, student, subject)
                    } ?: emptyList(),
                )
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
                    .and(EVALUATION.USER_ID.eq(studentView.id.toBigDecimal()))
            ).fetch()

        val evaluationsMap = records.groupBy { it.get(EVALUATION.SUBJECT_ID)!!.toInt() }
            .mapValues { (_, values) -> values.groupBy { it.get(EVALUATION.EVALUATION_TYPE) } }

        return subjects.map { subject ->
            (evaluationsMap[subject.id] ?: emptyMap()).let { evaluationTypeToEvaluationsMap ->
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
            id = it.id!!.toInt(),
            student = student,
            subject = subject,
            schoolLessonId = it.schoolLessonId?.toInt(),
            evaluationDate = it.evaluationDate!!,
            evaluationType = EvaluationType.valueOf(it.evaluationType!!),
            evaluationValue = Json.decodeFromString(it.evaluationValue!!),
            semester = Semester.valueOf(it.semester!!),
            createdBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            comment = it.comment
        )
    }

    fun updateEvaluations(
        evaluations: List<StudentWithEvaluationDTO>,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) {
        val allEvaluations = evaluations.map { evaluationDto ->
            evaluationDto.grades.plus(evaluationDto.feedbacks).plus(evaluationDto.absences)
        }.flatten()

        val evaluationRecordToEvaluation = allEvaluations.mapNotNull { evaluation ->
            val updatedEvaluationRecord = mapToEvaluationToEvaluationRecord(evaluation, schoolId, periodId)
            val evaluationRecordFromDatabase =
                db.selectFrom(EVALUATION).where(EVALUATION.ID.eq(evaluation.id?.toBigDecimal())).fetchAny()!!
            if (updatedEvaluationRecord.compareToByMatchingPk(evaluationRecordFromDatabase) == 0) {
                null
            } else {
                updatedEvaluationRecord to Pair(evaluation, evaluationRecordFromDatabase)
            }
        }
        val evaluationRecords = evaluationRecordToEvaluation.map { it.first }
        db.batchUpdate(evaluationRecords).execute().also {
            evaluationNotificationService.sendEmailForEvaluationsUpdate(
                evaluationRecordToEvaluation.map { it.second },
                periodId,
                schoolId
            )
        }
    }


    fun saveEvaluations(
        evaluations: List<StudentWithEvaluationDTO>,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<StudentWithEvaluationDTO> {
        val evaluationsWithId = evaluations.map { evaluationDto ->
            evaluationDto.copy(
                absences = evaluationDto.absences.mapNotNull {
                    if (it.id == null) {
                        it.copy(id = getEvaluationSeqNextVal().toInt(), evaluationDate = LocalDateTime.now())
                    } else null
                },
                grades = evaluationDto.grades.mapNotNull {
                    if (it.id == null) {
                        it.copy(id = getEvaluationSeqNextVal().toInt(), evaluationDate = LocalDateTime.now())
                    } else null
                },
                feedbacks = evaluationDto.feedbacks.mapNotNull {
                    if (it.id == null) {
                        it.copy(id = getEvaluationSeqNextVal().toInt(), evaluationDate = LocalDateTime.now())
                    } else null
                },
            )

        }

        val evaluationRecords = evaluationsWithId.map { evaluationDto ->
            mapToEvaluationsToEvaluationRecords(
                evaluationDto.grades.plus(evaluationDto.feedbacks).plus(evaluationDto.absences),
                schoolId,
                periodId
            )
        }.flatten()
        db.batchInsert(evaluationRecords).execute().also {
            evaluationNotificationService.sendEmailForEvaluationsCreation(evaluationsWithId.map {
                it.absences + it.feedbacks + it.grades
            }.flatten(), periodId, schoolId)
        }
        return evaluationsWithId
    }


    private fun mapToEvaluationsToEvaluationRecords(
        evaluations: List<Evaluation>,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = evaluations.map { evaluation ->
        mapToEvaluationToEvaluationRecord(evaluation, schoolId, periodId)
    }


    private fun mapToEvaluationToEvaluationRecord(
        evaluation: Evaluation,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = db.newRecord(EVALUATION, evaluation).apply {
        id = evaluation.id?.toBigDecimal()
        subjectId = evaluation.subject.id.toBigDecimal()
        schoolLessonId = evaluation.schoolLessonId?.toBigDecimal()
        evaluationDate = evaluation.evaluationDate ?: LocalDateTime.now()
        evaluationType = evaluation.evaluationType.name
        evaluationValue = Json.encodeToString(evaluation.evaluationValue)
        this.schoolId = schoolId
        schoolPeriodId = periodId
        userId = evaluation.student.id.toBigDecimal()
        semester = evaluation.semester.name
        createdBy = evaluation.createdBy.id.toBigDecimal()
        comment = evaluation.comment
    }


    fun getEvaluationSeqNextVal(): BigDecimal =
        db.select(DSL.field("EVALUATION_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}



