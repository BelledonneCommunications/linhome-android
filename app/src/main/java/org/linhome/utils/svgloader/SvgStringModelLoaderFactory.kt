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

package org.linhome.utils.svgloader

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

/**
 * Decodes an SVG internal representation from an [String].
 */
class SvgStringModelLoaderFactory : ModelLoaderFactory<String, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
        return object : ModelLoader<String, InputStream> {
            override fun handles(model: String) = model.contains("<svg")

            override fun buildLoadData(
                model: String,
                width: Int,
                height: Int,
                options: Options
            ): ModelLoader.LoadData<InputStream>? {
                return ModelLoader.LoadData<InputStream>(
                    Key { messageDigest -> messageDigest.update("svg_string_$model".toByteArray()) },
                    object : DataFetcher<InputStream> {
                        override fun cancel() {}

                        override fun getDataSource() = DataSource.LOCAL

                        override fun loadData(
                            priority: Priority,
                            callback: DataFetcher.DataCallback<in InputStream>
                        ) {
                            callback.onDataReady(model.byteInputStream())
                        }

                        override fun getDataClass() = InputStream::class.java

                        override fun cleanup() {}
                    })
            }
        }
    }

    override fun teardown() {

    }
}