package org.lindoor.ui.assistant

import org.lindoor.GenericFragment
import org.lindoor.customisation.Texts
import org.lindoor.ui.widgets.LTextInput
import org.linphone.core.AccountCreator

open class CreatorAssistantFragment : GenericFragment() {

    fun updateField(status: AccountCreator.UsernameStatus?, textInput: LTextInput) {
        when (status) {
            AccountCreator.UsernameStatus.TooShort -> {
                textInput.setError(Texts.get("account_creator_username_too_short"))
            }
            AccountCreator.UsernameStatus.TooLong -> {
                textInput.setError(Texts.get("account_creator_username_too_long"))
            }
            AccountCreator.UsernameStatus.InvalidCharacters -> {
                textInput.setError(
                    Texts.get("account_creator_username_invalid_characters")
                )
            }
            AccountCreator.UsernameStatus.Invalid -> {
                textInput.setError(Texts.get("account_creator_username_invalid"))
            }
            AccountCreator.UsernameStatus.Ok -> {
                textInput.clearError()
            }
        }
    }


    fun updateField(status: AccountCreator.PasswordStatus?, textInput: LTextInput) {
        when (status) {
            AccountCreator.PasswordStatus.TooShort -> {
                textInput.setError(Texts.get("account_creator_password_too_short"))
            }
            AccountCreator.PasswordStatus.TooLong -> {
                textInput.setError(Texts.get("account_creator_password_too_long"))
            }
            AccountCreator.PasswordStatus.InvalidCharacters -> {
                textInput.setError(Texts.get("account_creator_password_invalid_characters"))
            }
            AccountCreator.PasswordStatus.MissingCharacters -> {
                textInput.setError(Texts.get("account_creator_password_missingchars"))
            }
            AccountCreator.PasswordStatus.Ok -> {
                textInput.clearError()
            }
        }
    }

    fun updateField(status: AccountCreator.EmailStatus?, textInput: LTextInput) {
        when (status) {
            AccountCreator.EmailStatus.Malformed -> {
                textInput.setError(Texts.get("account_creator_email_malformed"))
            }
            AccountCreator.EmailStatus.InvalidCharacters -> {
                textInput.setError(Texts.get("account_creator_email_invalid_characters"))
            }
            AccountCreator.EmailStatus.Ok -> {
                textInput.clearError()
            }
        }
    }

    fun updateField(status: AccountCreator.DomainStatus?, textInput: LTextInput) {
        when (status) {
            AccountCreator.DomainStatus.Invalid -> {
                textInput.setError(Texts.get("account_creator_domain_invalid"))
            }
            AccountCreator.DomainStatus.Ok -> {
                textInput.clearError()
            }
        }
    }


}

