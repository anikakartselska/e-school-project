package com.nevexis.backend.schoolManagement.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.token.expiration}")
    private lateinit var expirationTime: String

    @Value("\${jwt.refreshtoken.expiration}")
    private lateinit var refreshTokenExpiration: String

    private var key = lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    fun extractUsername(token: String): String? {
        return extractClaim(token, Claims::getSubject)
    }

    fun generateToken(userDetails: SMSUserDetails): String {
        return buildToken(userDetails, expirationTime.toLong())
    }

    fun generateRefreshToken(userDetails: SMSUserDetails): String {
        return buildToken(userDetails, refreshTokenExpiration.toLong())
    }

    private fun buildToken(userDetails: SMSUserDetails, expiration: Long): String {
        return Jwts.builder().apply {
            val extraClaims = userDetails.user.role?.role?.name?.let {
                mutableMapOf("role" to it)
            } ?: emptyMap()
            setClaims(extraClaims)
        }
            .setSubject(userDetails.username)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(expiration)))
            .signWith(key.value)
            .compact() ?: throw Exception("Error in generating token")
    }

    fun isTokenValid(token: String): Boolean {
        return !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token)?.before(Date()) ?: true
    }

    private fun extractExpiration(token: String): Date? {
        return extractClaim(token, Claims::getExpiration)
    }


    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver.invoke(claims)

    }

    fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder().setSigningKey(key.value).build().parseClaimsJws(token).body
    }
}