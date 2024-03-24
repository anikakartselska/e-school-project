package com.nevexis.backend.schoolManagement.excel

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriodService
import com.nevexis.backend.schoolManagement.users.DetailsForUser
import com.nevexis.backend.schoolManagement.users.Gender
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.math.BigDecimal

@Service
class ImportService : BaseService() {

    @Autowired
    private lateinit var schoolService: SchoolService

    @Autowired
    private lateinit var periodService: SchoolPeriodService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var userService: UserSecurityService

    fun mapExcelToListOfUsers(
        byteArray: ByteArray,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolRole: SchoolRole,
        schoolClassId: BigDecimal? = null
    ): List<User> {
        return db.transactionResult { transaction ->
            val school = schoolService.getSchoolById(schoolId, transaction.dsl())
            val period = periodService.fetchPeriodById(periodId, transaction.dsl())
            return@transactionResult WorkbookFactory.create(ByteArrayInputStream(byteArray)).firstOrNull()
                ?.let { sheet ->
                    sheet.drop(1).map { row ->
                        val schoolUserRole = when (schoolRole) {
                            SchoolRole.ADMIN, SchoolRole.TEACHER -> SchoolUserRole(
                                period = period,
                                school = school,
                                role = schoolRole,
                                status = RequestStatus.APPROVED,
                                detailsForUser = null
                            )

                            SchoolRole.STUDENT -> SchoolUserRole(
                                period = period,
                                school = school,
                                role = schoolRole,
                                status = RequestStatus.APPROVED,
                                detailsForUser = DetailsForUser.DetailsForStudent(
                                    schoolClass = if (schoolClassId != null) {
                                        schoolClassService.getSchoolClassById(schoolClassId, transaction.dsl())
                                    } else {
                                        schoolClassService.getSchoolClassByNameSchoolAndPeriod(
                                            DataFormatter().formatCellValue(
                                                row.getCell(9)
                                            ), schoolId, periodId, transaction.dsl()
                                        )
                                    },
                                    numberInClass = DataFormatter().formatCellValue(
                                        row.getCell(10)
                                    ).toIntOrNull()
                                )
                            )

                            SchoolRole.PARENT -> SchoolUserRole(
                                period = period,
                                school = school,
                                role = schoolRole,
                                status = RequestStatus.APPROVED,
                                detailsForUser = DetailsForUser.DetailsForParent(
                                    child = (schoolClassId
                                        ?: schoolClassService.getSchoolClassByNameSchoolAndPeriod(
                                            DataFormatter().formatCellValue(
                                                row.getCell(10)
                                            ), schoolId, periodId, transaction.dsl()
                                        ).id.toBigDecimal())
                                        .let { schoolClass ->
                                            val childPhoneNumber = DataFormatter().formatCellValue(
                                                row.getCell(9)
                                            )
                                            userService.findStudentByPhoneNumberPeriodAndSchoolClass(
                                                childPhoneNumber,
                                                periodId,
                                                schoolClass
                                            )
                                        },
                                )
                            )
                        }
                        val phoneNumber = DataFormatter().formatCellValue(row.getCell(0))
                        User(
                            personalNumber = phoneNumber,
                            email = DataFormatter().formatCellValue(row.getCell(1)),
                            phoneNumber = DataFormatter().formatCellValue(row.getCell(2)),
                            firstName = DataFormatter().formatCellValue(row.getCell(3)),
                            middleName = DataFormatter().formatCellValue(row.getCell(4)),
                            lastName = DataFormatter().formatCellValue(row.getCell(5)),
                            username = DataFormatter().formatCellValue(row.getCell(6)),
                            address = DataFormatter().formatCellValue(row.getCell(7)),
                            password = phoneNumber,
                            gender = Gender.valueOf(DataFormatter().formatCellValue(row.getCell(8))),
                            roles = listOf(schoolUserRole)
                        )
                    }
                } ?: throw SMSError("EMPTY", "Workbook empty !")
        }
    }

}