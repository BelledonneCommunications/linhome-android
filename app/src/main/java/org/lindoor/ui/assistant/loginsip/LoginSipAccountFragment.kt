package org.lindoor.ui.assistant.loginsip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_login_sip.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantLoginSipBinding
import org.lindoor.entities.Account
import org.lindoor.ui.assistant.CreatorAssistantFragment
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil
import org.linphone.core.TransportType

class LoginSipAccountFragment :CreatorAssistantFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginSipBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginSipAccountViewModel::class.java)
        binding.model = model
        binding.validators  = ValidatorFactory.Companion

        binding.root.more.root.setOnClickListener {
            model.moreOptionsOpened.value = true
        }

        binding.root.loginsip.root.setOnClickListener{
            binding.root.username.validate()
            binding.root.password.validate()
            binding.root.domain.validate()
            binding.root.proxy.validate()
            binding.root.expiration.validate()
            updateField(model.setUsername(model.username),binding.root.username)
            updateField(model.setPassword(model.pass1),binding.root.password)
            updateField(model.setDomain(model.domain),binding.root.domain)
            model.setTransport(TransportType.values()[model.transport.value!!])
            if (model.valid()) {
                hideKeyboard()
                Account.sipAccountLogin(model.accountCreator,model.proxy.first.value,model.expiration.first.value!!,model.pushReady)
            }
        }
        model.pushReady.observe(viewLifecycleOwner, Observer { status ->
            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
            if (status) {
                DialogUtil.info("sip_account_created")
            } else
                DialogUtil.error("failed_creating_pushgateway")
        })

        return binding.root
    }
}
