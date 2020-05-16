package org.lindoor.ui.settings

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.lindoor.customisation.Texts
import org.lindoor.utils.databindings.ViewModelWithTools
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.MediaEncryption

class SettingsViewModel : ViewModelWithTools() {

    val audioCodecs = ArrayList<ViewDataBinding>()
    val videCodecs = ArrayList<ViewDataBinding>()

    val enableIpv6 = MutableLiveData(core.ipv6Enabled())

    // Logs
    val enableDebugLogs = MutableLiveData(corePref.debugLogs)
    var logUploadResult = MutableLiveData<Pair<Core.LogCollectionUploadState,String>>()

    private val uploadCoreListener = object : CoreListenerStub() {
        override fun onLogCollectionUploadStateChanged(
            core: Core,
            state: Core.LogCollectionUploadState,
            url: String
        ) {
            logUploadResult.postValue(Pair(state,url))
        }
    }

    // Media Encryption
    private val encryptionValues = arrayListOf<Int>()

    val encryptionListener = object : SettingListenerStub() {
        override fun onListValueChanged(position: Int) {
            core.mediaEncryption = MediaEncryption.fromInt(encryptionValues[position])
            encryptionIndex.value = position
        }
    }
    val encryptionIndex = MutableLiveData<Int>()
    val encryptionLabels = MutableLiveData<ArrayList<String>>()

    init {
        core.addListener(uploadCoreListener)
        initEncryptionList()
    }

    override fun onCleared() {
        core.removeListener(uploadCoreListener)
        super.onCleared()
    }

    val enableIpv6Listener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            core.enableIpv6(newValue)
        }
    }

    val enableDebugLogsListener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            corePref.debugLogs = newValue
        }
    }

    private fun initEncryptionList() {
        val labels = arrayListOf<String>()

        labels.add(Texts.get("none"))
        encryptionValues.add(MediaEncryption.None.toInt())

        if (core.mediaEncryptionSupported(MediaEncryption.SRTP)) {
            labels.add("SRTP")
            encryptionValues.add(MediaEncryption.SRTP.toInt())
        }

        encryptionLabels.value = labels
        encryptionIndex.value = encryptionValues.indexOf(core.mediaEncryption.toInt())
    }



}
