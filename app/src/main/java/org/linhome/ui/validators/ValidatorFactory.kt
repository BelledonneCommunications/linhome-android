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

package org.linhome.ui.validators

class ValidatorFactory {
    companion object {
        val nonEmptyStringValidator = NonEmptyStringValidator()
        val uriValidator = NonEmptyUrlFormatValidator()
        val hostnameEmptyOrValidValidator =
            RegExpFormatValidator("^[a-zA-Z0-9.]*$", "invalid_host_name")
        val numberEmptyOrValidValidator = RegExpFormatValidator("^[0-9]*$", "invalid_number")
        val numberNonEmptyOrValidValidator = RegExpFormatValidator("^[0-9]+$", "invalid_number")
        val sipUri =
            NonEmptyWithRegExpFormatValidator("^(sip|sips):(.+)@(.+)\$", "invalid_sip_uri")
        val optionalSipUri =
            EmptyWithRegExpFormatValidator("^(?:sips?:)?([^@]+)\$", "invalid_sip_uri")
        val actionCode = RegExpFormatValidator("^[0-9#*]+$", "invalid_action_code")
    }
}