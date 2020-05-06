package org.lindoor.ui.assistant.createlindoor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_create_lindoor.*
import kotlinx.android.synthetic.main.fragment_assistant_create_lindoor.view.*
import kotlinx.android.synthetic.main.fragment_assistant_root.view.create
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.databinding.FragmentAssistantCreateLindoorBinding
import org.lindoor.ui.assistant.CreatorAssistantFragment
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil
import org.linphone.core.AccountCreator

class CreateLindoorAccountFragment :
    CreatorAssistantFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantCreateLindoorBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(CreateLindoorAccountViewModel::class.java)
        binding.model = model
        binding.validators  = ValidatorFactory.Companion

        binding.root.create.root.setOnClickListener{
            binding.root.username.validate()
            binding.root.email.validate()
            binding.root.password.validate()
            binding.root.password_confirmation.validate()
            updateField(model.setUsername(model.username),binding.root.username)
            updateField(model.setPassword(model.pass1),binding.root.password)
            updateField(model.setEmail(model.email),binding.root.email)

            if (model.valid()) {
                hideKeyboard()
                showProgress()
                create.root.isEnabled = false
                model.creationResult.observe(viewLifecycleOwner, Observer { status ->
                    hideProgress()
                    create.root.isEnabled = true
                    when (status) {
                        AccountCreator.Status.AccountExist -> binding.root.username.setError(Texts.get("lindoor_account_username_already_exists"))
                        AccountCreator.Status.AccountCreated -> {
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("lindoor_account_created",model.username.first.value!!)
                        } else -> {
                            binding.root.username.setError(Texts.get("lindoor_account_creation_failed","$status"))
                        }
                    }
                })
                if ( model.accountCreator.createAccount() != AccountCreator.Status.RequestOk) {
                    hideProgress()
                    create.root.isEnabled = true
                    DialogUtil.error("lindoor_account_creation_request_failed")
                }
            }
        }

        return binding.root
    }

}

