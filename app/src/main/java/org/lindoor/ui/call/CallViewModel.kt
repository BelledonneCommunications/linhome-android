package org.lindoor.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.customisation.DeviceTypes
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.lindoor.linphonecore.forceEarpieceAudioRoute
import org.lindoor.linphonecore.forceSpeakerAudioRoute
import org.lindoor.managers.DeviceManager
import org.lindoor.managers.RecordingsManager
import org.linphone.core.*

class CallViewModelFactory(private val call: Call, private val acceptEarlyMedia:Boolean = true, val record:Boolean = true) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CallViewModel(call,acceptEarlyMedia,record) as T
    }
}

class CallViewModel(val call:Call, val autoAcceptEarlyMedia:Boolean,val record:Boolean) : ViewModel() {
    val device: MutableLiveData<Device?> = MutableLiveData(DeviceManager.findDeviceByAddress(call.remoteAddress))
    val defaultDeviceType: MutableLiveData<String?> = MutableLiveData(DeviceTypes.defaultType)

    val callState: MutableLiveData<Call.State> = MutableLiveData(call.state)
    val videoContent: MutableLiveData<Boolean> = MutableLiveData(false)
    val videoFullScreen: MutableLiveData<Boolean> = MutableLiveData(false)

    val speakerEnabled: MutableLiveData<Boolean> = MutableLiveData(coreContext.core.outputAudioDevice?.type == AudioDevice.Type.Speaker)
    val microphoneMuted: MutableLiveData<Boolean> = MutableLiveData(!coreContext.core.micEnabled())

    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call?, cstate: Call.State?, message: String?) {
            callState.postValue(cstate)

            if (autoAcceptEarlyMedia && cstate == Call.State.IncomingReceived) {
                acceptEarlyMedia ()
            }
            if (cstate == Call.State.Released) {
                call?.removeListener(this)
            }
        }

        override fun onNextVideoFrameDecoded(call: Call?) {
            super.onNextVideoFrameDecoded(call)
            videoContent.value = true
            device.value?.also {d ->
                if (d.snapshotImage == null)
                    call?.takeVideoSnapshot("${DeviceManager.snapshotsPath.absolutePath}/"+d.id+"+.jpg")
            }
        }
    }

    init {
        call.addListener(callListener)
        call.requestNotifyNextVideoFrameDecoded()
        if (autoAcceptEarlyMedia && call.state ==  Call.State.IncomingReceived)
            acceptEarlyMedia ()

    }

    fun acceptEarlyMedia() {
        val earlyMediaCallParams:CallParams = coreContext.core.createCallParams(call)
        earlyMediaCallParams.videoDirection = MediaDirection.RecvOnly
        earlyMediaCallParams.audioDirection = MediaDirection.Inactive
        if (record)
            earlyMediaCallParams.recordFile = RecordingsManager.recordingPath(call,earlyMedia = true)
        call.requestNotifyNextVideoFrameDecoded()
        call.acceptEarlyMediaWithParams(earlyMediaCallParams)
        if (record)
            call.startRecording()
    }

    fun accept() {
        val inCallParams:CallParams = coreContext.core.createCallParams(call)
        inCallParams.videoDirection = MediaDirection.RecvOnly
        inCallParams.audioDirection = MediaDirection.SendRecv
        if (record)
            inCallParams.recordFile = RecordingsManager.recordingPath(call,earlyMedia = false)
        call.acceptWithParams(inCallParams)
        call.requestNotifyNextVideoFrameDecoded()
        if (record)
            call.startRecording()
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
            speakerEnabled.value = false
        } else {
            coreContext.core.forceSpeakerAudioRoute()
            speakerEnabled.value = true
        }
    }


    fun toggleVideoFullScreen() {
        videoFullScreen.value = !videoFullScreen.value!!
    }

    fun performAction(action: Action) {
        device.value?.also {d ->
            coreContext.core.useInfoForDtmf = true
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

}
