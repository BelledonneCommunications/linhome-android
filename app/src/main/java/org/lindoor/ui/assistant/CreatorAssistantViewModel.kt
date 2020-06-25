package org.lindoor.ui.assistant

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.linphone.core.AccountCreator
import org.linphone.core.TransportType
import java.util.*

open class CreatorAssistantViewModel(defaultValuePath: String) : ViewModel() {

    var accountCreator: AccountCreator

    init {
        coreContext.core.loadConfigFromXml(defaultValuePath)
        accountCreator =
            coreContext.core.createAccountCreator(LindoorApplication.corePreferences.xmlRpcServerUrl)
        accountCreator.language = Locale.getDefault().language
        accountCreator.domain = corePreferences.loginDomain
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