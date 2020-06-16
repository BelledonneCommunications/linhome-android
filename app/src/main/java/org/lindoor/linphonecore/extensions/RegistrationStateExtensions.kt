package org.lindoor.linphonecore.extensions

import org.lindoor.customisation.Texts
import org.linphone.core.RegistrationState

fun RegistrationState.toHumanReadable():String {
    return when (this) {
        RegistrationState.None -> Texts.get("registration_state_none")
        RegistrationState.Progress -> Texts.get("registration_state_progress")
        RegistrationState.Ok -> Texts.get("registration_state_ok")
        RegistrationState.Failed -> Texts.get("registration_state_failed")
        RegistrationState.Cleared -> Texts.get("registration_state_cleared")
    }
}
