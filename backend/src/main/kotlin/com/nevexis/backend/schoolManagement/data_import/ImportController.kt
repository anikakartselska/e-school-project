package com.nevexis.backend.schoolManagement.data_import

import com.nevexis.backend.schoolManagement.users.SchoolRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class ImportController {

    @Autowired
    private lateinit var importService: ImportService

    @PostMapping("/import-user-excel")
    fun importUserExcel(
        @RequestPart file: ByteArray,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolRole: SchoolRole,
        @RequestParam schoolClassId: BigDecimal? = null,
        @RequestParam userId: BigDecimal
    ) = importService.createRequestsFromUsersExcel(file, periodId, schoolId, schoolRole, schoolClassId, userId)

}