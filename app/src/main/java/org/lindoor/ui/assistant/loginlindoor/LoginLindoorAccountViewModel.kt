package org.lindoor.ui.assistant.loginlindoor

import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.ui.assistant.CreatorAssistantViewModel
import org.linphone.core.XmlRpcArgType
import org.linphone.core.XmlRpcRequest
import org.linphone.core.XmlRpcRequestListener
import org.linphone.mediastream.Log

class LoginLindoorAccountViewModel : CreatorAssistantViewModel(corePreferences.lindoorAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair(MutableLiveData<String>(),MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair( MutableLiveData<String>(),MutableLiveData<Boolean>(false))

    fun fieldsValid(): Boolean {
        return username.second.value!! && pass1.second.value!!
    }

}
