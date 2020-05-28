package org.lindoor.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.customisation.DeviceTypes
import org.lindoor.entities.Device
import org.lindoor.managers.DeviceManager
import org.lindoor.managers.RecordingsManager
import org.linphone.core.*

class CallViewModelFactory(private val call: Call,val acceptEarlyMedia:Boolean = true, val record:Boolean = true) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CallViewModel(call,acceptEarlyMedia,record) as T
    }
}

class CallViewModel(val call:Call, val autoAcceptEarlyMedia:Boolean,val record:Boolean) : ViewModel() {
    val device: MutableLiveData<Device?> = MutableLiveData(DeviceManager.findDeviceByAddress(call.remoteAddress))
    val defaultDeviceType: MutableLiveData<String?> = MutableLiveData(DeviceTypes.detaultType())
    var callState: MutableLiveData<Call.State> = MutableLiveData(call.state)
    var videoContent: MutableLiveData<Boolean> = MutableLiveData(false)
    private var aVideoFrameWasReceived = false


    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call?, cstate: Call.State?, message: String?) {
            callState.value = cstate
            videoContent.value = hasVideoContent()

            if (autoAcceptEarlyMedia && cstate == Call.State.IncomingReceived) {
                acceptEarlyMedia ()
            }
            if (cstate == Call.State.Released) {
                call?.removeListener(this)
            }
        }

        override fun onNextVideoFrameDecoded(call: Call?) {
            super.onNextVideoFrameDecoded(call)
            aVideoFrameWasReceived = true
            videoContent.value = hasVideoContent()
            device.value?.also {d ->
                if (d.snapshotImage == null)
                    call?.takeVideoSnapshot("${DeviceManager.snapshotsPath.absolutePath}/"+d.id+"+.jpg")
            }
        }
    }

    private fun hasVideoContent():Boolean {
        return (call.remoteParams?.videoEnabled() ?: false) && aVideoFrameWasReceived && (call.state == Call.State.IncomingEarlyMedia || call.state == Call.State.StreamsRunning)
    }

    init {
        call.addListener(callListener)
        if (autoAcceptEarlyMedia && call.state ==  Call.State.IncomingReceived)
            acceptEarlyMedia ()
    }

    fun acceptEarlyMedia() {
        call.requestNotifyNextVideoFrameDecoded()
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
        call.requestNotifyNextVideoFrameDecoded()
        call.acceptWithParams(inCallParams)
        if (record)
            call.startRecording()
    }

    fun decline() {
        call.decline(Reason.Declined)
    }

    override fun onCleared() {
        call.removeListener(callListener)
        super.onCleared()
    }


}
