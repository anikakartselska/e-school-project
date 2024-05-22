package com.nevexis.backend.schoolManagement.data_import

import com.nevexis.backend.schoolManagement.users.SchoolRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class ImportController {

    @Autowired
    private lateinit var importService: ImportService

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/import-user-excel")
    suspend fun importUserExcel(
        @RequestPart file: ByteArray,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolRole: SchoolRole,
        @RequestParam schoolClassId: BigDecimal,
        principal: Principal,
    ) = importService.createRequestsFromUsersExcel(
        file,
        periodId,
        schoolId,
        schoolRole,
        schoolClassId,
        principal.name.toBigDecimal()
    )

}