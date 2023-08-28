package com.itshedi.weedworld.entities

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.parcelize.Parcelize
import org.checkerframework.checker.units.qual.s


data class Like(
    val userId:String?=null,
    val postId:String?=null,
)

data class Comment(
    val userId:String?=null,
    val postId:String?=null,
    val content:String?=null,
    val timestamp: Timestamp? = null,
)

data class CommentSender(
    val userId:String,
    val postId:String,
    val content:String,
    val timestamp: FieldValue,
)

data class ShareSender(
    val userId:String,
    val postId:String,
    val timestamp: FieldValue,
)


data class ChatSender(
    val from:String,
    val to:String,
    val content:String,
    val timestamp: FieldValue,
    val emoji:Int?
)
data class Chat(
    val from:String?=null,
    val to:String?=null,
    val content:String?=null,
    val outgoing:Boolean?=true,
    val timestamp: Timestamp?=null,
    val emoji:Int?=null,
val inqueue:Boolean = false,
)

// note: Used to save in firestore
data class PostSender(
    val userId:String,
    val storeId:String? = null,
    val text: String? = null,
    val image: String? = null,
    val aspectRatio: Float? = null,
    val private: Boolean = false,
    val timestamp: FieldValue, // date time in timestamp format so we can sort by it
)

// note:used to retrieve
@Parcelize
data class Post(
    val postId: String? = null,
    val storeId: String? = null,
    val userId:String? = null,
    val text: String? = null,
    val image: String? = null, //path in firebase db
    val aspectRatio: Float? = null, //path in firebase db
    val private: Boolean = false,
    val timestamp: Timestamp? = null, // date time in timestamp format so we can sort by it
): Parcelable


data class StatusSender(
    val userId:String,
    val timestamp: FieldValue,
)

data class Status(
    val userId:String?=null,
    val timestamp: Timestamp?=null,
)

data class ChatGroup(
    val userId:String?=null,
    val to:List<String>?= listOf(),
    val name:String?=null
)

data class StoreRegistration(
    val userId:String?=null,
    val businessType:String?=null,
    val businessName:String?=null,
    val useFor:String?=null,
    val phone:String?=null,
    val address:String?=null,
    val licence:String?=null,
    val emailAddress:String?=null,
    val website:String?=null,
    val licencePDF:String?=null,
    val geohash:String?=null,
    val lat:Double?=null,
    val lng:Double?=null,
    val timestamp: FieldValue,
)
data class StoreSender(
    val userId:String?=null,
    val photo:String?=null,
    val coverPhoto:String?=null,
    val businessType:String?=null,
    val businessNameLowercase:String?=null,
    val businessName:String?=null,
    val phone:String?=null,
    val licence:String?=null,
    val emailAddress:String?=null,
    val website:String?=null,
    val timestamp: FieldValue,
)

@Parcelize
data class Store(
    val storeId:String?=null,
    val businessNameLowercase:String?=null,
    val photo:String?=null,
    val coverPhoto:String?=null,
    val businessType:String?=null,
    val businessName:String?=null,
    val phone:String?=null,
    val licence:String?=null,
    val emailAddress:String?=null,
    val website:String?=null,
    val timestamp: Timestamp?=null,
    val geohash:String?=null,
    val lat:Double?=null,
    val lng:Double?=null,
    val address:String?=null,
    val bio:String?=null,
    val fromTime:String?=null,
    val toTime:String?=null,
    val days:List<Int>?=null,
    val isFeatured:Boolean?=false
): Parcelable

data class UserLocationSender(
    val userId:String,
    val geohash:String,
    val lat:Double,
    val lng:Double,
    val timestamp: FieldValue,
)

data class UserLocation(
    val userId:String?=null,
    val geohash:String?=null,
    val lat:Double?=null,
    val lng:Double?=null,
    val timestamp: Timestamp?=null,
)

data class ProductSender(
    val storeId: String,
    val images: List<String>,
    val name:String,
    val nameLowerCase:String,
    val brand:String,
    val category:String,
    val specie:String?=null,
    val description:String,
    val thc:Double?=null,
    val cbd:Double?=null,
    val price:Double,
    val discountPrice:Double?=null,
    val weight:Double?=null,
    val timestamp: FieldValue,
)

@Parcelize
data class Product(
    val images: List<String> = listOf(),
    val storeId: String?=null,
    val productId:String?=null,
    val name:String?=null,
    val nameLowerCase:String?=null,
    val brand:String?=null,
    val category:String?=null,
    val specie:String?=null,
    val description:String?=null,
    val thc:Double?=null,
    val cbd:Double?=null,
    val price:Double?=null,
    val discountPrice:Double?=null,
    val weight:Double?=null,
    val timestamp: Timestamp?=null,
): Parcelable

