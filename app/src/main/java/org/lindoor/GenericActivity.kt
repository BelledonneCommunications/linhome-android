/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
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

package org.lindoor

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.ensureCoreExists
import org.lindoor.utils.DialogUtil

abstract class GenericActivity(val allowsLandscapeOnSmartPhones: Boolean = false) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureCoreExists(applicationContext)

    }

    override fun onResume() {
        super.onResume()
        DialogUtil.context = this
        LindoorApplication.someActivityRunning = true

        // Remove service notification if it has been started by device boot
        coreContext.notificationsManager.stopForegroundNotificationIfPossible()

        if(!allowsLandscapeOnSmartPhones && !tablet()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    fun tablet(): Boolean {
        return resources.getBoolean(R.bool.tablet)
    }

    override fun onPause() {
        if (DialogUtil.context == this)
            DialogUtil.context = null
        LindoorApplication.someActivityRunning = false
        super.onPause()
    }


}
