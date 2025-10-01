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
package org.linhome.linphonecore


import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_PHONE_STATE
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.R
import org.linhome.compatibility.Compatibility
import org.linhome.linphonecore.extensions.extendedAccept
import org.linhome.notifications.NotificationsManager
import org.linhome.ui.call.CallInProgressActivity
import org.linhome.ui.call.CallIncomingActivity
import org.linhome.ui.call.CallOutgoingActivity
import org.linhome.utils.DialogUtil
import org.linphone.compatibility.PhoneStateInterface
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Config
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.GlobalState
import org.linphone.core.Player
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.mediastream.Log
import org.linphone.mediastream.Version
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class CoreContext(
    val context: Context,
    coreConfig: Config,
    service: CoreService? = null,
    useAutoStartDescription: Boolean = false
) {
    var stopped = false
    val core: Core
    var closeAppUponCallFinish = false
    private var phoneStateListener: PhoneStateInterface? = null

    val appVersion: String by lazy {
        "${org.linhome.BuildConfig.VERSION_NAME} / versionCode: ${org.linhome.BuildConfig.VERSION_CODE}  (${org.linhome.BuildConfig.BUILD_TYPE})"
    }

    val sdkVersion: String by lazy {
        val sdkVersion = context.getString(R.string.linphone_sdk_version)
        val sdkBranch = context.getString(R.string.linphone_sdk_branch)
        "$sdkVersion ($sdkBranch)"
    }


    val notificationsManager: NotificationsManager by lazy {
        NotificationsManager(context)
    }

    private var overlayX = 0f
    private var overlayY = 0f
    private var callOverlay: View? = null
    private var isVibrating = false

    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    fun gsmCallActive() : Boolean {
        return phoneStateListener?.isInCall() == true
    }

    private val listener: CoreListenerStub = object : CoreListenerStub() {

        override fun onPushNotificationReceived(core: Core, payload: String?) {
            org.linphone.core.tools.Log.i("[Context] Push notification received: $payload")
        }

        override fun onGlobalStateChanged(core: Core, state: GlobalState, message: String) {
            Log.i("[Context] Global state changed [$state]")
            if (state == GlobalState.On) {
                Log.d("[Context] Configuration is \n : ${core.config.dump()}")
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State,
            message: String
        ) {
            Log.i("[Context] Call state changed [$state]")
            if (state == Call.State.IncomingReceived) {

                if (core.callsNb > 1) {
                    call.decline(Reason.Busy)
                    return
                    }

                if (gsmCallActive()) {
                    Log.w("[Context] Receiving a Linhome call while a GSM call is active")
                }

                if (core.callsNb == 1 && corePreferences.vibrateWhileIncomingCall) {
                    val audioManager =
                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    if ((audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE ||
                                audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL)
                    ) {
                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (vibrator.hasVibrator()) {
                            Log.i("[Context] Starting incoming call vibration")
                            Compatibility.vibrate(vibrator)
                            isVibrating = true
                        }
                    }
                }

                // Starting SDK 24 (Android 7.0) we rely on the fullscreen intent of the call incoming notification
                if (Version.sdkStrictlyBelow(Version.API24_NOUGAT_70) || LinhomeApplication.someActivityRunning) {
                    onIncomingReceived(call)
                }

                if (corePreferences.autoAnswerEnabled) {
                    val autoAnswerDelay = corePreferences.autoAnswerDelay
                    if (autoAnswerDelay == 0) {
                        Log.w("[Context] Auto answering call immediately")
                        answerCall(call)
                    } else {
                        val timer = Timer("Auto answer scheduler")
                        Log.i("[Context] Scheduling auto answering in $autoAnswerDelay milliseconds")
                        timer.schedule(object : TimerTask() {
                            override fun run() {
                                Log.w("[Context] Auto answering call")
                                answerCall(call)
                            }
                        }, autoAnswerDelay.toLong())
                    }
                }
            } else if (state == Call.State.OutgoingInit || state == Call.State.OutgoingProgress) {
                onOutgoingStarted(call)
            } else if (state == Call.State.Connected) {
                if (isVibrating) {
                    Log.i("[Context] Stopping vibration")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.cancel()
                    isVibrating = false
                }

                onCallStarted(call)
            } else if (state == Call.State.End || state == Call.State.Error || state == Call.State.Released) {
                if (core.callsNb == 0) {
                    if (isVibrating) {
                        Log.i("[Context] Stopping vibration")
                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.cancel()
                        isVibrating = false
                    }

                    removeCallOverlay()
                }
            }
            if (state == Call.State.Error && call.callLog?.dir == Call.Dir.Outgoing) {
                GlobalScope.launch(context = Dispatchers.Main) {
                    delay(250)
                    DialogUtil.error("unable_to_call_device")
                }
            }
        }
    }

    init {
        if (service != null) {
            org.linphone.core.tools.Log.i("[Context] Starting foreground service")
            notificationsManager.startForegroundToKeepAppAlive(service, useAutoStartDescription)
        }
        core = Factory.instance().createCoreWithConfig(coreConfig, context)
        stopped = false
        Log.i("[Context] Ready")
    }


    fun start(isPush: Boolean = false, startService: Boolean = true) {
        Log.i("[Context] Starting")

        notificationsManager.onCoreReady()

        core.addListener(listener)
        if (isPush) {
            Log.i("[Context] Push received, assume in background")
            core.enterBackground()
        }
        core.config.setBool("net", "use_legacy_push_notification_params", true)
        core.start()

        configureCore()

        initPhoneStateListener()

        if (corePreferences.keepServiceAlive && Compatibility.hasPermission(context,
                POST_NOTIFICATIONS)) {
            org.linphone.core.tools.Log.i("[Context] Background mode setting is enabled, starting Service")
            notificationsManager.startForeground()
        }

    }

    fun stop() {
        Log.i("[Context] Stopping")

        notificationsManager.destroy()

        core.stop()
        core.removeListener(listener)
        stopped = true
    }

    private fun configureCore() {
        Log.i("[Context] Configuring Core")

        core.zrtpSecretsFile = context.filesDir.absolutePath + "/zrtp_secrets"

        initUserCertificates()

        computeUserAgent()

        Log.i("[Context] Core configured")
    }

    private fun computeUserAgent() {
        val deviceName: String = corePreferences.deviceName
        val appName: String = context.resources.getString(R.string.app_name)
        val androidVersion = org.linhome.BuildConfig.VERSION_NAME
        val userAgent = "$deviceName $appName $androidVersion linphone-sdk"
        val sdkVersion = context.getString(R.string.linphone_sdk_version)
        val sdkBranch = context.getString(R.string.linphone_sdk_branch)
        val sdkUserAgent = "$sdkVersion ($sdkBranch)"
        core.setUserAgent(userAgent, sdkUserAgent)
    }

    private fun initUserCertificates() {
        val userCertsPath = context.filesDir.absolutePath + "/user-certs"
        val f = File(userCertsPath)
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e("[Context] $userCertsPath can't be created.")
            }
        }
        core.userCertificatesPath = userCertsPath
    }

    /* Call related functions */

    fun answerCall(call: Call) {
        Log.i("[Context] Answering call $call")
        call.extendedAccept()
    }

    fun declineCall(call: Call) {
        Log.i("[Context] Declining call $call")
        call.decline(Reason.Declined)
    }

    fun terminateCall(call: Call) {
        Log.i("[Context] Terminating call $call")
        call.terminate()
    }

    fun transferCallTo(addressToCall: String) {
        val currentCall = core.currentCall ?: core.calls.first()
        if (currentCall == null) {
            Log.e("[Context] Couldn't find a call to transfer")
        } else {
            Log.i("[Context] Transferring current call to $addressToCall")
            currentCall.transfer(addressToCall)
        }
    }

    fun pixelsToDp(pixels: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels,
            context.resources.displayMetrics
        )
    }

    fun createCallOverlay() {
        if (!corePreferences.showCallOverlay || callOverlay != null) {
            return
        }

        if (overlayY == 0f) overlayY = pixelsToDp(40f)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Compatibility.getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.x = overlayX.toInt()
        params.y = overlayY.toInt()
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        val overlay = LayoutInflater.from(context).inflate(R.layout.call_overlay, null)

        var initX = overlayX
        var initY = overlayY
        overlay.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = params.x - event.rawX
                    initY = params.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = (event.rawX + initX).toInt()
                    val y = (event.rawY + initY).toInt()

                    params.x = x
                    params.y = y
                    windowManager.updateViewLayout(overlay, params)
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(overlayX - params.x) < 5 && abs(overlayY - params.y) < 5) {
                        core.currentCall?.also {
                            onCallStarted(it)
                        }
                    }
                    overlayX = params.x.toFloat()
                    overlayY = params.y.toFloat()
                }
                else -> false
            }
            true
        }

        callOverlay = overlay
        windowManager.addView(overlay, params)
    }

    fun removeCallOverlay() {
        if (callOverlay != null) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(callOverlay)
            callOverlay = null
        }
    }

    fun checkIfForegroundServiceNotificationCanBeRemovedAfterDelay(delayInMs: Long) {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                delay(delayInMs)
                withContext(Dispatchers.Main) {
                    if (core.defaultAccount != null && core.defaultAccount?.state == RegistrationState.Ok) {
                        org.linphone.core.tools.Log.i("[Context] Default account is registered, cancel foreground service notification if possible")
                        notificationsManager.stopForegroundNotificationIfPossible()
                    }
                }
            }
        }
    }

    /* Start call related activities */

    private fun onIncomingReceived(call: Call) {
        val intent = Intent(context, CallIncomingActivity::class.java)
        intent.putExtra("callId", call.callLog.callId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun onOutgoingStarted(call: Call) {
        val intent = Intent(context, CallOutgoingActivity::class.java)
        intent.putExtra("callId", call.callLog.callId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun onCallStarted(call: Call) {
        val intent = Intent(context, CallInProgressActivity::class.java)
        intent.putExtra("callId", call.callLog.callId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Player

    var sharedPlayer : Player? = null

    fun getPlayer() : Player? {
        var speakerCard: String? = null
        var earpieceCard: String? = null
        for (device in LinhomeApplication.coreContext.core.audioDevices) {
            if (device.hasCapability(AudioDevice.Capabilities.CapabilityPlay)) {
                if (device.type == AudioDevice.Type.Speaker) {
                    speakerCard = device.id
                } else if (device.type == AudioDevice.Type.Earpiece) {
                    earpieceCard = device.id
                }
            }
        }

        sharedPlayer = LinhomeApplication.coreContext.core.createLocalPlayer(
            speakerCard ?: earpieceCard,
            null,
            null
        )

        return sharedPlayer
    }

    fun initPhoneStateListener() {
        Log.i("[Context] Registering notification_phone state listener")
        if (Compatibility.hasPermission(context,READ_PHONE_STATE)) {
            try {
                phoneStateListener =
                    Compatibility.createPhoneListener(
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    )
            } catch (exception: SecurityException) {
                val hasReadPhoneStatePermission = Compatibility.hasPermission(context,READ_PHONE_STATE)
                Log.e("[Context] Failed to create phone state listener: $exception, READ_PHONE_STATE permission status is $hasReadPhoneStatePermission")
            }
        } else {
            Log.w("[Context] Can't create phone state listener, READ_PHONE_STATE permission isn't granted")
        }
    }
}
