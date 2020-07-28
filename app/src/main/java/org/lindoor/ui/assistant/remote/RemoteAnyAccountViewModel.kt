package org.lindoor.ui.assistant.remote

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.entities.Account
import org.linphone.core.ConfiguringState
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub


class RemoteAnyAccountViewModel : ViewModel() {

    var url: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(MutableLiveData(), MutableLiveData(false))

    var configurationResult = MutableLiveData<ConfiguringState>()
    val pushReady = MutableLiveData<Boolean>()


    private val coreListener = object : CoreListenerStub() {
        override fun onConfiguringStatus(core: Core, status: ConfiguringState, message: String?) {
            if (status == ConfiguringState.Successful) {
                if (Account.pushGateway() != null) {
                    Account.linkProxiesWithPushGateway(pushReady)
                } else
                    Account.createPushGateway(pushReady)
            }
            configurationResult.postValue(status)
        }

        override fun onQrcodeFound(core: Core, qr: String) {
            GlobalScope.launch(context = Dispatchers.Main) {
                coreContext.core.enableQrcodeVideoPreview(false)
                coreContext.core.enableVideoPreview(false)
                url.first.value = qr
                startRemoteProvisionning()
            }
        }
    }

    fun valid(): Boolean {
        return url.second.value!!
    }

    init {
        coreContext.core.addListener(coreListener)
    }

    fun startRemoteProvisionning() {
        coreContext.core.provisioningUri = url.first.value
        coreContext.core.stop()
        coreContext.core.start()
    }

    override fun onCleared() {
        coreContext.core.removeListener(coreListener)
        super.onCleared()
    }


}
