package org.lindoor.ui.assistant.loginlindoor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_login_lindoor.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantLoginLindoorBinding
import org.lindoor.entities.Account
import org.lindoor.entities.AccountType
import org.lindoor.ui.assistant.CreatorAssistantFragment
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil

class LoginLindoorAccountFragment :CreatorAssistantFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginLindoorBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginLindoorAccountViewModel::class.java)
        binding.model = model
        binding.validators  = ValidatorFactory.Companion

        binding.root.login.root.setOnClickListener{
            binding.root.username.validate()
            binding.root.password.validate()
            updateField(model.setUsername(model.username),binding.root.username)
            updateField(model.setPassword(model.pass1),binding.root.password)
            if (model.valid()) {
                hideKeyboard()
                Account.configure(model.accountCreator,AccountType.Lindoor) // TODO Check api availbility to check user / pass instead of creating proxy config & doing SIP Register
                mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                DialogUtil.info("lindoor_account_loggedin")
            }
        }

        return binding.root
    }

}
