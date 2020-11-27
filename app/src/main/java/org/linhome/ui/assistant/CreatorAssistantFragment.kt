/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.ui.assistant

import org.linhome.GenericFragment
import org.linhome.customisation.Texts
import org.linhome.ui.widgets.LTextInput
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

