package com.itshedi.weedworld.ui.checkout

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.orders.OrdersActivity


class CheckOutCompletedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_completed)

        val qrCodeImageView = findViewById<ImageView>(R.id.qr_bitmap)
        val checkoutCompletedBtn = findViewById<Button>(R.id.doneCkeckOutBtn)
        val qrCode = intent.getStringExtra("randomQrCode")
        val qrCodeBitmap = generateQRCodeBitmap(qrCode.toString(), 300, 300)

        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        checkoutCompletedBtn.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            // intent.putExtra("qrCode", qrCode)
            startActivity(intent)
            finish()
        }

    }


    private fun generateQRCodeBitmap(content: String, width: Int, height: Int): Bitmap {
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

}