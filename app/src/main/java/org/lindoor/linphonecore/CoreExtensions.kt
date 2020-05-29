package org.lindoor.linphonecore

import org.lindoor.LindoorApplication.Companion.coreContext
import org.linphone.core.AudioDevice
import org.linphone.core.Core
import org.linphone.core.ProxyConfig
import org.linphone.core.tools.Log

val Core.firstProxyConfig: ProxyConfig?
    get() {
        return if (proxyConfigList.size > 0) proxyConfigList.get(0) else null
    }

fun Core.forceEarpieceAudioRoute() {
    for (audioDevice in coreContext.core.audioDevices) {
        if (audioDevice.type == AudioDevice.Type.Earpiece) {
            Log.i("[Call] Found earpiece audio device [${audioDevice.deviceName}], routing audio to it")
            coreContext.core.outputAudioDevice = audioDevice
            return
        }
    }
    Log.e("[Call] Couldn't find earpiece audio device")
}

fun Core.forceSpeakerAudioRoute() {
    for (audioDevice in coreContext.core.audioDevices) {
        if (audioDevice.type == AudioDevice.Type.Speaker) {
            Log.i("[Call] Found speaker audio device [${audioDevice.deviceName}], routing audio to it")
            coreContext.core.outputAudioDevice = audioDevice
            return
        }
    }
    Log.e("[Call] Couldn't find speaker audio device")
}

fun Core.forceBluetoothAudioRoute() {
    for (audioDevice in coreContext.core.audioDevices) {
        if ((audioDevice.type == AudioDevice.Type.Bluetooth) && audioDevice.hasCapability(
                AudioDevice.Capabilities.CapabilityPlay)) {
            Log.i("[Call] Found bluetooth audio device [${audioDevice.deviceName}], routing audio to it")
            coreContext.core.outputAudioDevice = audioDevice
            return
        }
    }
    Log.e("[Call] Couldn't find bluetooth audio device")
}