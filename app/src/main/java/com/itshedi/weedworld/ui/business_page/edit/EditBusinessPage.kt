package com.itshedi.weedworld.ui.business_page.edit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.auth.DefaultSnackbar
import com.itshedi.weedworld.ui.business_page.AddProductEvent
import com.itshedi.weedworld.ui.business_page.ConfirmCheckButton
import com.itshedi.weedworld.ui.dialogs.ChoiceDialog
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.store_registration.FormInput
import com.itshedi.weedworld.ui.theme.VeryLightGray
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.ui.theme.editBusinessFieldBackground
import com.itshedi.weedworld.utils.generateAmPmTimes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditBusinessPage : ComponentActivity() {
    lateinit var viewModel: EditBusinessPageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[EditBusinessPageViewModel::class.java]

        val bundle = intent.getBundleExtra("bundle")

        if (bundle!!.containsKey("business")) {
            viewModel.business = bundle.getParcelable("business")
        }

        setContent{


            LaunchedEffect(viewModel.business){
                viewModel.phone = viewModel.business?.phone?:""
                viewModel.address = viewModel.business?.address?:""
                viewModel.emailAddress = viewModel.business?.emailAddress?:""
                viewModel.website = viewModel.business?.website?:""
                viewModel.bio = viewModel.business?.bio?:""
                viewModel.fromTime = viewModel.business?.fromTime?:""
                viewModel.toTime = viewModel.business?.toTime?:""
                viewModel.days.clear()
                viewModel.days.addAll(viewModel.business?.days?: listOf())
            }
            WeedWorldTheme {

                val scaffoldState = rememberScaffoldState() // this contains the `SnackbarHostState`
                var snackbarMessage by remember { mutableStateOf<String?>(null) }
                var cs = rememberCoroutineScope()
                val state = viewModel.eventFlow.collectAsState(EditBusinessPageEvent.empty).value
                if (state is EditBusinessPageEvent.loading) {
                    ProgressDialog()
                }
                LaunchedEffect(state) {
                    when (state) {
                        is EditBusinessPageEvent.showSnackbar -> {
                            snackbarMessage = state.message
                        }
                        is EditBusinessPageEvent.done -> {
                            cs.launch {
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                        else -> {}
                    }
                }
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.back_chevron),
                                    contentDescription = null
                                )
                            }
                        }, title = { Text("Edit information") },
                            backgroundColor = Color.White,
                            contentColor = Color.Black,
                            elevation = 0.dp
                        )
                    }
                ) { paddingValues ->

                    Box(modifier = Modifier.fillMaxSize()) {

                        ConfirmCheckButton(onClick = {
                            viewModel.submit()
                        }, modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp))

                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                                .padding(20.dp)
                        ) {

                            val textFieldColors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                disabledTextColor = MaterialTheme.colors.onBackground,
                                disabledTrailingIconColor = MaterialTheme.colors.onBackground,
                                disabledLabelColor = MaterialTheme.colors.onBackground.copy(
                                    ContentAlpha.medium
                                ),
                                disabledIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                disabledPlaceholderColor = MaterialTheme.colors.onBackground,
                            )
                            FormInput(
                                value = viewModel.phone,
                                onValueChange = {
                                    viewModel.phone = it
                                    viewModel.phoneError = false
                                },
                                label = "Phone no",
                                icon = painterResource(id = R.drawable.business_edit_phone),
                                isError = viewModel.phoneError,
                                numeric = true,
                                textFieldColors = textFieldColors,
                                background = editBusinessFieldBackground
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            FormInput(
                                value = viewModel.website,
                                onValueChange = {
                                    viewModel.website = it
                                    viewModel.websiteError = false
                                },
                                label = "Email address",
                                icon = painterResource(id = R.drawable.business_edit_email),
                                isError = viewModel.websiteError,
                                textFieldColors = textFieldColors,
                                background = editBusinessFieldBackground
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            FormInput(
                                value = viewModel.emailAddress,
                                onValueChange = {
                                    viewModel.emailAddress = it
                                    viewModel.emailAddressError = false
                                },
                                label = "Website",
                                icon = painterResource(id = R.drawable.business_edit_website),
                                isError = viewModel.emailAddressError,
                                textFieldColors = textFieldColors,
                                background = editBusinessFieldBackground
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            FormInput(
                                value = viewModel.bio,
                                onValueChange = {
                                    viewModel.bio = it
                                },
                                label = "Add bio",
                                iconVector = Icons.Default.Add,
                                isError = false,
                                textFieldColors = textFieldColors,
                                background = editBusinessFieldBackground
                            )
                            


                            Spacer(modifier = Modifier.padding(top = 20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painter = painterResource(id = R.drawable.business_edit_time), contentDescription = null,
                                tint = accentGreen, modifier = Modifier.padding(start = 10.dp))
                                Spacer(modifier = Modifier.padding(end = 10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    ChoiceDialog(
                                        title = "Opening time",
                                        showDialog = viewModel.fromTimeMenu,
                                        list = generateAmPmTimes(),
                                        onItemClick = { item,index ->
                                            viewModel.fromTime = item
                                            Log.i("yayy","ChoiceDialog")
                                        },
                                        onDismissRequest = {
                                            viewModel.fromTimeMenu = false
                                        }
                                    )
                                    FormInput(
                                        value = viewModel.fromTime,
                                        onValueChange = { },
                                        label = "Open at",
                                        isError = false,
                                        enabled = false,
                                        actionIcon = Icons.Default.ArrowDropDown,
                                        onAction = { viewModel.fromTimeMenu = true },
                                        textFieldColors = textFieldColors,
                                        borderColor = VeryLightGray,
                                        modifier = Modifier
                                    )
                                }
                                Text(
                                    "to",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontSize = 16.sp
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    ChoiceDialog(
                                        title = "Closing time",
                                        showDialog = viewModel.toTimeMenu,
                                        list = generateAmPmTimes(),
                                        onItemClick = { item,index ->
                                            viewModel.toTime = item
                                        },
                                        onDismissRequest = {
                                            viewModel.toTimeMenu = false
                                        }
                                    )
                                    FormInput(
                                        value = viewModel.toTime,
                                        onValueChange = { },
                                        label = "Closes at",
                                        isError = false,
                                        enabled = false,
                                        actionIcon = Icons.Default.ArrowDropDown,
                                        onAction = { viewModel.toTimeMenu = true},
                                        textFieldColors = textFieldColors,
                                        borderColor = VeryLightGray,
                                        modifier = Modifier
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.padding(top = 20.dp))

                            Box {
                                DropdownMenu(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    expanded = viewModel.daysMenu,
                                    onDismissRequest = { viewModel.daysMenu = false },
                                ) {
                                    viewModel.daysList.forEachIndexed { index, item ->
                                        DropdownMenuItem(onClick = {
                                            when (viewModel.days.contains(index)) {
                                                true -> viewModel.days.remove(index)
                                                false -> viewModel.days.add(index)
                                            }
                                        }) {
                                            Checkbox(
                                                checked = viewModel.days.contains(index),
                                                onCheckedChange = null)
                                            Spacer(modifier = Modifier.padding(end = 10.dp))
                                            Text(viewModel.daysList[index])
                                            Spacer(modifier = Modifier.padding(end = 4.dp))
                                        }
                                    }
                                }
                                FormInput(
                                    value = viewModel.days.joinToString(", ") { viewModel.daysList[it] },
                                    onValueChange = { },
                                    label = "Select day",
                                    icon = painterResource(id = R.drawable.business_edit_day),
                                    isError = false,
                                    enabled = false,
                                    actionIcon = Icons.Default.ArrowDropDown,
                                    onAction = { viewModel.daysMenu = true },
                                    textFieldColors = textFieldColors,
                                    borderColor = VeryLightGray
                                )
                            }
                            
                            Spacer(modifier = Modifier.padding(top = 60.dp))
                        }
                        if(snackbarMessage!=null){
                            DefaultSnackbar(
                                message = snackbarMessage?:"", onDismiss = {
                                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .systemBarsPadding()
                            )
                            LaunchedEffect(true){
                                delay(2000)
                                snackbarMessage = null
                            }
                        }
                    }
                }
            }
        }
    }
}