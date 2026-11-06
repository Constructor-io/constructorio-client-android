package io.constructor.util

private val emailRegex = Regex("[\\w\\-+\\\\.]+@([\\w-]+\\.)+[\\w-]{2,4}")
private val phoneRegex = Regex("^(?:\\+\\d{11,12}|\\+\\d{1,3}\\s\\d{3}\\s\\d{3}\\s\\d{3,4}|\\(\\d{3}\\)\\d{7}|\\(\\d{3}\\)\\s\\d{3}\\s\\d{4}|\\(\\d{3}\\)\\d{3}-\\d{4}|\\(\\d{3}\\)\\s\\d{3}-\\d{4})$")
private val creditCardRegex = Regex("^(?:4[0-9]{15}|(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11})$")

/**
 * @suppress
 * Replaces personally identifiable information (emails, phone numbers, credit card numbers) with
 * a placeholder so that it never reaches the behavioral endpoints.
 */
fun String.redactPii(): String {
    if (emailRegex.containsMatchIn(this)) {
        return emailRegex.replace(this, "<email_omitted>")
    }

    if (phoneRegex.containsMatchIn(this)) {
        return phoneRegex.replace(this, "<phone_omitted>")
    }

    if (creditCardRegex.containsMatchIn(this)) {
        return creditCardRegex.replace(this, "<credit_omitted>")
    }

    return this
}
