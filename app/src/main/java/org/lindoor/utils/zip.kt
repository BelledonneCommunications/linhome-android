package org.lindoor.utils

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