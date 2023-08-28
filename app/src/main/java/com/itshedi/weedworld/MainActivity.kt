package com.itshedi.weedworld

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.itshedi.weedworld.ui.auth.AuthScreen
import com.itshedi.weedworld.ui.auth.AuthViewModel
import com.itshedi.weedworld.ui.navigation.Navigation
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var viewModel:MainViewModel

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Log.i("cooltag", Firebase.auth.currentUser?.uid.toString())
            val navController = rememberNavController()
            WeedWorldTheme {
                BackHandler(true) {

                    viewModel.setStatus(false, onDone = {finish()})
                }
                Scaffold(

                ) {
                    Navigation(navController = navController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("yayy","resume")
        viewModel.setStatus(true, onDone = {})
        super.onDestroy()
    }
}
