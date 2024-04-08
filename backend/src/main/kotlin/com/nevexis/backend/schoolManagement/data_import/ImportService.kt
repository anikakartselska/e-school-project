package com.nevexis.backend.schoolManagement.data_import

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriodService
import com.nevexis.backend.schoolManagement.users.*
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jooq.DSLContext
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
    private lateinit var userSecurityService: UserSecurityService

    @Autowired
    private lateinit var requestService: RequestService

    fun createRequestsFromUsersExcel(
        byteArray: ByteArray,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolRole: SchoolRole,
        schoolClassId: BigDecimal? = null,
        userId: BigDecimal,
        dslContext: DSLContext = db
    ): List<UserView> = dslContext.transactionResult { transaction ->
        mapExcelToListOfUsers(
            byteArray,
            periodId,
            schoolId,
            schoolRole,
            schoolClassId,
            transaction.dsl()
        ).also { users ->
            requestService.createRequests(users, userId, transaction.dsl())
        }.map { userSecurityService.mapUserToUserView(it) }
    }

    private fun mapExcelToListOfUsers(
        byteArray: ByteArray,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolRole: SchoolRole,
        schoolClassId: BigDecimal? = null,
        dslContext: DSLContext = db
    ): List<User> {
        return dslContext.transactionResult { transaction ->
            val school = schoolService.getSchoolById(schoolId, transaction.dsl())
            val period = periodService.fetchPeriodById(periodId, transaction.dsl())
            return@transactionResult WorkbookFactory.create(ByteArrayInputStream(byteArray)).firstOrNull()
                ?.let { sheet ->
                    sheet.drop(1).map { row ->
                        val phoneNumber = DataFormatter().formatCellValue(row.getCell(2))
                        val existingUser = userSecurityService.findUserWithAllRolesByPhoneNumberForSchoolAndPeriod(
                            phoneNumber,
                            schoolId,
                            periodId
                        )
                        val schoolUserRole = when (schoolRole) {
                            SchoolRole.ADMIN, SchoolRole.TEACHER -> SchoolUserRole(
                                userId = existingUser?.id,
                                period = period,
                                school = school,
                                role = schoolRole,
                                status = RequestStatus.PENDING,
                                detailsForUser = null
                            )

                            SchoolRole.STUDENT -> SchoolUserRole(
                                userId = existingUser?.id,
                                period = period,
                                school = school,
                                role = schoolRole,
                                status = RequestStatus.PENDING,
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
                                userId = existingUser?.id,
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
                                        ).id!!.toBigDecimal())
                                        .let { schoolClass ->
                                            val childPhoneNumber = DataFormatter().formatCellValue(
                                                row.getCell(9)
                                            )
                                            userSecurityService.findStudentByPhoneNumberPeriodAndSchoolClass(
                                                childPhoneNumber,
                                                periodId,
                                                schoolClass
                                            )
                                        },
                                )
                            )
                        }.let { schoolUserRole ->

                            val existingRoleId = existingUser?.roles?.find { role -> role == schoolUserRole }?.id
                            schoolUserRole.copy(
                                id = existingRoleId
                            )
                        }

                        User(
                            id = existingUser?.id,
                            personalNumber = DataFormatter().formatCellValue(row.getCell(0)),
                            email = DataFormatter().formatCellValue(row.getCell(1)),
                            phoneNumber = phoneNumber,
                            firstName = DataFormatter().formatCellValue(row.getCell(3)),
                            middleName = DataFormatter().formatCellValue(row.getCell(4)),
                            lastName = DataFormatter().formatCellValue(row.getCell(5)),
                            username = DataFormatter().formatCellValue(row.getCell(6)),
                            address = DataFormatter().formatCellValue(row.getCell(7)),
                            gender = getTranslationFromBulgarianToEnglish(
                                DataFormatter().formatCellValue(
                                    row.getCell(
                                        8
                                    )
                                )
                            ),
                            roles = listOf(schoolUserRole),
                            status = existingUser?.status ?: RequestStatus.PENDING
                        )
                    }
                } ?: throw SMSError("EMPTY", "Workbook empty !")
        }
    }

}