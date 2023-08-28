package com.itshedi.weedworld.ui.qr_code_scanner

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.ResultPoint
import com.itshedi.weedworld.R
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)


        firestore = FirebaseFirestore.getInstance()


        barcodeView = findViewById(R.id.decoratedBarcodeView)
        barcodeView.decodeContinuous(callback)

        barcodeView.setOnClickListener {
            barcodeView.barcodeView.resume()
        }

    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                val scannedText = it.text
                // Handle the scanned QR code text
                Log.d("QRCodeScannerActivity", "Scanned Text: $scannedText")
                checkAndUpdateDeliveryStatus(scannedText)

                // To stop continuous scanning after a successful scan
                barcodeView.barcodeView.pause()
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {}
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }


    private fun checkAndUpdateDeliveryStatus(qrCodeContent: String) {
        // Query Firestore to find the order by QR code content
        val ordersCollection = firestore.collection("Order")
        val query = ordersCollection.whereEqualTo("qrCode", qrCodeContent)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val orderDocument = querySnapshot.documents.first()
                    val deliveryStatus = orderDocument.getLong("deliveryStatus") ?: 0

                    if (deliveryStatus == 0L) {
                        // Update the delivery status to 1
                        orderDocument.reference.update("deliveryStatus", 1)
                        Toast.makeText(this, "Delivery status updated!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Delivery status updated!")
                    } else {
                        Toast.makeText(this, "Order already picked up!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Order already picked up!")
                    }
                } else {
                    Toast.makeText(this, "Order not found!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Order not found!")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error retrieving order: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "Error retrieving order: ${exception.message}")
            }
    }

}