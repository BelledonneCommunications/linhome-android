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

package org.linhome.linphonecore.extensions

import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.store.DeviceStore
import org.linphone.core.Call
import org.linphone.core.CallParams


fun Call.extendedAcceptEarlyMedia() {
    if (state == Call.State.IncomingReceived) {
        val earlyMediaCallParams: CallParams? = coreContext.core.createCallParams(this)
        earlyMediaCallParams?.recordFile = callLog.historyEvent().mediaFileName
        earlyMediaCallParams?.isAudioEnabled = false
        isCameraEnabled = false
        acceptEarlyMediaWithParams(earlyMediaCallParams)
        startRecording()
    }
}

fun Call.extendedAccept() {
    val inCallParams: CallParams? = coreContext.core.createCallParams(this)
    inCallParams?.recordFile = callLog.historyEvent().mediaFileName
    isCameraEnabled = false
    inCallParams?.isAudioEnabled = true

    val device = DeviceStore.findDeviceByAddress(remoteAddress)
    if (device != null) {
        coreContext.core.useRfc2833ForDtmf = device.actionsMethodType == "method_dtmf_rfc_4733"
        coreContext.core.useInfoForDtmf = device.actionsMethodType == "method_dtmf_sip_info"
    } else {
        coreContext.core.useRfc2833ForDtmf = corePreferences.defaultActionsMethodType == "method_dtmf_rfc_4733"
        coreContext.core.useInfoForDtmf = corePreferences.defaultActionsMethodType == "method_dtmf_sip_info"
    }

    acceptWithParams(inCallParams)
    if (!isRecording)
        startRecording()
}


