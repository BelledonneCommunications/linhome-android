package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.customisation.ActionTypes

@Parcelize
data class Action(val type:String?,val code:String?) : Parcelable {

    fun typeName():String? {
        return type?.let {
            ActionTypes.typeNameForActionType(it)
        } ?: null
    }


}