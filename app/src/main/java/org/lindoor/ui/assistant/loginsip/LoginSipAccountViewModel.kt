package org.lindoor.ui.assistant.loginsip

import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.ui.assistant.CreatorAssistantViewModel

class LoginSipAccountViewModel :
    CreatorAssistantViewModel(corePreferences.sipAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var domain: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var transport: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var proxy: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var expiration: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(
        MutableLiveData<String>(
            corePreferences.config.getString(
                "proxy_default_values",
                "reg_expires",
                "31536000"
            )
        ), MutableLiveData<Boolean>(false)
    )

    val moreOptionsOpened = MutableLiveData(false)
    var pushReady = MutableLiveData(false)


    fun valid(): Boolean {

        return username.second.value!! && domain.second.value!! && pass1.second.value!! && proxy.second.value!! && expiration.second.value!!
    }
}
