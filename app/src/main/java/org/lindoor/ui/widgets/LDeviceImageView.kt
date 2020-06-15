package org.lindoor.ui.widgets

import android.content.Context
import android.graphics.Color
import android.os.FileObserver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.customisation.Theme
import org.lindoor.databinding.WidgetRoundRectButtonWithIconBinding
import org.lindoor.entities.Device
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.fileObserverWithMainThreadRunnable
import java.io.File


class LDeviceImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

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
                addObserver(thumb)
            }
        }

    private fun addObserver(thumb:File) {
        deviceImageObserver = fileObserverWithMainThreadRunnable(thumb,Runnable {
            load(thumb)
        }).also {
            it.startWatching()
        }
    }

    private fun load(thumb:File) {
        Theme.glidegeneric.load(thumb)
            .signature(ObjectKey(thumb.lastModified()))
            .into(this)

        post {
            scaleType = ImageView.ScaleType.FIT_XY
            parent.requestLayout()
        }
        addObserver(thumb)
    }

}