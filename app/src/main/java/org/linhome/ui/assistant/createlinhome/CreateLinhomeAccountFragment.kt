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
import kotlinx.android.synthetic.main.fragment_assistant_create_linhome.*
import kotlinx.android.synthetic.main.fragment_assistant_create_linhome.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
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

        binding.root.create.root.setOnClickListener {
            binding.root.username.validate()
            binding.root.email.validate()
            binding.root.password.validate()
            binding.root.password_confirmation.validate()
            updateField(model.setUsername(model.username), binding.root.username)
            updateField(model.setPassword(model.pass1), binding.root.password)
            updateField(model.setEmail(model.email), binding.root.email)

            if (model.valid()) {
                hideKeyboard()
                showProgress()
                create.root.isEnabled = false
                model.creationResult.observe(viewLifecycleOwner, Observer { status ->
                    hideProgress()
                    create.root.isEnabled = true
                    when (status) {
                        AccountCreator.Status.AccountExist -> binding.root.username.setError(
                            Texts.get(
                                "linhome_account_username_already_exists"
                            )
                        )
                        AccountCreator.Status.AccountCreated -> {
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("linhome_account_created", model.username.first.value!!)
                        }
                        else -> {
                            binding.root.username.setError(
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
                    create.root.isEnabled = true
                    DialogUtil.error("linhome_account_creation_request_failed")
                }
            }
        }

        return binding.root
    }

}

