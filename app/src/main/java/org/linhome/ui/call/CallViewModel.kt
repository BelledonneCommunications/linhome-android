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

package org.linhome.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.images.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.customisation.DeviceTypes
import org.linhome.entities.Action
import org.linhome.entities.Device
import org.linhome.entities.HistoryEvent
import org.linhome.linphonecore.CoreContext
import org.linhome.linphonecore.extensions.*
import org.linhome.store.DeviceStore
import org.linhome.utils.extensions.existsAndIsNotEmpty
import org.linhome.utils.getImageDimension
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.CallListenerStub
import org.linphone.core.Reason
import org.linphone.core.tools.Log
import java.io.File
import java.util.ArrayList


class CallViewModelFactory(private val call: Call) :
        ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CallViewModel(call) as T
    }
}

class CallViewModel(val call: Call) : ViewModel() {
    val device: MutableLiveData<Device?> =
            MutableLiveData(DeviceStore.findDeviceByAddress(call.remoteAddress))
    val defaultDeviceType: MutableLiveData<String?> = MutableLiveData(DeviceTypes.defaultType)
    val actions: ArrayList<Action> = device.value?.let {
        it.actions
    } ?: defaultActionsIfAny()

    val callState: MutableLiveData<Call.State> = MutableLiveData(call.state)
    val videoContent: MutableLiveData<Boolean> = MutableLiveData()
    val videoFullScreen: MutableLiveData<Boolean> = MutableLiveData(false)

    val speakerDisabled: MutableLiveData<Boolean> =
            MutableLiveData(coreContext.core.outputAudioDevice?.type != AudioDevice.Type.Speaker)
    val microphoneMuted: MutableLiveData<Boolean> = MutableLiveData(!coreContext.core.isMicEnabled)

    val videoSize = MutableLiveData<Size>()

