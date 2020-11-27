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

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.linhome.R
import org.linhome.customisation.Texts


class DialogUtil {
    companion object {

        var context: Context? =
            null // Multi context as activity can change during calls/idle transition

        fun info(textKey: String, args: Array<String>? = null) {
            context?.also {
                MaterialAlertDialogBuilder(it, R.style.LindoorDialogTheme)
                    .setMessage(Texts.get(textKey, args))
                    .setPositiveButton(Texts.get("ok"), null)
                    .show()
            }
        }

        fun info(textKey: String, oneArg: String) {
            info(textKey, arrayOf(oneArg))
        }

        fun error(
            textKey: String,
            args: Array<String>? = null,
            titleKey: String = "generic_dialog_error_title",
            postAction: ((DialogInterface, Int) -> Unit) ? = null
        ) {
            context?.also {
                val dialog = MaterialAlertDialogBuilder(it, R.style.LindoorDialogTheme)
                    .setTitle(Texts.get(titleKey, args))
                    .setMessage(Texts.get(textKey, args))
                    .setPositiveButton(Texts.get("ok"), null)
                if (postAction != null) {
                    dialog.setOnDismissListener { dialog ->
                        postAction.invoke(dialog, 0)
                    }
                }
                dialog.show()
            }
        }

        fun error(textKey: String, oneArg: String) {
            error(textKey, arrayOf(oneArg))
        }

        fun error(textKey: String, postAction: ((DialogInterface, Int) -> Unit)) {
            error(textKey, null,"generic_dialog_error_title",postAction)
        }

        fun confirm(
            titleKey: String? = null,
            messageKey: String,
            confirmFonction: (DialogInterface, Int) -> Unit,
            cancelFunction: ((DialogInterface, Int) -> Unit)? = { _: DialogInterface, _: Int -> },
            confirmTextKey: String? = "confirm",
            cancelTextKey: String? = "cancel",
            oneArg: String? = null
        ) {
            context?.also {
                val dialog = MaterialAlertDialogBuilder(it, R.style.LindoorDialogTheme)
                    .setMessage(
                        if (oneArg != null) Texts.get(messageKey, oneArg) else Texts.get(
                            messageKey
                        )
                    )
                    .setPositiveButton(
                        Texts.get(confirmTextKey!!),
                        DialogInterface.OnClickListener(function = confirmFonction)
                    )
                    .setNegativeButton(
                        Texts.get(cancelTextKey!!),
                        DialogInterface.OnClickListener(function = cancelFunction!!)
                    )
                titleKey?.also {
                    dialog.setTitle(Texts.get(it))
                }
                dialog.setOnDismissListener { dialog ->
                    cancelFunction.invoke(dialog, 0)
                }
                dialog.setCancelable(false)
                dialog.show()
            }
        }

        fun confirm(
            messageKey: String,
            confirmFonction: (DialogInterface, Int) -> Unit,
            cancelFunction: ((DialogInterface, Int) -> Unit)? = { _: DialogInterface, _: Int -> },
            confirmTextKey: String? = "confirm",
            cancelTextKey: String? = "cancel",
            oneArg: String? = null
        ) {
            confirm(
                null,
                messageKey,
                confirmFonction,
                cancelFunction,
                confirmTextKey,
                cancelTextKey,
                oneArg
            )
        }

        fun confirm(
            messageKey: String,
            confirmFonction: (DialogInterface, Int) -> Unit,
            oneArg: String? = null,
            cancelFunction: ((DialogInterface, Int) -> Unit)? = { _: DialogInterface, _: Int -> }
        ) {
            confirm(null, messageKey, confirmFonction, cancelFunction, "confirm", "cancel", oneArg)
        }

        fun toast(textKey: String, long: Boolean = false) {
            context?.also {
                Toast.makeText(
                    context,
                    Texts.get(textKey),
                    if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}