package com.nevexis.backend.schoolManagement.school_period

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolPeriodController {
    @Autowired
    private lateinit var schoolPeriodService: SchoolPeriodService


}