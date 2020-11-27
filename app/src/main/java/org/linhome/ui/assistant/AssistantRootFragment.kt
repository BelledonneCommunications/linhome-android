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

package org.linhome.ui.assistant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_assistant_create_linhome.view.*
import kotlinx.android.synthetic.main.fragment_assistant_create_linhome.view.create
import kotlinx.android.synthetic.main.fragment_assistant_root.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.linhome.GenericFragment
import org.linhome.R
import org.linhome.databinding.FragmentAssistantRootBinding
import org.linhome.entities.Account
import org.linhome.utils.DialogUtil

class AssistantRootFragment : GenericFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAssistantRootBinding.inflate(inflater, container, false)

        binding.root.create.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_create_linhome)
        }
        binding.root.use.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_login_linhome)
        }
        binding.root.sip.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_login_sip)
        }
        binding.root.remote.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_remote_root)
        }
        return binding.root
    }

    private fun navigateToCompotent(componentResource: Int)  {
        if (Account.configured()) {
            DialogUtil.confirm(
                "assistant_using_will_disconnect_title",
                "assistant_using_will_disconnect_message",
                { _: DialogInterface, _: Int ->
                    Account.disconnect()
                    mainactivity.navController.navigate(componentResource)
                })
        } else
            mainactivity.navController.navigate(componentResource)
    }

}
