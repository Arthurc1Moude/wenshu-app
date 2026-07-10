package com.wenshu.app.util

object PasswordValidator {

    data class ValidationResult(
        val valid: Boolean,
        val message: String? = null,
        val strength: Int = 0
    )

    fun validate(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "密码长度至少8位", 0)
        }
        
        var strength = 0
        
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val hasChinese = password.any { it in '\u4e00'..'\u9fa5' }
        
        if (hasChinese) {
            return ValidationResult(false, "密码不能包含中文字符", 0)
        }
        
        if (!hasLowerCase) {
            return ValidationResult(false, "密码需包含至少1个小写字母", 1)
        }
        strength++
        
        if (!hasUpperCase) {
            return ValidationResult(false, "密码需包含至少1个大写字母", strength)
        }
        strength++
        
        if (!hasDigit) {
            return ValidationResult(false, "密码需包含至少1个数字", strength)
        }
        strength++
        
        if (hasSpecial) strength++
        if (password.length >= 12) strength++
        
        return ValidationResult(true, null, strength.coerceAtMost(5))
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^1[3-9]\\d{9}$"))
    }
}
