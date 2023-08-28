package com.itshedi.weedworld

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
//            .setPersistenceEnabled(false)
//            .build()
    }
}