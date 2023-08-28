package com.itshedi.weedworld.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.dt.composedatepicker.ComposeCalendar
import com.dt.composedatepicker.SelectDateListener
import com.itshedi.weedworld.ui.dialogs.DatePicker
import com.itshedi.weedworld.ui.theme.accentGreen
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*



@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel, modifier: Modifier) {

    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        DatePicker(onDateSelected = {
            viewModel.registerBirthDate =
            DateTimeFormatter.ofPattern("MMM d, yyyy").format(it)
                                    },
            onDismissRequest = { showDatePicker = false })
    }
    Box(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            val focusManager = LocalFocusManager.current


            Spacer(modifier = Modifier.padding(top = 16.dp))
            //username
            TextField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.registerUsername,
                onValueChange = {
                    viewModel.registerUsername = it
                    viewModel.registerUsernameError = false

                },
                label = {
                    Text(text = "Username")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.registerUsernameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.registerUsernameError) {
                Text(
                    text = "Invalid username",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))

            //password
            TextField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.registerPassword,
                onValueChange = {
                    viewModel.registerPassword = it
                    viewModel.registerPasswordError = false
                },
                label = {
                    Text(text = "Password")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.registerPasswordError,
                visualTransformation = PasswordVisualTransformation(),
                //Todo: if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = (
                            ImeAction.Next
                            )

                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.register(
                        email = viewModel.registerEmail,
                        password = viewModel.registerPassword,
                        username = viewModel.registerUsername,
                        gender = viewModel.registerGender,
                        phone = viewModel.registerPhone,
                        birthdate = viewModel.registerBirthDate
                    )
                }),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.registerPasswordError) {
                Text(
                    text = "Invalid password",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            //email
            TextField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.registerEmail,
                onValueChange = {
                    viewModel.registerEmail = it
                    viewModel.registerEmailError = false

                },
                label = {
                    Text(text = "Email")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.registerEmailError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.registerEmailError) {
                Text(
                    text = "Invalid email",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))

            //phone
            TextField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.registerPhone,
                onValueChange = {
                    viewModel.registerPhone = it
                    viewModel.registerPhoneError = false
                },
                label = {
                    Text(text = "Phone number")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.registerPhoneError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.registerPhoneError) {
                Text(
                    text = "Invalid phone number",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))


            // birth date
            TextField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground.copy(
                            ContentAlpha.medium
                        )
                    )
                },
                value = viewModel.registerBirthDate,
                readOnly = true,
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    viewModel.registerBirthDateError = false
                                    showDatePicker = true
                                }
                            }
                        }
                    },
                onValueChange = {
                },
                label = {
                    Text(text = "Birth date")
                },
                placeholder = {
                    Text("DD/MM/YYYY")
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isError = viewModel.registerBirthDateError,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
            )
            if (viewModel.registerBirthDateError) {
                Text(
                    text = "Invalid birth date",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 22.dp))

            GenderPicker(onPicked = {
                viewModel.registerGenderError = false
                viewModel.registerGender = it
            }, selectedGender = viewModel.registerGender)
            Spacer(modifier = Modifier.padding(top = 8.dp))
            if (viewModel.registerGenderError) {
                Text(
                    text = "You must select a gender",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 14.dp))

            AgeAgreementSection(onAgree = {  viewModel.ageAgreedError = false
                viewModel.ageAgreed = true }, agreed = viewModel.ageAgreed, isError = viewModel.ageAgreedError)
            Box(modifier = Modifier
                .height(70.dp)
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(color = accentGreen, shape = RoundedCornerShape(30.dp))
                .clickable {
                    viewModel.register(
                        email = viewModel.registerEmail,
                        password = viewModel.registerPassword,
                        username = viewModel.registerUsername,
                        gender = viewModel.registerGender,
                        phone = viewModel.registerPhone,
                        birthdate = viewModel.registerBirthDate
                    )
                }) {
                Text(
                    text = "Register",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}


@Composable
fun GenderPicker(onPicked: (String) -> Unit, selectedGender: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(
            Pair("Male", Icons.Filled.Male),
            Pair("Female", Icons.Filled.Female),
            Pair("Other", Icons.Filled.Transgender),
        ).forEach { (gender, icon) ->
            GenderItem(text = gender, icon = icon, isSelected = selectedGender == gender,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onPicked(gender)
                    }
                    .padding(4.dp))
        }
    }
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
    )
}

@Composable
fun AgeAgreementSection(onAgree: () -> Unit, agreed: Boolean, isError:Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = agreed, onClick = { onAgree() })
        Spacer(modifier = Modifier.padding(end = 10.dp))
        Text(
            "I confirm I am 18 years or older",
            color = when(isError){true -> Color.Red else -> Color.Gray},
            fontSize = 14.sp,
        )
    }
}

@Composable
fun GenderItem(modifier: Modifier, text: String, icon: ImageVector, isSelected: Boolean) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon, contentDescription = null, tint = when (isSelected) {
                true -> accentGreen
                false -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
            }
        )
        Spacer(modifier = Modifier.padding(start = 4.dp))
        Text(text = text)
    }
}