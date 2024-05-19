package com.nevexis.backend.schoolManagement.school_period

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodSchoolRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD_SCHOOL
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolPeriodService : BaseService() {

    fun fetchAllSchoolPeriods(): List<SchoolPeriod> =
        db.selectFrom(SCHOOL_PERIOD).fetch().map { it.mapSchoolPeriodRecordToSchoolPeriod() }

    fun checkIfSchoolPeriodIsStartedInSchool(schoolPeriod: SchoolPeriod, schoolId: BigDecimal) =
        db.selectCount().from(SCHOOL_PERIOD_SCHOOL).leftJoin(SCHOOL_PERIOD)
            .on(SCHOOL_PERIOD_SCHOOL.SCHOOL_PERIOD_ID.eq(SCHOOL_PERIOD.ID))
            .where(SCHOOL_PERIOD_SCHOOL.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_PERIOD.START_YEAR.eq(schoolPeriod.startYear.toBigDecimal()))
            .and(SCHOOL_PERIOD.END_YEAR.eq(schoolPeriod.endYear.toBigDecimal()))
            .fetchAnyInto(Int::class.java)?.let { it > 0 } ?: false

    fun saveSchoolPeriod(schoolPeriod: SchoolPeriod, dslContext: DSLContext) =
        (dslContext.selectFrom(SCHOOL_PERIOD).where(SCHOOL_PERIOD.START_YEAR.eq(schoolPeriod.startYear.toBigDecimal()))
            .and(SCHOOL_PERIOD.END_YEAR.eq(schoolPeriod.endYear.toBigDecimal())).fetchAny() ?: (dslContext.newRecord(
            SCHOOL_PERIOD
        ).apply {
            id = getSchoolPeriodSeqNextVal()
            startYear = schoolPeriod.startYear.toBigDecimal()
            endYear = schoolPeriod.endYear.toBigDecimal()
        }.also {
            it.insert()
        })).mapSchoolPeriodRecordToSchoolPeriod()

    fun getPreviousPeriod(schoolPeriod: SchoolPeriod, dslContext: DSLContext) =
        dslContext.selectFrom(SCHOOL_PERIOD)
            .where(SCHOOL_PERIOD.START_YEAR.eq((schoolPeriod.startYear - 1).toBigDecimal()))
            .and(SCHOOL_PERIOD.END_YEAR.eq((schoolPeriod.endYear - 1).toBigDecimal())).fetchAny()
            ?.mapSchoolPeriodRecordToSchoolPeriod()


    fun fetchPeriodById(periodId: BigDecimal, dsl: DSLContext): SchoolPeriod = dsl.fetchOne(
        SCHOOL_PERIOD, SCHOOL_PERIOD.ID.eq(periodId)
    )?.mapSchoolPeriodRecordToSchoolPeriod() ?: error("Period with id:$periodId does not exist")

    fun fetchAllSchoolPeriodsWithTheSchoolsTheyAreStarted(): List<SchoolPeriodWithSchoolIds> {
        val allSchoolPEriodSchoolGroupedByPeriodId = fetchAllSchoolPeriodSchool().groupBy { it.schoolPeriodId }
        return db.selectFrom(SCHOOL_PERIOD).fetch().into(SchoolPeriodRecord::class.java).map {
            SchoolPeriodWithSchoolIds(id = it.id!!,
                startYear = it.startYear!!.toInt(),
                endYear = it.endYear!!.toInt(),
                schoolIds = allSchoolPEriodSchoolGroupedByPeriodId[it.id]?.mapNotNull { it.schoolId } ?: emptyList())
        }
    }

    fun fetchAllSchoolPeriodSchool(): List<SchoolPeriodSchoolRecord> = db.selectFrom(SCHOOL_PERIOD_SCHOOL).fetch()

    fun createSchoolPeriodSchoolRecord(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        dslContext: DSLContext
    ) = dslContext.newRecord(SCHOOL_PERIOD_SCHOOL).apply {
        id = getSchoolPeriodSchoolSeqNextVal()
        this.schoolId = schoolId
        this.schoolPeriodId = periodId
    }.insert()

    fun getSchoolPeriodSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_PERIOD_SEQ.nextval")).from("DUAL").fetchOne()!!
            .map { it.into(BigDecimal::class.java) }

    fun getSchoolPeriodSchoolSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_PERIOD_SCHOOL_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}

fun SchoolPeriodRecord.mapSchoolPeriodRecordToSchoolPeriod() = SchoolPeriod(
    id = this.id!!.toInt(), startYear = this.startYear!!.toInt(), endYear = this.endYear!!.toInt()
)