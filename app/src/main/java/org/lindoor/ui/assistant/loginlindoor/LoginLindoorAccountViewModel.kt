package org.lindoor.ui.assistant.loginlindoor

import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.ui.assistant.CreatorAssistantViewModel

class LoginLindoorAccountViewModel : CreatorAssistantViewModel(corePreferences.lindoorAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair(MutableLiveData<String>(),MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair( MutableLiveData<String>(),MutableLiveData<Boolean>(false))

    fun valid(): Boolean {
        return username.second.value!! && pass1.second.value!!
    }

}
