package com.nevexis.backend.schoolManagement.security

import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SMSUserDetails(var user: UserSecurity) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority(user.role?.role?.name))

    override fun getPassword(): String = user.password!!

    override fun getUsername(): String = user.id.toString()

    override fun isAccountNonExpired(): Boolean = false

    override fun isAccountNonLocked(): Boolean = false

    override fun isCredentialsNonExpired(): Boolean = false

    override fun isEnabled(): Boolean = true

}