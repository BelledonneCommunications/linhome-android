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

package org.linhome.utils

import android.graphics.BitmapFactory
import com.google.android.gms.common.images.Size
import org.linhome.LinhomeApplication
import org.linhome.utils.extensions.existsAndIsNotEmpty
import java.io.File


fun pxFromDp(dp: Int): Int {
    return (dp * LinhomeApplication.instance.resources.displayMetrics.density).toInt()
}

fun pxFromDp(dp: Float): Float {
    return dp * LinhomeApplication.instance.resources.displayMetrics.density
}

fun String.getImageDimension(): Size? {
    val file = File(this)
    if (!file.existsAndIsNotEmpty())
        return null
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(this, options)
    if (options.outWidth > 0 && options.outHeight > 0)
        return Size(options.outWidth, options.outHeight)
    else
        return null
}