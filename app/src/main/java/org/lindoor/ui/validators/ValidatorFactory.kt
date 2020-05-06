package org.lindoor.ui.validators

class ValidatorFactory {
    companion object {
        val nonEmptyStringValidator = NonEmptyStringValidator()
        val uriValidator = NonEmptyUrlFormatValidator()
        val  hostnameEmptyOrValidValidator = RegExpFormatValidator("^[a-zA-Z0-9.]*$","invalid_host_name")
        val  numberEmptyOrValidValidator = RegExpFormatValidator("^[0-9]*$","invalid_number")

    }
}