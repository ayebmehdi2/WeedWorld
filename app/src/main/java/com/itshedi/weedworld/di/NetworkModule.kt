package com.itshedi.weedworld.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.itshedi.weedworld.repository.chat_repository.ChatRepository
import com.itshedi.weedworld.repository.chat_repository.ChatRepositoryImpl
import com.itshedi.weedworld.repository.nearby_repository.NearbyRepository
import com.itshedi.weedworld.repository.nearby_repository.NearbyRepositoryImpl
import com.itshedi.weedworld.repository.post_repository.PostRepository
import com.itshedi.weedworld.repository.post_repository.PostRepositoryImpl
import com.itshedi.weedworld.repository.store_repository.StoreRepository
import com.itshedi.weedworld.repository.store_repository.StoreRepositoryImpl
import com.itshedi.weedworld.repository.user_repository.UserRepository
import com.itshedi.weedworld.repository.user_repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth () = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseCloudStorage () = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore () = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase () = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun providesAuthRepository (firebaseAuth: FirebaseAuth, firebaseDatabase: FirebaseDatabase, firebaseStorage: FirebaseStorage) : UserRepository{
        return UserRepositoryImpl(firebaseAuth, firebaseDatabase, firebaseStorage)
    }

    @Provides
    @Singleton
    fun providesPostRepository (firebaseAuth: FirebaseAuth, firebaseDatabase: FirebaseDatabase, firebaseStorage: FirebaseStorage,
    firebaseFirestore: FirebaseFirestore) : PostRepository {
        return PostRepositoryImpl(firebaseAuth, firebaseStorage, firebaseFirestore)
    }

    @Provides
    @Singleton
    fun providesChatRepository (firebaseAuth: FirebaseAuth, firebaseFirestore: FirebaseFirestore) : ChatRepository{
        return ChatRepositoryImpl(firebaseAuth, firebaseFirestore)
    }

    @Provides
    @Singleton
    fun providesStoreRepository (firebaseAuth: FirebaseAuth, firebaseStorage: FirebaseStorage, firebaseFirestore: FirebaseFirestore) : StoreRepository{
        return StoreRepositoryImpl(firebaseAuth, firebaseStorage ,firebaseFirestore)
    }

    @Provides
    @Singleton
    fun providesNearbyRepository (firebaseAuth: FirebaseAuth, firebaseFirestore: FirebaseFirestore) : NearbyRepository{
        return NearbyRepositoryImpl(firebaseFirestore, firebaseAuth)
    }
}