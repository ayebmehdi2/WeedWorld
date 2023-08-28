package com.itshedi.weedworld.ui.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itshedi.weedworld.ui.theme.accentGreen

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier, viewModel: AuthViewModel) {
    Box(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            val focusManager = LocalFocusManager.current

            Spacer(modifier = Modifier.padding(top = 16.dp))
            //email
            TextField(
                leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colors.onBackground.copy(ContentAlpha.medium)) },
                value = viewModel.loginEmail,
                onValueChange = {
                    if (it.length < 64) {
                        viewModel.loginEmail = it
                        viewModel.loginEmailError = false
                    }
                },
                label = {
                    Text(text = "Email")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.loginEmailError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.loginEmailError) {
                Text(
                    text = "Invalid email",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            //password
            TextField(
                leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colors.onBackground.copy(ContentAlpha.medium)) },
                value = viewModel.loginPassword,
                onValueChange = {
                    viewModel.loginPassword = it
                    viewModel.loginPasswordError = false
                },
                label = {
                    Text(text = "Password")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.loginPasswordError,
                visualTransformation = PasswordVisualTransformation(),
                //Todo: if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = (
                            androidx.compose.ui.text.input.ImeAction.Next
                            )

                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.login(
                        email = viewModel.loginEmail,
                        password = viewModel.loginPassword
                    )
                }),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.loginPasswordError) {
                Text(
                    text = "Invalid password",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.padding(top = 40.dp))


            Box(modifier= Modifier.height(70.dp)
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(30.dp)).background(color = accentGreen, shape = RoundedCornerShape(30.dp)).clickable {
                viewModel.login(email = viewModel.loginEmail, password = viewModel.loginPassword)
            }){
                Text(text = "Login", color = Color.White, modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Forgot password?",
                color = accentGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp).align(CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}