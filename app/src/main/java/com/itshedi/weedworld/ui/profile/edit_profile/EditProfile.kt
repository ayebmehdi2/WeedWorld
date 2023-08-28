package com.itshedi.weedworld.ui.profile.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.ui.theme.accentGreen

@Composable
fun EditProfile(navController: NavController, viewModel: EditProfileViewModel) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = true
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(title = { Text(text = "Personnal information") },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.username,
                onValueChange = {
                    viewModel.username = it
                    viewModel.usernameError = false

                },
                label = {
                    Text(text = "Username")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.usernameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))

            TextField(
                enabled = false,
                value = viewModel.email,
                onValueChange = {
                    viewModel.email = it

                },
                label = {
                    Text(text = "Email")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))

            TextField(
                enabled = false,
                value = viewModel.wwID,
                onValueChange = {
                    viewModel.wwID = it
                    viewModel.wwIDError = false

                },
                label = {
                    Text(text = "WW ID")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.wwIDError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )


            Spacer(modifier = Modifier.padding(top = 10.dp))

            TextField(
                enabled = false,

                leadingIcon = {
                    Icon(
                        imageVector = when (viewModel.gender) {
                            "Male" -> Icons.Outlined.Male
                            "Female" -> Icons.Outlined.Female
                            else -> Icons.Outlined.Transgender
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.gender,
                onValueChange = {
                    viewModel.gender = it
                    viewModel.genderError = false

                },
                label = {
                    Text(text = "Gender")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.genderError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))

            TextField(
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.password,
                onValueChange = {
                    viewModel.password = it
                    viewModel.passwordError = false
                },
                label = {
                    Text(text = "Password")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                //Todo: if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))

            TextField(
                enabled = false,

                value = viewModel.birthday,
                onValueChange = {
                    viewModel.birthday = it
                    viewModel.birthdateError = false

                },
                label = {
                    Text(text = "Date of Birth")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.birthdateError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))


            TextField(
                enabled = false,
                value = viewModel.location,
                onValueChange = {
                    viewModel.location = it
                    viewModel.localtionError = false
                },
                label = {
                    Text(text = "Location")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )

            Spacer(modifier = Modifier.padding(top = 30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .fillMaxHeight()
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color = Color.LightGray, shape = RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.42f)
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color = Color(0xFFB93118), shape = RoundedCornerShape(5.dp))
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "42% ", color = Color(0xFFB93118),
                    fontSize = 12.sp
                )
                Text(
                    "complete", color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Text(
                "Completing profile can help gain more followers",
                color = Color.LightGray,
                modifier = Modifier.align(CenterHorizontally),
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Box(modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(30.dp))
                    .background(color = accentGreen, shape = RoundedCornerShape(30.dp))
                    .clickable {
                        //todo: save edited info
                    }
                    .padding(horizontal = 15.dp, vertical = 6.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

        }
    }
}