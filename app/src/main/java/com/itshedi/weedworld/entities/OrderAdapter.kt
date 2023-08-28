package com.itshedi.weedworld.entities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.itshedi.weedworld.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class OrderAdapter(private val orders: List<Order>, private val context: Context) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.qrCodeImg)
        val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTxtView)
        val deliveryStatusTextView: TextView = itemView.findViewById(R.id.deliveryStatusTxtView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]


        holder.imageView.setOnClickListener {
            val qrCodeContent = order.qrCode
            val qrCodeBitmap = generateQRCodeBitmapForItem(qrCodeContent, 500, 500)
            qrCodeBitmap?.let {
                showQrCodeDialog(it)
            }
        }
        holder.itemView.setOnClickListener {
            val orderTotal = order.orderTotal
            val deliveryType = order.deliveryMode
            val paymentType = order.paymentMode
            val firstName = order.firstName
            val lastName = order.lastName
            val mobileNumber = order.mobileNumber
            val address = order.address
            showOrderDetailsDialog(orderTotal,deliveryType,paymentType,firstName,lastName,mobileNumber,address)
        }


        holder.orderIdTextView.text = order.orderId

        if (order.deliveryStatus == 0) {
            holder.deliveryStatusTextView.text = "Delivery Status: Pending"
            holder.deliveryStatusTextView.setTextColor(Color.parseColor("#FF0000"))
        } else if (order.deliveryStatus == 1) {
            holder.deliveryStatusTextView.text = "Delivery Status: Delivered"
            holder.deliveryStatusTextView.setTextColor(Color.parseColor("#008000"))
        }

        val qrCodeContent = order.qrCode

        // Show a placeholder bitmap while the actual bitmap is generated
        holder.imageView.setImageResource(R.drawable.qr_example)

        CoroutineScope(Dispatchers.Main).launch {
            val qrCodeBitmap = generateQRCodeBitmap(qrCodeContent)
            if (qrCodeBitmap != null) {
                holder.imageView.setImageBitmap(qrCodeBitmap)
            } else {
                // Handle error case
            }
        }
    }

    override fun getItemCount() = orders.size

    private fun generateQRCodeBitmapForItem(content: String, width: Int, height: Int): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private suspend fun generateQRCodeBitmap(qrCodeContent: String): Bitmap? {
        return CoroutineScope(Dispatchers.Default).async {
            val qrCodeWriter = QRCodeWriter()
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.QR_VERSION] = 15

            try {
                val width = 1000
                val height = 1000

                val bitMatrix =
                    qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, width, height, hints)
                val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        qrCodeBitmap.setPixel(
                            x,
                            y,
                            if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                        )
                    }
                }
                qrCodeBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }.await()
    }

    private fun showQrCodeDialog(qrCodeBitmap: Bitmap) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qr_code, null)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.dialogQrCodeImageView)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showOrderDetailsDialog(
        orderTotal: String,
        deliveryType: Int,
        paymentType: Int,
        firstName: String,
        lastName: String,
        mobileNumber: String,
        address: String
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_details, null)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeOrderDetailsBtn)


        val orderTotalTxtView = dialogView.findViewById<TextView>(R.id.orderTotalTxtView)
        val deliveryTypeTxtView = dialogView.findViewById<TextView>(R.id.deliveryTypeTxtView)
        val paymentTypeTxtView = dialogView.findViewById<TextView>(R.id.paymentTypeTxtView)
        val firstNameTxtView = dialogView.findViewById<TextView>(R.id.firstNameTxtView)
        val lastNameTxtView = dialogView.findViewById<TextView>(R.id.lastNameTxtView)
        val mobileNumberTxtView = dialogView.findViewById<TextView>(R.id.mobileNumberTxtView)
        val addressTxtView = dialogView.findViewById<TextView>(R.id.addressTxtView)

        var deliveryTypeString  = ""
        var paymentTypeString = ""

        if (deliveryType == 0){
            deliveryTypeString = "Delivery Type : PickUp"
        } else if (deliveryType == 1){
            deliveryTypeString = "Delivery Type : Delivery"
        }
        if (paymentType == 0){
            paymentTypeString = "Payment Type : Cash"
        } else if (paymentType == 1){
            paymentTypeString = "Payment Type : Credit Card"
        }

        orderTotalTxtView.text = "$orderTotal$"
        deliveryTypeTxtView.text = deliveryTypeString
        paymentTypeTxtView.text = paymentTypeString
        firstNameTxtView.text = "First Name : $firstName"
        lastNameTxtView.text = "Last Name : $lastName"
        mobileNumberTxtView.text = "Mobile Number : $mobileNumber"
        addressTxtView.text = address



        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}