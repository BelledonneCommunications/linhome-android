package org.lindoor.store

import android.os.Environment
import org.lindoor.LindoorApplication
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import java.io.File

object StorageManager {

    // UserData File structure
    // devices.xml --> contains the devices descriptions (Linphone config format)
    // snapshots/<device ID>.jpg -> picture for device in device line, one picture per device, snapthots taken at the very first time a call is made with video content
    // recordings/<unique ID>.mkv -> audio/video recording unique ID is different than call ID as it needs to be set before the call is made. Stores in CallLogUserData.
    // recordings/<unique ID>.jpg -> picture for thumbnail when video recording unique ID is different than call ID as it needs to be set before the call is made. Stores in CallLogUserData.

    val devicesXml = File(LindoorApplication.instance.filesDir, "devices.xml")
    val historyEventsXml = File(LindoorApplication.instance.filesDir, "history_events.xml")
    val devicesThumnailPath = File(getUserDataPath(),"devices/thumbnails")
    val callsRecordingsDir = File(getUserDataPath(),"calls/recordings")
    val storePrivately = Theme.arbitraryValue("store_user_data_in_private_storage",false)

    init {
        if (!devicesThumnailPath.exists())
            devicesThumnailPath.mkdirs()
        if (!callsRecordingsDir.exists())
            callsRecordingsDir.mkdirs()
    }


    fun getUserDataPath(): File {
        if (canUseExternalStorage())
            return LindoorApplication.instance.getExternalFilesDir(Texts.appName)?.let {
                if (!it.exists())
                    it.mkdirs()
                return it
            } ?:  LindoorApplication.instance.filesDir
        else
            return  LindoorApplication.instance.filesDir
    }

    fun canUseExternalStorage(): Boolean { // LindoorApplication.instance.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        return !storePrivately &&
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}