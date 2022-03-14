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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.FragmentAssistantCreateLinhomeBinding
import org.linhome.ui.assistant.CreatorAssistantFragment
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
                if (model.accountCreator.createAccount() != AccountCreator.Status.RequestOk) {
                    hideProgress()
                    binding.create.isEnabled = true
                    DialogUtil.error("linhome_account_creation_request_failed")
                }
            }
        }

        return binding.root
    }

}

