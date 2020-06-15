package org.lindoor.ui.widgets

import android.content.Context
import android.os.FileObserver
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.signature.ObjectKey
import org.lindoor.customisation.Theme
import org.lindoor.entities.Device
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.fileObserverWithMainThreadRunnable
import java.io.File


class LDeviceImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var deviceImageObserver: FileObserver? = null

    var device: Device? = null
        set(value) {
            value?.thumbNail?.also { thumb ->
                if (thumb.existsAndIsNotEmpty()) {
                    load(thumb)
                    visibility = View.VISIBLE
                } else {
                    visibility = View.INVISIBLE
                }
                addObserver(value.thumbNail)
            }
        }

    private fun addObserver(thumb: File) {
        deviceImageObserver = fileObserverWithMainThreadRunnable(thumb, Runnable {
            load(thumb)
        }).also {
            it.startWatching()
        }
    }

    private fun load(thumb: File) {
        Theme.glidegeneric.load(thumb)
            .signature(ObjectKey(thumb.lastModified()))
            .into(this)

        post {
            scaleType = ScaleType.FIT_XY
            parent.requestLayout()
        }
        addObserver(thumb)
    }

}