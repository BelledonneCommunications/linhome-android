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

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linphone.core.AccountCreator
import org.linphone.core.TransportType
import java.util.*

open class CreatorAssistantViewModel(defaultValuePath: String) : ViewModel() {

    var accountCreator: AccountCreator

    init {
        coreContext.core.loadConfigFromXml(defaultValuePath)
        accountCreator =
            coreContext.core.createAccountCreator(LinhomeApplication.corePreferences.xmlRpcServerUrl)
        accountCreator.language = Locale.getDefault().language
        accountCreator.domain = corePreferences.loginDomain
        accountCreator.algorithm = corePreferences.passwordAlgo

    }

    fun setUsername(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.UsernameStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setUsername(field.first.value)
        field.second.value = result == AccountCreator.UsernameStatus.Ok
        return result
    }

    fun setPassword(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.PasswordStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setPassword(field.first.value)
        field.second.value = result == AccountCreator.PasswordStatus.Ok
        return result
    }

    fun setEmail(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.EmailStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setEmail(field.first.value)
        field.second.value = result == AccountCreator.EmailStatus.Ok
        return result
    }

    fun setDomain(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.DomainStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setDomain(field.first.value)
        field.second.value = result == AccountCreator.DomainStatus.Ok
        return result
    }

    fun setTransport(transport: TransportType) {
        accountCreator.transport = transport
    }

}