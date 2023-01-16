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

package org.linhome.ui.assistant.loginsip

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.linhome.R
import org.linhome.databinding.FragmentAssistantLoginSipBinding
import org.linhome.entities.Account
import org.linhome.ui.assistant.CreatorAssistantFragment
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.utils.DialogUtil
import org.linphone.core.TransportType

class LoginSipAccountFragment : CreatorAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginSipBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginSipAccountViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.more.setOnClickListener {
            model.moreOptionsOpened.value = true
        }

        binding.loginsip.setOnClickListener {
            binding.username.validate()
            binding.password.validate()
            binding.domain.validate()
            binding.proxy.validate()
            binding.expiration.validate()
            updateField(model.setUsername(model.username), binding.username)
            updateField(model.setPassword(model.pass1), binding.password)
            updateField(model.setDomain(model.domain), binding.domain)
            model.setTransport(TransportType.values()[model.transport.value!!])
            if (model.valid()) {
                hideKeyboard()
                showProgress()
                Account.sipAccountLogin(
                    model.accountCreator,
                    model.proxy.first.value,
                    model.expiration.first.value!!,
                    model.pushReady,
                    model.sipRegistered
                )
            } else {
                DialogUtil.toast("ouille")
            }
        }
        model.pushReady.observe(viewLifecycleOwner, Observer { pushready ->
            hideProgress()
            if (pushready) {
                DialogUtil.info("sip_account_created")
                mainactivity.navController.popBackStack(R.id.navigation_devices, false)
            } else
                DialogUtil.error("failed_creating_pushgateway")
        })

        model.sipRegistered.observe(viewLifecycleOwner, Observer { sipRegistered ->
            hideProgress()
            if (!sipRegistered) {
                DialogUtil.confirm(
                    null,
                    "failed_sip_login_modify_parameters",
                    { _: DialogInterface, _: Int ->
                        Account.delete()
                    },
                    cancelTextKey = "no",
                    confirmTextKey = "yes")
            }
        })


        return binding.root
    }
}
