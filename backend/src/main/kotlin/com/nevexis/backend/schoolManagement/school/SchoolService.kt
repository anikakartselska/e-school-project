package com.nevexis.backend.schoolManagement.school

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolService : BaseService() {

    fun getAllSchools(): List<School> = db.selectFrom(SCHOOL).fetch().map { it.into(School::class.java) }

    fun getSchoolById(id: BigDecimal, dsl: DSLContext = db): School =
        dsl.selectFrom(SCHOOL).where(SCHOOL.ID.eq(id)).fetchAny()?.into(School::class.java)
            ?: error("School with id $id does not exist")
}