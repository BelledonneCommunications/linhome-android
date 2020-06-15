package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.store.HistoryEventStore
import org.lindoor.store.StorageManager
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.extensions.xDigitsUUID
import java.io.File


@Parcelize
data class HistoryEvent(
    var id: String = xDigitsUUID(), // HistoryEvent for outgoing call must be created before the call is created to set recording path so it can't be the callID
    var callId: String? = null,
    var viewedByUser: Boolean = false,
    var mediaFileName: String = File(StorageManager.callsRecordingsDir, "${id}.mkv").absolutePath,
    var mediaThumbnailFileName: String = File(
        StorageManager.callsRecordingsDir,
        "${id}.jpg"
    ).absolutePath,
    var hasVideo: Boolean = false
) : Parcelable {

    val media: File
        get() {
            return File(mediaFileName)
        }

    val mediaThumbnail: File
        get() {
            return File(mediaThumbnailFileName)
        }

    fun hasMedia(): Boolean {
        return media.let {
            it.existsAndIsNotEmpty()
        }
    }

    fun hasMediaThumbnail(): Boolean {
        return mediaThumbnail.let {
            it.existsAndIsNotEmpty()
        }
    }

    fun persist() {
        HistoryEventStore.persistHistoryEvent(this)
    }


}
