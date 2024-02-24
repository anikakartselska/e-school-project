package com.nevexis.backend.schoolManagement.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SMSUserDetails(var user: UserSec) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        user.roles.map { SimpleGrantedAuthority(it.name) }.toMutableList()

    override fun getPassword(): String = user.password

    override fun getUsername(): String = user.username

    override fun isAccountNonExpired(): Boolean = false

    override fun isAccountNonLocked(): Boolean = false

    override fun isCredentialsNonExpired(): Boolean = false

    override fun isEnabled(): Boolean = true

}