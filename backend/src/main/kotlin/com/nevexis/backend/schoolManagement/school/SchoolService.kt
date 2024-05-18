package com.nevexis.backend.schoolManagement.school

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolService : BaseService() {

    fun getAllSchools(): List<School> = db.selectFrom(SCHOOL).fetch().map {
        School(
            id = it.id!!.toInt(),
            schoolName = it.schoolName!!,
            city = it.city!!,
            address = it.address!!,
            rooms = it.rooms?.let { it1 -> Json.decodeFromString(it1) }
        )
    }

    fun getSchoolById(id: BigDecimal, dsl: DSLContext = db): School =
        dsl.selectFrom(SCHOOL).where(SCHOOL.ID.eq(id)).fetchAny()?.into(SchoolRecord::class.java)?.map {
            School(
                id = (it as SchoolRecord).id!!.toInt(),
                schoolName = it.schoolName!!,
                city = it.city!!,
                address = it.address!!,
                rooms = Json.decodeFromString(it.rooms ?: "[]")
            )
        } ?: throw SMSError("Данните не са намерени", "Училището не съществува")

    fun getAllRoomsFromSchool(schoolId: BigDecimal, dsl: DSLContext = db): List<RoomToSubjects> =
        dsl.select(SCHOOL.ROOMS).from(
            SCHOOL
        ).where(SCHOOL.ID.eq(schoolId)).fetchAny().let { Json.decodeFromString(it!!.into(String::class.java)) }

    fun updateSchool(school: School) {
        db.selectFrom(SCHOOL)
            .where(SCHOOL.ID.eq(school.id.toBigDecimal()))
            .fetchAny()!!
            .apply {
                this.id = school.id.toBigDecimal()
                this.schoolName = school.schoolName
                this.city = school.city
                this.address = school.address
                this.rooms = Json.encodeToString(school.rooms)
            }.update()
    }

}