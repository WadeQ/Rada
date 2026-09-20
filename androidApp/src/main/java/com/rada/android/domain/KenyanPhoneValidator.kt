package com.rada.android.domain

object KenyanPhoneValidator {

    data class Result(val isValid: Boolean, val e164: String?, val errorMessage: String?)

    fun validate(rawInput: String): Result {
        val digitsOnly = rawInput.filter { it.isDigit() }

        val normalized = when {
            digitsOnly.startsWith("254") && digitsOnly.length == 12 -> digitsOnly
            digitsOnly.startsWith("0") && digitsOnly.length == 10 -> "254" + digitsOnly.drop(1)
            digitsOnly.length == 9 && (digitsOnly.startsWith("7") || digitsOnly.startsWith("1")) -> "254$digitsOnly"
            else -> null
        }

        if (normalized == null) {
            return Result(false, null, "Enter a valid Kenyan number, e.g. 0712 345 678")
        }
        val leadingDigit = normalized[3]
        if (leadingDigit != '7' && leadingDigit != '1') {
            return Result(false, null, "Kenyan mobile numbers start with 7 or 1 after the country code")
        }
        return Result(true, "+$normalized", null)
    }
}
