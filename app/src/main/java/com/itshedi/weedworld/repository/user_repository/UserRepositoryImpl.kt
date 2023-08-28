package com.itshedi.weedworld.repository.user_repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itshedi.weedworld.entities.StatusSender
import com.itshedi.weedworld.entities.User
import com.itshedi.weedworld.entities.UserInfo
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) : UserRepository {
    override fun login(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            //check if user signup is complete (have userinfo in RTDB)
            if(firebaseDatabase.getReference("Users").child(
                result.user!!.uid
            ).get().await().value == null) {
                throw(Exception("User registration incomplete"))
            }
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun register(
        email: String,
        password: String,
        birthdate: String,
        username: String,
        gender: String,
        phone: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            firebaseDatabase.getReference("Users").child(result.user!!.uid).setValue(
                User(
                    username = username,
                    birthday = birthdate,
                    gender = gender,
                    phone = phone,
                    uid = result.user!!.uid,
                    email = email
                )
            ).await()
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun currentUserInfo(): Flow<Resource<UserInfo>> {
        return flow {
            emit(Resource.Loading())
            firebaseAuth.currentUser?.uid?.let {

                emit(
                    Resource.Success(
                        firebaseDatabase.getReference("Users").child(it).get().await()
                            .getValue(UserInfo::class.java)
                    )
                )
            }
        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error loading user data"))
        }
    }

    override fun updateUserInfo(user: UserInfo): Flow<Resource<UserInfo>> {
        return flow {
            emit(Resource.Loading())
            firebaseDatabase.getReference("Users").child(firebaseAuth.currentUser!!.uid).setValue(
                user
            ).await()
            emit(Resource.Success(user))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun getUserById(id: String): Flow<Resource<UserInfo>> {
        return flow {
            emit(Resource.Loading())

            emit(
                Resource.Success(
                    firebaseDatabase.getReference("Users").child(id).get().await()
                        .getValue(UserInfo::class.java)
                )
            )

        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error loading user data"))
        }
    }
//
//    override fun getDownloadUrl(path: String): Flow<Resource<String>> {
//        return flow {
//            emit(Resource.Loading())
//            emit(
//                Resource.Success(
//                    firebaseStorage.reference.child(path).downloadUrl.await().toString()
//                )
//            )
//        }.catch {
//            emit(Resource.Error("Resource not found"))
//        }
//    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun updateProfilePicture(user: UserInfo, uri: Uri): Flow<Resource<Uri>> {
        Log.i("yayy", "addProfilePicture")
        return flow {
            emit(Resource.Loading())
            val profilePhotoUrl = uploadImage(uri, "images/profile", user.uid!!).downloadUrl.await()
            emit(Resource.Success(profilePhotoUrl))
        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error updating user data"))
        }
    }

    override fun updateCoverPicture(user: UserInfo, uri: Uri): Flow<Resource<Uri>> {
        Log.i("yayy", "updateCoverPicture")
        return flow {
            emit(Resource.Loading())
            val profileCoverUrl = uploadImage(uri, "images/cover", user.uid!!).downloadUrl.await()
            emit(Resource.Success(profileCoverUrl))
        }.catch {
            Log.i("yayy", it.message.toString())
            emit(Resource.Error("Error updating user data"))
        }
    }

    private suspend fun delelteImage(path: String) {
        firebaseStorage.reference.child(path).delete().await()
    }

    private suspend fun uploadImage(uri: Uri, path: String, userId:String): StorageReference { // todo: update user
        val storageRef = firebaseStorage.reference
        val riversRef = storageRef.child("$path/$userId")
        riversRef.putFile(uri).await()
        return riversRef
    }


}