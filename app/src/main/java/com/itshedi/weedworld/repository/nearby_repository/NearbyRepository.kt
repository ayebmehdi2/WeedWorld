package com.itshedi.weedworld.repository.nearby_repository

import com.itshedi.weedworld.entities.Store
import com.itshedi.weedworld.entities.UserLocation
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NearbyRepository {
    fun updateLocation(lat: Double, lng:Double): Flow<Resource<Unit>>

    fun findNearbyUsers(lat: Double, lng:Double): Flow<Resource<List<UserLocation>>>

    fun findNearbyStores(lat: Double, lng:Double, radius:Double): Flow<Resource<List<Store>>>

}