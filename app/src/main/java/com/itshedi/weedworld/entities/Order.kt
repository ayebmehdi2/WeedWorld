package com.itshedi.weedworld.entities

import com.google.firebase.Timestamp

data class Order(
    val orderId: String,
    val deliveryMode: Int,
    val paymentMode: Int,
    val deliveryStatus: Int,
    val firstName: String,
    val lastName: String,
    val mobileNumber: String,
    val address: String,
    val orderTotal: String,
    val products: List<String>,
    val qrCode: String,
    val storeId: String,
    val storeName: String,
    val timeStamp: Timestamp,
    val userId: String
)
