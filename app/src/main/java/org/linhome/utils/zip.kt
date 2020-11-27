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

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object zip {
    fun unzipInputStream(zipInputStream: InputStream, folderName: String): Boolean {
        val folder = File(folderName)
        if (!folder.isDirectory || !folder.exists()) {
            folder.mkdirs()
        }
        try {
            val targetDir = File(folderName)
            zipInputStream.reset()
            val zin = ZipInputStream(zipInputStream)
            var ze: ZipEntry?
            while (zin.nextEntry.also { ze = it } != null) {
                if (!ze!!.isDirectory) {
                    val f = File(targetDir.toString() + "/" + ze!!.name)
                    f.parentFile?.mkdirs()
                    val fout = FileOutputStream(f)
                    val bufout = BufferedOutputStream(fout)
                    val buffer = ByteArray(4096)
                    var read: Int
                    while (zin.read(buffer).also { read = it } != -1) {
                        bufout.write(buffer, 0, read)
                    }
                    zin.closeEntry()
                    bufout.close()
                    fout.close()
                }
            }
            zin.close()
            zipInputStream.close()
            android.util.Log.i("", "[ZIP] Successfully unzipped input stream to $folderName")
            return true

        } catch (e: Exception) {
            android.util.Log.e(
                "",
                "[ZIP] unzipInputStream operation on $zipInputStream failed : $e"
            )
            e.printStackTrace()
            return false
        }
    }
}