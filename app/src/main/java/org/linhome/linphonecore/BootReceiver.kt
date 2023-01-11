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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.R
import org.linphone.core.tools.service.CoreService
import org.linphone.mediastream.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(Intent.ACTION_MAIN).setClass(context, CoreService::class.java)
        if (intent.action.equals(Intent.ACTION_SHUTDOWN, ignoreCase = true)) {
            android.util.Log.d(
                context.getString(R.string.app_name),
                "[Boot Receiver] Device is shutting down, destroying Core to unregister"
            )
            context.stopService(serviceIntent)
        } else if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
            val autoStart = corePreferences.autoStart
            Log.i("[Boot Receiver] Device is starting, autoStart is $autoStart")
            if (autoStart) {
                serviceIntent.putExtra("StartForeground", true)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}
