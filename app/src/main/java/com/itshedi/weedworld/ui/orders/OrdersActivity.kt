package com.itshedi.weedworld.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.itshedi.weedworld.R
import com.itshedi.weedworld.entities.Order
import com.itshedi.weedworld.entities.OrderAdapter
import com.itshedi.weedworld.ui.qr_code_scanner.QRCodeScannerActivity


class OrdersActivity : AppCompatActivity() {

    private val orders = mutableListOf<Order>()
    private lateinit var orderAdapter: OrderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val recyclerView: RecyclerView = findViewById(R.id.ordersRecyclerView)
        val mProgressBar: ProgressBar = findViewById(R.id.mProgressBar)
        val backBtn: ImageView = findViewById(R.id.back_from_checkout_btn)
        val scanBtn: ImageView = findViewById(R.id.scan_qr_code_btn)

        scanBtn.setOnClickListener {
            val intent6 = Intent(this, QRCodeScannerActivity::class.java)
            this.startActivity(intent6)
        }

        val currentUserId = Firebase.auth.currentUser?.uid.toString()

        // Initialize RecyclerView and adapter
        orderAdapter = OrderAdapter(orders, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = orderAdapter
        // Retrieve orders from Firestore and update the adapter
        val firestore = FirebaseFirestore.getInstance()
        val ordersCollection = firestore.collection("Order")
        mProgressBar.visibility = View.VISIBLE
        ordersCollection.addSnapshotListener { querySnapshot, _ ->
            orders.clear()
            for (document in querySnapshot!!.documents) {
                val orderId = document.getString("orderId") ?: ""
                val deliveryMode = document.getLong("deliveryMode")?.toInt() ?: 0
                val paymentMode = document.getLong("paymentMode")?.toInt() ?: 0
                val deliveryStatus = document.getLong("deliveryStatus")?.toInt() ?: 0
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val address = document.getString("address") ?: ""
                val mobileNumber = document.getString("mobileNumber") ?: ""
                val orderTotal = document.getString("orderTotal") ?: ""
                val products = document.get("products") as? List<String> ?: emptyList()
                val qrCode = document.getString("qrCode") ?: ""
                val storeId = document.getString("storeId") ?: ""
                val storeName = document.getString("storeName") ?: ""
                val timeStamp = document.getTimestamp("timeStamp") ?: Timestamp.now()
                val userId = document.getString("userId") ?: ""
                val order = Order(
                    orderId,
                    deliveryMode,
                    paymentMode,
                    deliveryStatus,
                    firstName,
                    lastName,
                    mobileNumber,
                    address,
                    orderTotal,
                    products,
                    qrCode,
                    storeId,
                    storeName,
                    timeStamp,
                    userId
                )

                // Check if the order's userId matches the current user's userId
                if (userId == currentUserId) {
                    orders.add(order)
                }

                mProgressBar.visibility = View.GONE
            }
            orderAdapter.notifyDataSetChanged()
        }

        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


}