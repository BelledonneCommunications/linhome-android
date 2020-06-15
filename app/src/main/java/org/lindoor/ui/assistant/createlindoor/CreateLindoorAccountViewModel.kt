package org.lindoor.ui.assistant.createlindoor

import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.entities.Account
import org.lindoor.ui.assistant.CreatorAssistantViewModel
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub

class CreateLindoorAccountViewModel :
    CreatorAssistantViewModel(corePreferences.lindoorAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var email: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass2: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    var creationResult = MutableLiveData<AccountCreator.Status>()

    private val creatorListener = object : AccountCreatorListenerStub() {
        override fun onCreateAccount(
            creator: AccountCreator?,
            status: AccountCreator.Status?,
            resp: String?
        ) {
            if (status == AccountCreator.Status.AccountCreated)
                creator?.also {
                    Account.lindoorAccountCreate(creator)
                }
            creationResult.postValue(status)
        }
    }

    init {
        accountCreator.addListener(creatorListener)
    }

    fun valid(): Boolean {
        return username.second.value!! && email.second.value!! && pass1.second.value!! && pass2.second.value!!
    }

    override fun onCleared() {
        accountCreator.removeListener(creatorListener)
        super.onCleared()
    }


}
