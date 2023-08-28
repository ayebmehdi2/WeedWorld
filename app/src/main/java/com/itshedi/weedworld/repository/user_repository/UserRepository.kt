package com.itshedi.weedworld.repository.user_repository

import android.net.Uri
import com.google.firebase.auth.AuthResult
import com.google.firebase.storage.StorageReference
import com.itshedi.weedworld.entities.User
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun login(email:String, password:String): Flow<Resource<AuthResult>>

    fun register(email:String, password:String, birthdate:String,username:String,gender:String,phone:String): Flow<Resource<AuthResult>>

    fun logout()

    fun updateProfilePicture(user:UserInfo, uri: Uri): Flow<Resource<Uri>>

    fun updateCoverPicture(user: UserInfo, uri: Uri): Flow<Resource<Uri>>

    fun currentUserInfo(): Flow<Resource<UserInfo>>

    fun updateUserInfo(user: UserInfo): Flow<Resource<UserInfo>>

    fun getUserById(id:String): Flow<Resource<UserInfo>>


//    fun getDownloadUrl(path:String): Flow<Resource<String>>
//    fun updateProfile(email:String, password:String, birthdate:String,username:String,gender:String,phone:String): Flow<Resource<AuthResult>>
}