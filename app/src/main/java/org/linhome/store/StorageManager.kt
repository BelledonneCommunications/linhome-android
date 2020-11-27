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

package org.linhome.store

import android.os.Environment
import org.linhome.LinhomeApplication
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import java.io.File

object StorageManager {

    // UserData File structure
    // devices.xml --> contains the devices descriptions (Linphone config format)
    // snapshots/<device ID>.jpg -> picture for device in device line, one picture per device, snapthots taken at the very first time a call is made with video content
    // recordings/<unique ID>.mkv -> audio/video recording unique ID is different than call ID as it needs to be set before the call is made. Stores in CallLogUserData.
    // recordings/<unique ID>.jpg -> picture for thumbnail when video recording unique ID is different than call ID as it needs to be set before the call is made. Stores in CallLogUserData.

    val devicesXml = File(LinhomeApplication.instance.filesDir, "devices.xml")
    val historyEventsXml = File(LinhomeApplication.instance.filesDir, "history_events.xml")
    val devicesThumnailPath = File(getUserDataPath(), "devices/thumbnails")
    val callsRecordingsDir = File(getUserDataPath(), "calls/recordings")
    val storePrivately = Theme.arbitraryValue("store_user_data_in_private_storage", false)

    init {
        if (!devicesThumnailPath.exists())
            devicesThumnailPath.mkdirs()
        if (!callsRecordingsDir.exists())
            callsRecordingsDir.mkdirs()
    }


    fun getUserDataPath(): File {
        if (canUseExternalStorage())
            return LinhomeApplication.instance.getExternalFilesDir(Texts.appName)?.let {
                if (!it.exists())
                    it.mkdirs()
                return it
            } ?: LinhomeApplication.instance.filesDir
        else
            return LinhomeApplication.instance.filesDir
    }

    fun canUseExternalStorage(): Boolean { // LindoorApplication.instance.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        return !storePrivately &&
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}