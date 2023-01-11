/*
 * Copyright (c) 2010-2021 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
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

import android.annotation.TargetApi
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import org.linphone.core.tools.Log

@TargetApi(31)
class Api31Compatibility {
    companion object {

        fun startForegroundService(service: Service, notifId: Int, notif: Notification?) {
            try {
                service.startForeground(notifId, notif)
            } catch (fssnae: ForegroundServiceStartNotAllowedException) {
                Log.e("[Api31 Compatibility] Can't start service as foreground! $fssnae")
            }
        }

        fun startForegroundService(context: Context, intent: Intent) {
            try {
                context.startForegroundService(intent)
            } catch (fssnae: ForegroundServiceStartNotAllowedException) {
                Log.e("[Api31 Compatibility] Can't start service as foreground! $fssnae")
            }
        }

    }
}
