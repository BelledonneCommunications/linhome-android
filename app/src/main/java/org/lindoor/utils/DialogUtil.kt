package org.lindoor.utils

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.lindoor.customisation.Texts


class DialogUtil() {
    companion object {

        var context: Context? = null // Multi context as activity can change during calls/idle transition

        fun info(textKey: String, args: Array<String>? = null) {
            context?.also {
                MaterialAlertDialogBuilder(it)
                    .setMessage(Texts.get(textKey, args))
                    .setPositiveButton(Texts.get("ok"),null)
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
                    .setPositiveButton(Texts.get("ok"),null)
                    .show()
            }
        }

        fun error(textKey: String, oneArg: String) {
            error(textKey, arrayOf(oneArg))
        }

        fun confirm(titleKey: String, messageKey: String, confirmFonction: (DialogInterface, Int) -> Unit, cancelFunction: ((DialogInterface, Int) -> Unit)? = { _: DialogInterface, _: Int -> },  confirmTextKey:String ? = "confirm", cancelTextKey: String? = "cancel") {
            context?.also {
                MaterialAlertDialogBuilder(it)
                    .setTitle(Texts.get(titleKey))
                    .setMessage(Texts.get(messageKey))
                    .setPositiveButton(Texts.get(confirmTextKey!!),DialogInterface.OnClickListener(function = confirmFonction))
                    .setNegativeButton(Texts.get(cancelTextKey!!),DialogInterface.OnClickListener(function = cancelFunction!!))
                    .show()
            }
        }

        fun toast(textKey: String, long:Boolean = false) {
            Toast.makeText(context,Texts.get(textKey),if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }
    }

}