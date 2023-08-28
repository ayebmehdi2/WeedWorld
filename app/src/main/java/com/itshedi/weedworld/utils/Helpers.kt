package com.itshedi.weedworld.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.util.*

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null): Resource<T>(data)
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
}

fun randomName():String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    val x = (1..10)
        .map { allowedChars.random() }
        .joinToString("")
    Log.i("yayy","randomName: $x")
    return UUID.randomUUID().toString()
}

fun getAspectRatio(uri: Uri, context: Context): Float{
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inJustDecodeBounds = true
    BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
    inputStream?.close()
    return  bitmapOptions.outWidth.toFloat() / bitmapOptions.outHeight.toFloat()
}
fun generateAmPmTimes(): List<String> {
    val timesAM = mutableListOf<String>()
    val timesPM = mutableListOf<String>()
    for (hour in 0..11) {
        for (minute in 0..59 step 5) {
            val hourStr = if (hour == 0) "12" else hour.toString()
            val minuteStr = if (minute < 10) "0$minute" else minute.toString()
            timesAM.add("$hourStr:$minuteStr AM")
            timesPM.add("$hourStr:$minuteStr PM")
        }
    }
    timesAM.addAll(timesPM)
    return timesAM
}