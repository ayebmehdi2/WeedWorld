package com.itshedi.weedworld.ui.checkout

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Random


@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var viewModel: CartViewModel
    private val mFirestore = FirebaseFirestore.getInstance()
    private val randomQrCode = generateRandomCode(200)
    private var deliveryMode: Int = 0
    private var paymentMode: Int = 0


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        checkDeliveryAmount()

        viewModel = ViewModelProvider(this)[CartViewModel::class.java]

        val userId = Firebase.auth.currentUser?.uid.toString()

        val totalPrice = intent.getStringExtra("totalPrice") ?: ""
        val subTotal = intent.getStringExtra("totalPrice") ?: ""
        val storeId = intent.getStringExtra("storeId") ?: ""
        val formattedResultNandW = intent.getStringExtra("formattedResultNandW") ?: ""
        val formattedResultP = intent.getStringExtra("formattedResultP") ?: ""
        val businessName = intent.getStringExtra("businessName") ?: ""
        val formattedProductIdsList = intent.getStringExtra("formattedProductIdsList") ?: ""

        val firstNameTxt: EditText = findViewById(R.id.firstNameTxt)
        val lastNameTxt: EditText = findViewById(R.id.lastNameTxt)
        val mobileNumberTxt: EditText = findViewById(R.id.mobileNumberTxt)
        val addressTxt: EditText = findViewById(R.id.addressTxt)
        val city: EditText = findViewById(R.id.city)
        val street: EditText = findViewById(R.id.street)
        val zipCode : EditText = findViewById(R.id.zipcode)







        var firstName: String
        var lastName: String
        var mobileNumber: String
        val deliveryStatus = 0

        // list of orders
        val productIdsArray = formattedProductIdsList.split("\n")

        val productNamesAndWeightTxtView =
            findViewById<TextView>(R.id.productNamesAndWeightTxtView)
        val priceTxtView = findViewById<TextView>(R.id.priceTxtView)
        val subTotalTxtView = findViewById<TextView>(R.id.subTotalTxtView)
        val totalTxtView = findViewById<TextView>(R.id.totalTxtView)
        val businessNameTxtView = findViewById<TextView>(R.id.name_ch)
        val checkoutBtn = findViewById<Button>(R.id.checkout_btn)

        val radioGroup =
            findViewById<RadioGroup>(R.id.radioGroupPD)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            deliveryMode = when (checkedId) {
                R.id.radioButtonP -> 0
                R.id.radioButtonD -> 1
                else -> 0 // Default value
            }
        }
        val radioGroupPaymentType =
            findViewById<RadioGroup>(R.id.radioGroupCC)
        radioGroupPaymentType.setOnCheckedChangeListener { _, checkedId ->
            paymentMode = when (checkedId) {
                R.id.radioButtonCash -> 0
                R.id.radioButtonCreditCard -> 1
                else -> 0 // Default value
            }
        }


        checkoutBtn.setOnClickListener {


            // Get the current timestamp
            val timeStamp = Timestamp.now()

            //  get user info
            firstName = firstNameTxt.text.toString()
            lastName = lastNameTxt.text.toString()
            mobileNumber = mobileNumberTxt.text.toString()

            val a = "Address : " + addressTxt.text.toString();
            val c = "City : " + city.text.toString();
            val s = "Street : " + street.text.toString();
            val z = "ZIP code : " + zipCode.text.toString();

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && mobileNumber.isNotEmpty() &&
                a.isNotEmpty() && s.isNotEmpty() && c.isNotEmpty() && z.isNotEmpty()) {

                val Adess : String =
                    a + "\n" +
                            c + "\n" +
                            s + "\n" +
                            z

                if (radioGroup.checkedRadioButtonId == -1) {
                    // No radio button selected in delivery mode
                    Toast.makeText(this, "Please select a delivery mode", Toast.LENGTH_SHORT).show()
                } else if (radioGroupPaymentType.checkedRadioButtonId == -1) {
                    // No radio button selected in payment type
                    Toast.makeText(this, "Please select a payment type", Toast.LENGTH_SHORT).show()
                } else {
                    // All fields are filled and at least one checkbox from each group is selected
                    // store the order in order collection
                    placeOrder(
                        deliveryMode,
                        paymentMode,
                        deliveryStatus,
                        firstName,
                        lastName,
                        mobileNumber,
                        Adess,
                        totalPrice,
                        productIdsArray,
                        randomQrCode,
                        storeId,
                        businessName,
                        timeStamp,
                        userId
                    )

                }
            } else {
                // Empty fields
                Toast.makeText(this, "Please fill all the order information.", Toast.LENGTH_SHORT).show()
            }


        }

        productNamesAndWeightTxtView.text = formattedResultNandW
        priceTxtView.text = formattedResultP
        subTotalTxtView.text = subTotal
        totalTxtView.text = "$totalPrice$"
        businessNameTxtView.text = businessName

    }

    private fun generateRandomCode(length: Int): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/+"
        val random = Random()
        val code = StringBuilder(length)
        for (i in 0 until length) {
            code.append(characters[random.nextInt(characters.length)])
        }
        return code.toString()
    }


    private fun checkDeliveryAmount() {
        val deliveryAmountTxtView =
            findViewById<TextView>(R.id.deliveryAmountTxtView)
        val dlView = findViewById<LinearLayout>(R.id.dlView)
        // Delayed action to hide the TextView after a delay
        if (deliveryAmountTxtView.text == "0.0$") {
            val hideDelayMillis = 1000L
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                dlView.visibility = View.GONE
            }, hideDelayMillis)
        }

    }

    private fun placeOrder(
        deliveryMode: Int,
        paymentMode: Int,
        deliveryStatus: Int,
        firstName: String,
        lastName: String,
        mobileNumber: String,
        address: String,
        orderTotal: String,
        products: List<String>,  // also check if this correct for array type in firestore
        qrCode: String,
        storeId: String,
        storeName: String,
        timeStamp: Timestamp,
        userId: String
    ) {
        val order = hashMapOf(
            "deliveryMode" to deliveryMode,
            "paymentMode" to paymentMode,
            "deliveryStatus" to deliveryStatus,
            "firstName" to firstName,
            "lastName" to lastName,
            "mobileNumber" to mobileNumber,
            "address" to address,
            "orderTotal" to orderTotal,
            "products" to products, // here u must do change to make it an array list of productId's list from the formatted string that u give me,
            "qrCode" to qrCode,
            "storeId" to storeId,
            "storeName" to storeName,
            "timeStamp" to timeStamp,
            "userId" to userId
        )

        // Create a new document without specifying a document ID
        val newOrderRef = mFirestore.collection("Order").document()

        // Get the auto-generated document ID
        val orderId = newOrderRef.id

        // Set the orderId in the order data
        order["orderId"] = orderId

        // Create the document with the orderId included
        newOrderRef.set(order)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "Order ID : $orderId")

                showRoundedDialog()


            }
            .addOnFailureListener { Log.w(ContentValues.TAG, "Error writing document") }
    }

    private fun showRoundedDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rounded, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

        // Set click listener for the dialog button to dismiss the dialog
        dialogButton.setOnClickListener {
            dialogBuilder.dismiss()

            val intent = Intent(this, CheckOutCompletedActivity::class.java)
            intent.putExtra("randomQrCode", randomQrCode)
            startActivity(intent)
            finish()


        }

        // Show the rounded dialog
        dialogBuilder.show()
    }

    override fun onBackPressed() {
        Toast.makeText(this,"Please fill all the order information!",Toast.LENGTH_SHORT).show()
    }
}