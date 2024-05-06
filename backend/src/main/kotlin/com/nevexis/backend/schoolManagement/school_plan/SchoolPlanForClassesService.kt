package com.nevexis.backend.schoolManagement.school_plan

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PLAN_FOR_CLASSES
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolPlanForClassesService : BaseService() {

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

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

}