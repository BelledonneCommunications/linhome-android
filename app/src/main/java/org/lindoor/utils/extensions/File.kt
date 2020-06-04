package org.lindoor.utils.extensions

import java.io.File


fun File.existsAndIsNotEmpty():Boolean {
    return exists() &&  length() != 0L
}