    private var historyEvent: HistoryEvent

    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call, cstate: Call.State?, message: String) {
            cstate?.also { state ->
                if (call != null) {
                    historyEvent = call.callLog.historyEvent()
                }
                fireActionsOnCallStateChanged(state)
                attemptSetDeviceThumbnail(state)
                call?.remoteParams?.isVideoEnabled?.also {
                    if (!videoContent.value!!)
                        call.requestNotifyNextVideoFrameDecoded()
                }
                callState.value = state
            }

        }

        override fun onNextVideoFrameDecoded(call: Call) {
            if (!videoContent.value!!) {
                videoContent.value = true
            }
            call?.callLog?.historyEvent()?.also { event ->
                if (!event.hasVideo) {
                    event.hasVideo = true
                    event.persist()
                }
                if (!event.mediaThumbnail.existsAndIsNotEmpty()) {
                    call.takeVideoSnapshot(event.mediaThumbnail.absolutePath)
                    GlobalScope.launch(context = Dispatchers.Main) {
                        delay(500) // Snapshot availability takes a little time.
                        event.mediaThumbnail.absolutePath.getImageDimension().also {
                            if (it.width != 0 && it.height != 0)
                                videoSize.value = it
                        }
                    }
                }
            }
        }
    }

    init {
        historyEvent = call.callLog.historyEvent()
        videoContent.value = historyEvent.hasVideo
        call.addListener(callListener)
        fireActionsOnCallStateChanged(call.state)
        if (!videoContent.value!!) {
            call?.remoteParams?.isVideoEnabled?.also {
                if (it)
                    call.requestNotifyNextVideoFrameDecoded()
            }
        }
        if (LinhomeApplication.instance.tablet()) {
            coreContext.core.forceSpeakerAudioRoute()
        }
        device.value?.thumbNail?.also {
            it.absolutePath.getImageDimension().also {
                if (it.width != 0 && it.height != 0)
                    videoSize.value = it
            }
        }
        call.callLog.historyEvent().also { event ->
            event.mediaThumbnail.absolutePath.getImageDimension().also {
                if (it.width != 0 && it.height != 0)
                    videoSize.value = it
            }
        }
    }

    private fun fireActionsOnCallStateChanged(cstate: Call.State) {
        if (cstate == Call.State.IncomingReceived) {
            call.extendedAcceptEarlyMedia()
        }
        if (cstate == Call.State.StreamsRunning && call.callLog?.dir == Call.Dir.Outgoing && !call.isRecording) {
            call.startRecording()
        }
    }


    private fun attemptSetDeviceThumbnail(cstate: Call.State) {
        if (cstate == Call.State.End) { // Copy call media file to device file if there is none or user needs last
            device.value?.also { d ->
                d.thumbNail.also { deviceThumb ->
                    if (corePreferences.showLatestSnapshot || !deviceThumb.existsAndIsNotEmpty()) {
                        call.callLog?.historyEvent()?.also { event ->
                            GlobalScope.launch(context = Dispatchers.Main) {
                                delay(500) // Snapshot availability takes a little time.
                                if (event.mediaThumbnail.existsAndIsNotEmpty()) {
                                    event.mediaThumbnail.copyTo(deviceThumb, true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun decline() {
        call.decline(Reason.Declined)
    }

    fun cancel() {
        call.terminate()
    }

    fun terminate() {
        call.terminate()
    }

    override fun onCleared() {
        call.removeListener(callListener)
        super.onCleared()
    }


    fun toggleMute() {
        val micEnabled = coreContext.core.isMicEnabled
        coreContext.core.isMicEnabled = !micEnabled
        microphoneMuted.value = micEnabled
    }


    fun toggleSpeaker() {
        val audioDevice = coreContext.core.outputAudioDevice
        if (audioDevice?.type == AudioDevice.Type.Speaker) {
            coreContext.core.forceEarpieceAudioRoute()
            speakerDisabled.value = true
        } else {
            coreContext.core.forceSpeakerAudioRoute()
            speakerDisabled.value = false
        }
    }


    fun toggleVideoFullScreen() {
        videoFullScreen.value = !videoFullScreen.value!!
    }

    fun performAction(action: Action) {
        val actionMethodType = device.value?.let { d->
            d.actionsMethodType
        } ?: corePreferences.defaultActionsMethodType
        when (actionMethodType) {
            "method_dtmf_sip_info" -> {
                coreContext.core.useInfoForDtmf = true
                action.code?.let { call.sendDtmfs(it) }
            }
            "method_dtmf_rfc_4733" -> {
                coreContext.core.useRfc2833ForDtmf = true
                action.code?.let { call.sendDtmfs(it) }
            }
            "method_sip_message" -> {
                var params = coreContext.core.createDefaultChatRoomParams()
                params.isGroupEnabled = false
                params.isEncryptionEnabled = false
                var chatRoom = coreContext.core.searchChatRoom(params,call.remoteAddress, call.remoteAddress, arrayOf(call.remoteAddress!!))
                if (chatRoom == null) {
                    chatRoom = coreContext.core.createChatRoom(params, call.remoteAddress,  arrayOf(call.remoteAddress!!))
                }
                val message = chatRoom?.createMessageFromUtf8(action.code)
                message?.send()
            }
        }
    }

    fun extendedAccept() {
        call.extendedAccept()
    }


    // Default actions to be displayed when no device is associated with the call. Taken from configuration.
    //[default_actions]
    //default_actions_method_type=[method_dtmf_sip_info|method_dtmf_rfc_4733|method_sip_message]
    //action_1_type=[action_open_door|action_open_gate|action_lightup|action_unlock] (match name in shared theme action_types.xml)
    //action_1_code=yyyy (dtmf code)
    //action_2... up to 3 actions

    fun defaultActionsIfAny() : ArrayList<Action> {
        var actions = ArrayList<Action>()
        corePreferences.defaultAction1?.also {actions.add(it)}
        corePreferences.defaultAction2?.also {actions.add(it)}
        corePreferences.defaultAction3?.also {actions.add(it)}
        return actions
    }

}
