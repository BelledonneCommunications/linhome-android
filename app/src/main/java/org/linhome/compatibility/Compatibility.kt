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
package org.linhome.compatibility

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.FileObserver
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linphone.compatibility.Api34Compatibility
import org.linphone.compatibility.Api35Compatibility
import org.linphone.compatibility.PhoneStateInterface
import org.linphone.compatibility.PhoneStateListener
import org.linphone.compatibility.TelephonyListener
import org.linphone.mediastream.Version
import java.io.File


@SuppressLint("NewApi")
class Compatibility {

    companion object {

        fun setupAppStartupListener(context: Context) {
            if (Version.sdkAboveOrEqual(Version.API35_ANDROID_15_VANILLA_ICE_CREAM)) {
                Api35Compatibility.setupAppStartupListener(context)
            }
        }

        fun getChannelImportance(
            notificationManager: NotificationManagerCompat,
            channelId: String
        ): Int {
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                return Api26Compatibility.getChannelImportance(notificationManager, channelId)
            }
            return NotificationManagerCompat.IMPORTANCE_DEFAULT
        }

        fun startForegroundService(context: Context, intent: Intent) {
            if (Version.sdkAboveOrEqual(Version.API31_ANDROID_12)) {
                Api31Compatibility.startForegroundService(context, intent)
            } else if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                Api26Compatibility.startForegroundService(context, intent)
            } else {
                Api23Compatibility.startForegroundService(context, intent)
            }
        }

        fun startForegroundService(service: Service, notifId: Int, notif: Notification?) {
            if (Version.sdkAboveOrEqual(Version.API31_ANDROID_12)) {
                Api31Compatibility.startForegroundService(service, notifId, notif)
            } else {
                Api23Compatibility.startForegroundService(service, notifId, notif)
            }
        }


        fun hasPermission(context: Context, permission: String): Boolean {
            return when (Version.sdkAboveOrEqual(Version.API23_MARSHMALLOW_60)) {
                true -> Api23Compatibility.hasPermission(context, permission)
                else -> context.packageManager.checkPermission(
                    permission,
                    context.packageName
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun getDeviceName(context: Context): String {
            return when (Version.sdkAboveOrEqual(Version.API25_NOUGAT_71)) {
                true -> Api25Compatibility.getDeviceName(context)
                else -> Api23Compatibility.getDeviceName(context)
            }
        }

        /* Notifications */

        fun createNotificationChannels(
            context: Context,
            notificationManager: NotificationManagerCompat
        ) {
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                Api26Compatibility.createServiceChannel(context, notificationManager)
                Api26Compatibility.createIncomingCallChannel(context, notificationManager)
            }
        }

        fun getOverlayType(): Int {
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                return Api26Compatibility.getOverlayType()
            }
            return WindowManager.LayoutParams.TYPE_PHONE
        }

        /* Call */

        fun canDrawOverlay(context: Context): Boolean {
            if (Version.sdkAboveOrEqual(Version.API23_MARSHMALLOW_60)) {
                return Api23Compatibility.canDrawOverlay(context)
            }
            return false
        }

        fun enterPipMode(activity: Activity) {
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                Api26Compatibility.enterPipMode(activity)
            }
        }

        fun vibrate(vibrator: Vibrator) {
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                Api26Compatibility.vibrate(vibrator)
            } else {
                Api23Compatibility.vibrate(vibrator)
            }
        }

        fun vibrateOneShot() {
            val v =
                LinhomeApplication.coreContext.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
                Api26Compatibility.vibrateOneShot(v)
            } else {
                Api23Compatibility.vibrateOneShot(v)
            }
        }

        fun setColorFilter(
            drawable: Drawable,
            color: Int
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            } else {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }

        /* UI */

        fun setShowWhenLocked(activity: Activity, enable: Boolean) {
            if (Version.sdkStrictlyBelow(Version.API27_OREO_81)) {
                Api23Compatibility.setShowWhenLocked(activity, enable)
            } else {
                Api28Compatibility.setShowWhenLocked(activity, enable)
            }
        }

        fun setTurnScreenOn(activity: Activity, enable: Boolean) {
            if (Version.sdkStrictlyBelow(Version.API27_OREO_81)) {
                Api23Compatibility.setTurnScreenOn(activity, enable)
            } else {
                Api28Compatibility.setTurnScreenOn(activity, enable)
            }
        }

        fun requestDismissKeyguard(activity: Activity) {
            if (Version.sdkStrictlyBelow(Version.API27_OREO_81)) {
                Api23Compatibility.requestDismissKeyguard(activity)
            } else {
                Api28Compatibility.requestDismissKeyguard(activity)
            }
        }



        // Linhome

        fun fileObserverWithMainThreadRunnable(file: File, runnable: Runnable): FileObserver {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return object : FileObserver(file, ATTRIB) {
                    override fun onEvent(event: Int, file: String?) {
                        GlobalScope.launch(context = Dispatchers.Main) {
                            runnable.run()
                        }
                    }
                }
            } else {
                return object : FileObserver(file.absolutePath, ATTRIB) {
                    override fun onEvent(event: Int, file: String?) {
                        GlobalScope.launch(context = Dispatchers.Main) {
                            runnable.run()
                        }
                    }
                }
            }
        }
        fun startCallForegroundService(
            service: Service,
            notifId: Int,
            notif: Notification,
            isCallActive: Boolean
        ) {
            if (Version.sdkAboveOrEqual(Version.API34_ANDROID_14_UPSIDE_DOWN_CAKE)) {
                Api34Compatibility.startCallForegroundService(service, notifId, notif, isCallActive)
            } else {
                startForegroundService(service, notifId, notif)
            }
        }

        fun startDataSyncForegroundService(
            service: Service,
            notifId: Int,
            notif: Notification,
            isCallActive: Boolean
        ) {
            if (Version.sdkAboveOrEqual(Version.API34_ANDROID_14_UPSIDE_DOWN_CAKE)) {
                Api34Compatibility.startDataSyncForegroundService(
                    service,
                    notifId,
                    notif,
                    isCallActive
                )
            } else {
                startForegroundService(service, notifId, notif)
            }
        }

        fun createPhoneListener(telephonyManager: TelephonyManager): PhoneStateInterface {
            return if (Version.sdkStrictlyBelow(Version.API31_ANDROID_12)) {
                PhoneStateListener(telephonyManager)
            } else {
                TelephonyListener(telephonyManager)
            }
        }
    }

}
