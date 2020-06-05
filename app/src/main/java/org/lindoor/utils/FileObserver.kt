package org.lindoor.utils

import android.os.FileObserver
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


fun fileObserverWithMainThreadRunnable(file: File, runnable:Runnable):FileObserver {
    return object : FileObserver(file,ATTRIB) {
        override fun onEvent(event: Int, file: String?) {
            GlobalScope.launch(context = Dispatchers.Main) {
                runnable.run()
            }
        }
    }
}