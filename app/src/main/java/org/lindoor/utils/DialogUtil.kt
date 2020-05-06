package org.lindoor.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.lindoor.customisation.Texts


class DialogUtil() {
    companion object {

        var context: Context? = null // Multi context as activity can change during calls/idle transition
        val okText = Texts.get("ok")

        fun info(textKey: String, args: Array<String>? = null) {
            context?.also {
                MaterialAlertDialogBuilder(it)
                    .setMessage(Texts.get(textKey, args))
                    .setPositiveButton(okText,null)
                    .show()
            }
        }

        fun info(textKey: String, oneArg: String) {
            info(textKey, arrayOf(oneArg))
        }

        fun error(textKey: String,args: Array<String>? = null, titleKey:String = "generic_dialog_error_title") {
            context?.also {
                MaterialAlertDialogBuilder(it)
                    .setTitle(Texts.get(titleKey, args))
                    .setMessage(Texts.get(textKey, args))
                    .setPositiveButton(okText,null)
                    .show()
            }
        }

        fun error(textKey: String, oneArg: String) {
            error(textKey, arrayOf(oneArg))
        }

        fun confirm(textKey: String, confirmText: String, cancelText: String) {

        }
    }

}