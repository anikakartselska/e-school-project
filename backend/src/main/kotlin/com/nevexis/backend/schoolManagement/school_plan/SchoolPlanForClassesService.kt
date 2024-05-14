package com.nevexis.backend.schoolManagement.school_plan

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_calendar.CalendarService
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_schedule.SubjectAndClassesCount
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPlanForClassesRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PLAN_FOR_CLASSES
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolPlanForClassesService : BaseService() {

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var calendarService: CalendarService
    fun getAllSchoolPlansForSchool(schoolId: BigDecimal): List<SchoolPlanForClasses> {
        return db.selectFrom(SCHOOL_PLAN_FOR_CLASSES)
            .where(SCHOOL_PLAN_FOR_CLASSES.SCHOOL_ID.eq(schoolId))
            .fetch()
            .map {
                SchoolPlanForClasses(
                    id = it.id!!.toInt(),
                    name = it.name!!,
                    subjectAndClassesCount = emptyList(),
                    schoolClassesWithTheSchoolPlan = schoolClassService.getSchoolClassesWithSchoolPlanId(it.id!!)
                )
            }
    }

    fun fetchPlanForSchoolClass(schoolClass: SchoolClass): SchoolPlanForClasses? {
        val calendar =
            calendarService.getSchoolCalendarForSchoolAndPeriod(
                schoolClass.schoolId.toBigDecimal(),
                schoolClass.schoolPeriodId.toBigDecimal()
            )
        return db.select(SCHOOL_CLASS.PLAN_ID, SCHOOL_PLAN_FOR_CLASSES.asterisk())
            .from(SCHOOL_PLAN_FOR_CLASSES)
            .leftJoin(SCHOOL_CLASS).on(SCHOOL_CLASS.PLAN_ID.eq(SCHOOL_PLAN_FOR_CLASSES.ID))
            .where(SCHOOL_CLASS.ID.eq(schoolClass.id?.toBigDecimal()))
            .fetchAnyInto(SchoolPlanForClassesRecord::class.java)?.let {
                SchoolPlanForClasses(
                    id = it.id!!.toInt(),
                    name = it.name!!,
                    subjectAndClassesCount = Json.decodeFromString<Map<String, Int>>(it.plan!!)
                        .map { (subject, classesCount) ->

                            SubjectAndClassesCount(
                                subject,
                                classesCount,
                                classesCount * (calendar?.let {
                                    (it.firstSemesterWeeksCount
                                        ?: 0) + (it.classToSecondSemesterWeeksCount[schoolClass.name.dropLast(
                                        1
                                    ).toInt()] ?: 0)
                                } ?: 0)
                            )
                        },
                    schoolClassesWithTheSchoolPlan = schoolClassService.getSchoolClassesWithSchoolPlanId(it.id!!)
                )
            }
    }

    fun getPlanById(
        planForClassesId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): SchoolPlanForClasses {
        return db.select(SCHOOL_PLAN_FOR_CLASSES.asterisk())
            .from(SCHOOL_PLAN_FOR_CLASSES)
            .where(SCHOOL_PLAN_FOR_CLASSES.ID.eq(planForClassesId))
            .fetchAnyInto(SchoolPlanForClassesRecord::class.java)?.let {
                SchoolPlanForClasses(
                    id = it.id!!.toInt(),
                    name = it.name!!,
                    subjectAndClassesCount = (Json.decodeFromString<Map<String, Int>>(it.plan!!)).map { (subject, classesCount) ->
                        SubjectAndClassesCount(
                            subject,
                            classesCount,
                            0
                        )
                    },
                    schoolClassesWithTheSchoolPlan = schoolClassService.getSchoolClassesWithSchoolPlanId(it.id!!)
                )
            } ?: throw SMSError(
            "Данните не са намерени",
            "Училищният план, който търсите не съществува или е бил изтрит"
        )
    }

    fun saveUpdateSchoolPlansForClasses(
        schoolPlanForClasses: SchoolPlanForClasses,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): SchoolPlanForClasses {
        val id = schoolPlanForClasses.id?.toBigDecimal() ?: getSchoolPlansForClassesSeqNextVal()
        return db.transactionResult { transaction ->
            (transaction.dsl().selectFrom(SCHOOL_PLAN_FOR_CLASSES)
                .where(SCHOOL_PLAN_FOR_CLASSES.ID.eq(schoolPlanForClasses.id?.toBigDecimal())).fetchAny()
                ?: transaction.dsl().newRecord(SCHOOL_PLAN_FOR_CLASSES)).apply {
                this.id = id
                plan = Json.encodeToString(schoolPlanForClasses.subjectAndClassesCount.associate {
                    it.subjectName to it.classesPerWeek
                })
                name = schoolPlanForClasses.name
                this.schoolId = schoolId
            }.store()

            schoolPlanForClasses.copy(id = id.toInt())
                .also {
                    schoolClassService.updateSchoolClassesProgram(
                        it, schoolId, periodId, transaction.dsl()
                    )
                }
        }
    }

    fun deleteSchoolPlansForClasses(
        schoolPlanForClasses: SchoolPlanForClasses,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) {
        db.transaction { transaction ->
            schoolPlanForClasses.copy(schoolClassesWithTheSchoolPlan = emptyList())
                .also {
                    schoolClassService.updateSchoolClassesProgram(
                        it, schoolId, periodId, transaction.dsl()
                    )
                }

            transaction.dsl().selectFrom(SCHOOL_PLAN_FOR_CLASSES)
                .where(SCHOOL_PLAN_FOR_CLASSES.ID.eq(schoolPlanForClasses.id?.toBigDecimal())).fetchAny()?.delete()

        }
    }

    fun getSchoolPlansForClassesSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_PLAN_FOR_CLASSES_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}