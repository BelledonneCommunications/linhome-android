package org.lindoor.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.customisation.DeviceTypes
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.lindoor.entities.HistoryEvent
import org.lindoor.linphonecore.extensions.*
import org.lindoor.store.DeviceStore
import org.lindoor.utils.cdlog
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.CallListenerStub
import org.linphone.core.Reason


class CallViewModelFactory(private val call: Call) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CallViewModel(call) as T
    }
}

class CallViewModel(val call: Call) : ViewModel() {
    val device: MutableLiveData<Device?> =
        MutableLiveData(DeviceStore.findDeviceByAddress(call.remoteAddress))
    val defaultDeviceType: MutableLiveData<String?> = MutableLiveData(DeviceTypes.defaultType)

    val callState: MutableLiveData<Call.State> = MutableLiveData(call.state)
    val videoContent: MutableLiveData<Boolean> = MutableLiveData(false)
    val videoFullScreen: MutableLiveData<Boolean> = MutableLiveData(false)

    val speakerDisabled: MutableLiveData<Boolean> =
        MutableLiveData(coreContext.core.outputAudioDevice?.type != AudioDevice.Type.Speaker)
    val microphoneMuted: MutableLiveData<Boolean> = MutableLiveData(!coreContext.core.micEnabled())

    private var historyEvent: HistoryEvent

    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call?, cstate: Call.State?, message: String?) {
            cstate?.also { state ->
                if (call != null) {
                    historyEvent = call.callLog.historyEvent()
                }
                fireActionsOnCallStateChanged(state)
                attemptSetDeviceThumbnail(state)
                call?.remoteParams?.videoEnabled()?.also {
                    call.requestNotifyNextVideoFrameDecoded()
                }
                callState.value = state
            }

        }

        override fun onNextVideoFrameDecoded(call: Call?) {
            videoContent.value = true
            call?.callLog?.historyEvent()?.also { event ->
                if (!event.hasVideo) {
                    event.hasVideo = true
                    event.persist()
                }
                if (!event.mediaThumbnail.existsAndIsNotEmpty()) {
                    call.takeVideoSnapshot(event.mediaThumbnail.absolutePath)
                }
            }
        }
    }

    init {
        historyEvent = call.callLog.historyEvent()
        call.addListener(callListener)
        fireActionsOnCallStateChanged(call.state)
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
        val micEnabled = coreContext.core.micEnabled()
        coreContext.core.enableMic(!micEnabled)
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
        device.value?.also { d ->
            when (d.actionsMethodType) {
                "method_dtmf_sip_info" -> {
                    coreContext.core.useInfoForDtmf = true
                    call.sendDtmfs(action.code)
                }
                "method_dtmf_rfc_4733" -> {
                    coreContext.core.useRfc2833ForDtmf = true
                    call.sendDtmfs(action.code)
                }
                "method_sip_message" -> {
                    val message = coreContext.core.createInfoMessage()
                    val content = coreContext.core.createContent()
                    content.type = "text/plain"
                    action.code?.length?.let { content.setBuffer(action.code.toByteArray(), it) }
                    message.content = content
                    call.sendInfoMessage(message)
                }
            }
        }
    }

    fun extendedAccept() {
        call.extendedAccept()
    }

}
