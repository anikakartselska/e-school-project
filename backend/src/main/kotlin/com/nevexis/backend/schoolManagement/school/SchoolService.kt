package com.nevexis.backend.schoolManagement.school

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolService : BaseService() {

    fun getAllSchools(): List<School> = db.selectFrom(SCHOOL).fetch().map { it.into(School::class.java) }

    fun getSchoolById(id: BigDecimal, dsl: DSLContext = db): School =
        dsl.selectFrom(SCHOOL).where(SCHOOL.ID.eq(id)).fetchAny()?.into(School::class.java)
            ?: error("School with id $id does not exist")

    fun getAllRoomsFromSchool(schoolId: BigDecimal, dsl: DSLContext = db): List<String> = dsl.select(SCHOOL.ROOMS).from(
        SCHOOL
    ).where(SCHOOL.ID.eq(schoolId)).fetchAny().let { Json.decodeFromString(it!!.into(String::class.java)) }

}