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

package org.linhome.ui.assistant.createlinhome

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.FragmentAssistantCreateLinhomeBinding
import org.linhome.ui.assistant.shared.CreatorAssistantFragment
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.utils.DialogUtil
import org.linphone.core.AccountCreator

class CreateLinhomeAccountFragment :
    CreatorAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantCreateLinhomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(CreateLinhomeAccountViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.create.setOnClickListener {
            binding.username.validate()
            binding.email.validate()
            binding.password.validate()
            binding.passwordConfirmation.validate()
            updateField(model.setUsername(model.username), binding.username)
            updateField(model.setPassword(model.pass1), binding.password)
            updateField(model.setEmail(model.email), binding.email)

            if (model.valid()) {
                hideKeyboard()
                showProgress()
                binding.create.isEnabled = false

                model.requestOtp.observe(viewLifecycleOwner, Observer {
                    requestOtp(
                        confirmFunction = { otp ->
                            model.validateOtp(otp)
                        }
                    )
                })

                model.otpError.observe(viewLifecycleOwner, Observer {
                    DialogUtil.confirm(
                        "otp_error_try_again_title",
                        "otp_error_try_again_message",
                        { _: DialogInterface, _: Int ->
                            model.requestOtp.postValue(true)
                        },
                        confirmTextKey = "otp_try_again_button",
                        cancelTextKey = "otp_do_not_try_again",
                        cancelFunction = { _: DialogInterface, _: Int ->
                            hideProgress()
                            DialogUtil.info("otp_cancelled")
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                        })
                })

                model.creationResult.observe(viewLifecycleOwner, Observer { status ->
                    hideProgress()
                    binding.create.isEnabled = true
                    when (status) {
                        AccountCreator.Status.AccountExist -> binding.username.setError(
                            Texts.get(
                                "linhome_account_username_already_exists"
                            )
                        )
                        AccountCreator.Status.AccountCreated -> {
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("linhome_account_created", model.username.first.value!!)
                        }
                        AccountCreator.Status.RequestTooManyRequests -> {
                            binding.username.setError(
                                Texts.get(
                                    "account_creator_token_requests_failed_too_many"
                                )
                            )
                        }
                        AccountCreator.Status.UnexpectedError -> {
                            binding.username.setError(
                                Texts.get(
                                    "account_creator_token_requests_failed_generic"
                                )
                            )
                        }
                        else -> {
                            binding.username.setError(
                                Texts.get(
                                    "linhome_account_creation_failed",
                                    "$status"
                                )
                            )
                        }
                    }
                })
                model.create()
            }
        }

        return binding.root
    }

    fun requestOtp(
        confirmFunction: (String) -> Unit,
    ) {
        DialogUtil.Companion.context?.also { ctx ->
            val input = android.widget.EditText(ctx).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                hint = Texts.get("otp_dialog_hint")
                maxLines = 1
                filters = arrayOf(android.text.InputFilter.LengthFilter(4)) // max 4 digits
                isSingleLine = true
                gravity = android.view.Gravity.CENTER // center text inside box

                val density = resources.displayMetrics.density
                minEms = 4
                maxEms = 4
                setEms(4)
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    (56 * density).toInt(), // ~56dp, fits 4 digits cleanly
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.view.Gravity.CENTER
                )
            }

            val container = android.widget.FrameLayout(ctx).apply {
                val padding = (24 * resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
                addView(input)
            }

            val dialog = MaterialAlertDialogBuilder(ctx, R.style.LindoorDialogTheme)
                .setTitle(Texts.get("otp_dialog_title"))
                .setMessage(Texts.get("otp_dialog_message"))
                .setView(container)
                .setPositiveButton(Texts.get("otp_validate")) { _, _ ->
                    val otp = input.text.toString().trim()
                    if (otp.length == 4) {
                        confirmFunction(otp)
                    } else {
                        Toast.makeText(
                            ctx,
                            Texts.get("otp_invalid_length"),
                            Toast.LENGTH_SHORT
                        ).show()
                        requestOtp(confirmFunction)
                    }
                }
                .setNegativeButton(Texts.get("cancel"), { _: DialogInterface, _: Int ->
                    DialogUtil.info("otp_cancelled")
                    hideProgress()
                    mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                })

            dialog.setCancelable(false)
            dialog.show()

            input.requestFocus()
            val imm = ctx.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

}

