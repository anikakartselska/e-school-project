package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.schoolManagement.users.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class RequestController {

    @Autowired
    private lateinit var requestService: RequestService

    @GetMapping("/get-user-requests-by-school-and-period")
    suspend fun getUserRequestsBySchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam userId: BigDecimal? = null
    ) = requestService.fetchUserRequestsForSchoolAndPeriod(schoolId, periodId, userId)

    @PostMapping("/create-user")
    suspend fun createUser(
        @RequestBody user: User,
        @RequestParam loggedInUserId: BigDecimal
    ): BigDecimal? = requestService.createRequests(listOf(user), loggedInUserId).firstOrNull()

    @GetMapping("/get-role-requests-by-school-and-period")
    suspend fun getRoleRequestsBySchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = requestService.fetchRoleRequestsForSchoolAndPeriod(schoolId, periodId)

    @PostMapping("/change-request-status")
    suspend fun changeRequestStatus(
        @RequestBody requestIds: List<Int>,
        @RequestParam requestStatus: RequestStatus,
        @RequestParam resolvedByUserId: BigDecimal
    ) = requestService.changeRequestStatus(requestIds.map { it.toBigDecimal() }, requestStatus, resolvedByUserId)

    @PostMapping("/change-user-change-status-request")
    fun createUserChangeStatusRequest(
        @RequestParam userId: BigDecimal,
        @RequestParam newStatus: RequestStatus,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam loggedUserId: BigDecimal
    ) = requestService.createUserChangeStatusRequest(userId, newStatus, periodId, schoolId, loggedUserId)
}