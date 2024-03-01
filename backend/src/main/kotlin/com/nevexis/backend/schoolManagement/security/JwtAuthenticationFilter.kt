package com.nevexis.backend.schoolManagement.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Component
class AuthenticationManager : ReactiveAuthenticationManager {
    @Autowired
    private lateinit var jwtUtil: JwtService

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()
        return Mono.just(
            jwtUtil.isTokenValid(authToken)
        )
            .filter {
                it
            }
            .switchIfEmpty(Mono.empty())
            .map {
                val username = jwtUtil.extractUsername(authToken)
                val claims = jwtUtil.extractAllClaims(authToken)
                val rolesMap = claims.get("role", List::class.java) ?: emptyList<String>()
                UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    rolesMap.map { SimpleGrantedAuthority(it?.toString()) }
                )
            }
    }
}

