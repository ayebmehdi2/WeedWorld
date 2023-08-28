package com.itshedi.weedworld.repository.nearby_repository

import android.util.Log
import androidx.compose.ui.text.toLowerCase
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.itshedi.weedworld.entities.*
import com.itshedi.weedworld.utils.Resource
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NearbyRepositoryImpl (
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
): NearbyRepository {
    override fun updateLocation(lat: Double, lng: Double): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())

            val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))

            val updates: MutableMap<String, Any> = mutableMapOf(
                "geohash" to hash,
                "lat" to lat,
                "lng" to lng
            )
            val locationCollection = firebaseFirestore.collection("UserLocation")
            val document = locationCollection.whereEqualTo("userId",firebaseAuth.currentUser!!.uid)
                .get().await().documents.firstOrNull()
            if (document==null){
                locationCollection.add(UserLocationSender(
                    geohash= hash,
                    lat = lat,
                    lng = lng,
                    timestamp = FieldValue.serverTimestamp(),
                    userId = firebaseAuth.currentUser!!.uid,
                )).await()
            }else{
                locationCollection.document(document.id).update(updates)
            }

            emit(Resource.Success(Unit))
        }.catch {
            Log.i("lstuff", " ${it.message}")
            emit(Resource.Error(message = "Error updating location"))
        }
    }

    override fun findNearbyUsers(lat: Double, lng: Double): Flow<Resource<List<UserLocation>>> {
        return flow {
            emit(Resource.Loading())

            val locationCollection = firebaseFirestore.collection("UserLocation")

            val userLocations = ArrayList<UserLocation>()

            val center = GeoLocation(lat, lng)
            val radiusInM = 50.0 * 1000.0

            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)

            for (b in bounds) {
                locationCollection
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .get().await().documents.forEach {
                        it.toObject<UserLocation>()?.let { if(it.userId!=firebaseAuth.currentUser?.uid) userLocations.add(it) }
                    }
            }

            emit(Resource.Success(userLocations.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading nearby users"))
        }
    }

    override fun findNearbyStores(lat: Double, lng: Double, radius:Double): Flow<Resource<List<Store>>> {
        return flow {
            emit(Resource.Loading())

            val storeCollection = firebaseFirestore.collection("Store")

            val storeLocations = ArrayList<Store>()

            val center = GeoLocation(lat, lng)
            val radiusInM = radius

            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)

            for (b in bounds) {
                storeCollection
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .get().await().documents.forEach { document ->
                        document.toObject<Store>()?.let {  storeLocations.add(it.copy(document.id)) }
                    }
            }
            emit(Resource.Success(storeLocations.toList()))
        }.catch {
            Log.i("zzzz", " ${it.message}")
            emit(Resource.Error(message = "Error loading nearby stores"))
        }
    }

}