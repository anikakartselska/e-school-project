package com.nevexis.backend.schoolManagement

import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
open class BaseService {
    @Autowired
    protected lateinit var db: DSLContext

}