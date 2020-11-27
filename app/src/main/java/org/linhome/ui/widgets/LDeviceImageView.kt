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

package org.linhome.ui.widgets

import android.content.Context
import android.os.FileObserver
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.signature.ObjectKey
import org.linhome.customisation.Theme
import org.linhome.entities.Device
import org.linhome.utils.extensions.existsAndIsNotEmpty
import org.linphone.compatibility.Compatibility.Companion.fileObserverWithMainThreadRunnable
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