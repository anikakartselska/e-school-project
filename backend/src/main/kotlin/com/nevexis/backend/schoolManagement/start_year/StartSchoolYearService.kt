package com.nevexis.backend.schoolManagement.start_year

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriodService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class StartSchoolYearService : BaseService() {

    @Autowired
    private lateinit var schoolPeriodService: SchoolPeriodService

    fun startSchoolYear(newSchoolPeriod: SchoolPeriod, schoolId: BigDecimal) {
        db.transaction { transaction ->
            val newSchoolPeriodFromDatabase = schoolPeriodService.saveSchoolPeriod(newSchoolPeriod, transaction.dsl())
            schoolPeriodService.createSchoolPeriodSchoolRecord(
                schoolId,
                newSchoolPeriodFromDatabase.id!!.toBigDecimal(),
                transaction.dsl()
            )

            schoolPeriodService.getPreviousPeriod(newSchoolPeriodFromDatabase, transaction.dsl())
                ?.also { previousPeriod ->
                    copyTablesFromPreviousPeriodToNewPeriod(
                        schoolId,
                        newSchoolPeriodFromDatabase.id.toBigDecimal(),
                        previousPeriod.id!!.toBigDecimal(),
                        transaction.dsl()
                    )
                }
        }

    }

    fun copyTablesFromPreviousPeriodToNewPeriod(
        schoolId: BigDecimal,
        newPeriodId: BigDecimal,
        oldPeriodId: BigDecimal,
        dslContext: DSLContext = db
    ) {
        dslContext.transaction { transaction ->
            copySchoolUserPeriodFromPreviousPeriodToNewPeriod(oldPeriodId, schoolId, newPeriodId, transaction.dsl())
            copySchoolRolePeriodFromPreviousPeriodToNewPeriod(oldPeriodId, schoolId, newPeriodId, transaction.dsl())
            val oldSchoolClassToNewSchoolClassMap =
                copySchoolClassFromPreviousPeriodToNewPeriod(oldPeriodId, schoolId, newPeriodId, transaction.dsl())
            copyStudentSchoolClassFromPreviousPeriodToNewPeriod(oldSchoolClassToNewSchoolClassMap, transaction.dsl())
        }
    }


    private fun copyStudentSchoolClassFromPreviousPeriodToNewPeriod(
        oldSchoolClassToNewSchoolClass: Map<SchoolClassRecord, SchoolClassRecord>,
        dslContext: DSLContext = db
    ) {
        val oldSchoolClassIdsToNewSchoolClass =
            oldSchoolClassToNewSchoolClass.mapKeys { (oldSchoolClass, _) -> oldSchoolClass.id }
        dslContext.select(
            STUDENT_SCHOOL_CLASS.asterisk()
        ).from(STUDENT_SCHOOL_CLASS)
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
            .leftJoin(SCHOOL_ROLE_PERIOD)
            .on(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
            .leftJoin(SCHOOL_USER)
            .on(SCHOOL_USER.USER_ID.eq(SCHOOL_USER_ROLE.USER_ID))
            .leftJoin(SCHOOL_USER_PERIOD)
            .on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
            .where(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.`in`(oldSchoolClassIdsToNewSchoolClass.keys))
            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .fetch()
            .map { oldStudentSchoolClassRecord ->
                oldStudentSchoolClassRecord to dslContext.newRecord(STUDENT_SCHOOL_CLASS, oldStudentSchoolClassRecord)
                    .apply {
                        this.id = getStudentSchoolClassSeqNextVal()
                        this.schoolClassId = oldSchoolClassIdsToNewSchoolClass[oldStudentSchoolClassRecord.get(
                            STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID, BigDecimal::class.java
                        )]?.id
                    }
            }.toMap()
            .also {
                dslContext.batchInsert(it.values).execute()
            }

    }

    private fun copySchoolClassFromPreviousPeriodToNewPeriod(
        oldPeriodId: BigDecimal,
        schoolId: BigDecimal,
        newPeriodId: BigDecimal,
        dslContext: DSLContext = db
    ): Map<SchoolClassRecord, SchoolClassRecord> {
        return dslContext.selectFrom(SCHOOL_CLASS)
            .where(
                SCHOOL_CLASS.SCHOOL_ID.eq(schoolId)
                    .and(SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(oldPeriodId))
                    .andNot(SCHOOL_CLASS.NAME.startsWith("12"))
            ).fetch()
            .map {
                it to dslContext.newRecord(SCHOOL_CLASS, it)
                    .apply {
                        this.id = getSchoolClassSeqNextVal()
                        this.schoolPeriodId = newPeriodId
                        this.planId = null
                        this.name = (it.name?.dropLast(1)!!.toInt() + 1).toString().plus(it.name!!.last())
                    }
            }.toMap()
            .also {
                dslContext.batchInsert(it.keys).execute()
            }

    }


    private fun copySchoolRolePeriodFromPreviousPeriodToNewPeriod(
        oldPeriodId: BigDecimal,
        schoolId: BigDecimal,
        newPeriodId: BigDecimal,
        dslContext: DSLContext = db
    ) {
        dslContext.select(SCHOOL_ROLE_PERIOD.asterisk()).from(SCHOOL_ROLE_PERIOD)
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
            .where(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(oldPeriodId))
            .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .and(SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId))
            .fetch()
            .map {
                dslContext.newRecord(SCHOOL_ROLE_PERIOD, it)
                    .apply {
                        this.id = getSchoolUserRolePeriodSeqNextVal()
                        this.periodId = newPeriodId
                    }
            }.also {
                dslContext.batchInsert(it).execute()
            }
    }

    private fun copySchoolUserPeriodFromPreviousPeriodToNewPeriod(
        oldPeriodId: BigDecimal,
        schoolId: BigDecimal,
        newPeriodId: BigDecimal,
        dslContext: DSLContext = db
    ) {
        dslContext.select(SCHOOL_USER_PERIOD.asterisk()).from(SCHOOL_USER_PERIOD)
            .leftJoin(SCHOOL_USER)
            .on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
            .where(SCHOOL_USER_PERIOD.PERIOD_ID.eq(oldPeriodId))
            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .and(SCHOOL_USER.SCHOOL_ID.eq(schoolId))
            .fetch()
            .map {
                dslContext.newRecord(SCHOOL_USER_PERIOD, it)
                    .apply {
                        this.id = getSchoolUserPeriodSeqNextVal()
                        this.periodId = newPeriodId
                    }
            }.also {
                dslContext.batchInsert(it).execute()
            }
    }

    fun getSchoolUserRolePeriodSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_ROLE_PERIOD_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolClassSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_CLASS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolUserPeriodSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_PERIOD_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getStudentSchoolClassSeqNextVal(): BigDecimal =
        db.select(DSL.field("STUDENT_SCHOOL_CLASS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}