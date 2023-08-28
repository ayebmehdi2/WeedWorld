package com.itshedi.weedworld.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val birthday: String? = "",
    val email: String? = "",
    val gender: String? = "",
    val phone: String? = "",
    val uid: String? = "",
    val username: String? = "",
    val location: String? = "",
    val bio: String? = "",
    val profilePhoto: String? = "",
    val coverPhoto: String? = ""
): Parcelable