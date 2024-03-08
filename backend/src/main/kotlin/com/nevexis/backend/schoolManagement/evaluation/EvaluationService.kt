package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.records.EvaluationRecord
import com.nevexis.`demo-project`.jooq.tables.references.EVALUATION
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EvaluationService : BaseService() {

    fun getAllEvaluationsForStudentAndPeriod(studentRoleId: BigDecimal, periodId: BigDecimal, schoolId: BigDecimal) =
        db.selectFrom(EVALUATION).where(
            EVALUATION.STUDENT_ROLE_ID.eq(studentRoleId).and(EVALUATION.SCHOOL_PERIOD_ID.eq(periodId)).and(
                EVALUATION.SCHOOL_ID.eq(schoolId)
            )
        ).fetch().map { it.mapToInternalModel() }

    fun getAllEvaluationsForSubject(
        subjectId: BigDecimal,
        schoolClassId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) = db.selectFrom(EVALUATION).where(
        EVALUATION.STUDENT_ROLE_ID.`in`(
            db.select(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID).from(STUDENT_SCHOOL_CLASS).where(
                STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId)
            )
        ).and(EVALUATION.SUBJECT_ID.eq(subjectId))
            .and(EVALUATION.SCHOOL_PERIOD_ID.eq(periodId)).and(
                EVALUATION.SCHOOL_ID.eq(schoolId)
            )
    ).fetch().map { it.mapToInternalModel() }


    fun EvaluationRecord.mapToInternalModel() = Evaluation(
        id = id!!,
        studentRoleId = studentRoleId!!,
        subjectId = subjectId!!,
        schoolLessonId = schoolLessonId!!,
        evaluationDate = evaluationDate!!,
        evaluationType = EvaluationType.valueOf(evaluationType!!),
        evaluationValue = Json.decodeFromString(evaluationValue!!),
        schoolId = schoolId!!,
        schoolPeriodId = schoolPeriodId!!,
    )

    fun Evaluation.mapToRecord() = EvaluationRecord(
        id = id,
        studentRoleId = studentRoleId,
        subjectId = subjectId,
        schoolLessonId = schoolLessonId,
        evaluationDate = evaluationDate,
        evaluationType = evaluationType.name,
        evaluationValue = Json.encodeToString(evaluationValue),
        schoolId = schoolId,
        schoolPeriodId = schoolPeriodId
    )


}