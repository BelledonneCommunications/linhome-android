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

package org.linhome.ui.assistant.loginlinhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_login_linhome.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.FragmentAssistantLoginLinhomeBinding
import org.linhome.entities.Account
import org.linhome.ui.assistant.CreatorAssistantFragment
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.utils.DialogUtil
import org.linphone.core.XmlRpcArgType


class LoginLinhomeAccountFragment : CreatorAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginLinhomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginLinhomeAccountViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.root.login.root.setOnClickListener {
            binding.root.username.validate()
            binding.root.password.validate()
            updateField(model.setUsername(model.username), binding.root.username)
            updateField(model.setPassword(model.pass1), binding.root.password)
            if (model.fieldsValid()) {
                hideKeyboard()
                showProgress()
                val xmlRpcSession = corePreferences.xmlRpcServerUrl?.let { it1 ->
                    LinhomeApplication.coreContext.core.createXmlRpcSession(
                        it1
                    )
                }
                val xmlRpcRequest =
                    xmlRpcSession?.createRequest(XmlRpcArgType.String, "check_authentication")
                xmlRpcRequest?.addListener { request ->
                    hideProgress()
                    if (request != null) {
                        if (request.stringResponse == "OK") {
                            Account.linhomeAccountCreateProxyConfig(model.accountCreator)
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("linhome_account_loggedin")
                        } else {
                            binding.root.username.setError(Texts.get("linhome_account_login_failed_unknown_user_or_wroong_password"))
                        }
                    }
                }
                xmlRpcRequest?.addStringArg(model.username.first.value!!)
                xmlRpcRequest?.addStringArg(
                    corePreferences.encryptedPass(
                        model.username.first.value!!,
                        model.pass1.first.value!!
                    )
                )
                corePreferences.loginDomain?.let { it1 -> xmlRpcRequest?.addStringArg(it1) }
                if (xmlRpcRequest != null) {
                    xmlRpcSession?.sendRequest(xmlRpcRequest)
                }
            }
        }

        return binding.root
    }


}
