package com.nammahomestay.utils

import android.util.Patterns

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.length == 10 && phone.all { it.isDigit() }
    }
}
