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

package org.linhome.ui.settings

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.linhome.customisation.Texts
import org.linhome.utils.databindings.ViewModelWithTools
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.MediaEncryption

class SettingsViewModel : ViewModelWithTools() {

    val audioCodecs = ArrayList<ViewDataBinding>()
    val videCodecs = ArrayList<ViewDataBinding>()

    val enableIpv6 = MutableLiveData(core.ipv6Enabled())
    val latestSnapshotShown = MutableLiveData(corePref.showLatestSnapshot)


    // Logs
    val enableDebugLogs = MutableLiveData(corePref.debugLogs)
    var logUploadResult = MutableLiveData<Pair<Core.LogCollectionUploadState, String>>()

    private val uploadCoreListener = object : CoreListenerStub() {
        override fun onLogCollectionUploadStateChanged(
            core: Core,
            state: Core.LogCollectionUploadState,
            url: String
        ) {
            logUploadResult.postValue(Pair(state, url))
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

    val showLatestSnapshot = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            corePref.showLatestSnapshot = newValue
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
