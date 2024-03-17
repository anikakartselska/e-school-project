package com.nevexis.backend.schoolManagement.requests

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

}