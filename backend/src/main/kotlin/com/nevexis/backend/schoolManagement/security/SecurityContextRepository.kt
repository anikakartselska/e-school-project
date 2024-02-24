package com.nevexis.backend.schoolManagement.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@Component
class SecurityContextRepository(private val authenticationManager: AuthenticationManager) :
    ServerSecurityContextRepository {

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userService: UserSecurityService

    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        val token = swe.request.cookies.getFirst("token")?.value
        val refreshToken = swe.request.cookies.getFirst("refreshToken")?.value
        return if (token.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
            Mono.empty()
        } else {
            val auth: Authentication =
                UsernamePasswordAuthenticationToken(token, token)
            authenticationManager.authenticate(auth).switchIfEmpty(
                authenticationManager.authenticate(UsernamePasswordAuthenticationToken(refreshToken, refreshToken))
                    .map { authentication ->
                        val userDetails = userService.findUserByUsername(authentication.principal.toString())!!
                        val newAccessToken = jwtService.generateToken(userDetails)
                        val newRefreshToken = jwtService.generateRefreshToken(userDetails)
                        swe.response.addCookie(generateCookie(newAccessToken, "token"))
                        swe.response.addCookie(generateCookie(newRefreshToken, "refreshToken"))
                        authentication
                    }
            ).map(::SecurityContextImpl)

        }
    }
}

private fun generateCookie(token: String, cookieName: String): ResponseCookie = ResponseCookie.from(cookieName, token)
    .path("/")
    .httpOnly(true)
    .sameSite("strict")
    .build()