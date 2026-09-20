package com.rada.android.domain

/**
 * Validates and normalizes Kenyan mobile numbers. Accepts the formats people actually type:
 * 07XXXXXXXX / 01XXXXXXXX (local, 10 digits), 2547XXXXXXXX / 2541XXXXXXXX (no plus), and
 * +2547XXXXXXXX / +2541XXXXXXXX. Rejects anything else rather than silently accepting garbage —
 * the prototype's slide-to-continue accepted any input, which isn't acceptable once this is
 * wired to real Firebase phone auth (an invalid number wastes an SMS quota call for nothing).
 *
 * NEEDS VERIFICATION: Kenyan mobile prefixes shift as operators get new number ranges
 * allocated (e.g. Safaricom, Airtel, Telkom blocks) — the 07/01 leading-digit check here is
 * deliberately loose (any 07/01 prefix) rather than hardcoding a specific operator's known
 * ranges, to avoid rejecting valid numbers on a range I don't have current data for.
 */
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
        val leadingDigit = normalized[3] // first digit after "254"
        if (leadingDigit != '7' && leadingDigit != '1') {
            return Result(false, null, "Kenyan mobile numbers start with 7 or 1 after the country code")
        }
        return Result(true, "+$normalized", null)
    }
}
