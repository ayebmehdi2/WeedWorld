package com.itshedi.weedworld.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
private object LocalModule {

    @Singleton
    @Provides
    @Named("preferences_inject")
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("STUFF_STORAGE", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

}