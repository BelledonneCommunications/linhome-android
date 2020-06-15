package org.lindoor.ui.assistant.loginlindoor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_login_lindoor.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.databinding.FragmentAssistantLoginLindoorBinding
import org.lindoor.entities.Account
import org.lindoor.ui.assistant.CreatorAssistantFragment
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil
import org.linphone.core.XmlRpcArgType


class LoginLindoorAccountFragment : CreatorAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginLindoorBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginLindoorAccountViewModel::class.java)
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
                val xmlRpcSession = LindoorApplication.coreContext.core.createXmlRpcSession(
                    corePreferences.xmlRpcServerUrl
                )
                val xmlRpcRequest =
                    xmlRpcSession.createRequest(XmlRpcArgType.String, "check_authentication")
                xmlRpcRequest.setListener { request ->
                    hideProgress()
                    if (request != null) {
                        if (request.stringResponse == "OK") {
                            Account.lindoorAccountLogin(model.accountCreator)
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("lindoor_account_loggedin")
                        } else {
                            binding.root.username.setError(Texts.get("lindoor_account_login_failed_unknown_user_or_wroong_password"))
                        }
                    }
                }
                xmlRpcRequest.addStringArg(model.username.first.value)
                xmlRpcRequest.addStringArg(
                    corePreferences.encryptedPass(
                        model.username.first.value!!,
                        model.pass1.first.value!!
                    )
                )
                xmlRpcRequest.addStringArg(corePreferences.loginDomain)
                xmlRpcSession.sendRequest(xmlRpcRequest)
            }
        }

        return binding.root
    }


}
