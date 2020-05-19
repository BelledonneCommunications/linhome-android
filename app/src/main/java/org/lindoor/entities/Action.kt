package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Action(val type:String?,val code:String?) : Parcelable