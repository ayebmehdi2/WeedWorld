package com.itshedi.weedworld.ui.store_registration

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.theme.VeryLightGray
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import com.itshedi.weedworld.ui.theme.accentBlue
import com.itshedi.weedworld.ui.theme.accentGreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreRegistrationActivity : ComponentActivity() {
    lateinit var viewModel: StoreRegistrationViewModel
    lateinit var geoCoder: Geocoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[StoreRegistrationViewModel::class.java]
        geoCoder = Geocoder(this)
        setContent {
            WeedWorldTheme {
                with(viewModel.currentPage) {
                    BackHandler(this != StoreRegistrationPage.StoreType) {
                        when (this) {
                            StoreRegistrationPage.Registration -> {
                                viewModel.currentPage = StoreRegistrationPage.StoreType
                            }
                            StoreRegistrationPage.BillingInfo -> {
                                viewModel.currentPage = StoreRegistrationPage.Registration
                            }
                            StoreRegistrationPage.LocationMap -> {
                                viewModel.currentPage = StoreRegistrationPage.Registration
                            }
                            else -> {}
                        }
                    }
                }

                val scaffoldState = rememberScaffoldState()
                var snackbarMessage by remember { mutableStateOf<String?>(null) }

                val state = viewModel.eventFlow.collectAsState(StoreRegistrationEvent.empty).value
                if (state is StoreRegistrationEvent.loading) {
                    ProgressDialog()
                }
                LaunchedEffect(state) {
                    when (state) {
                        is StoreRegistrationEvent.showSnackbar -> {
                            snackbarMessage = state.message
                        }
                        is StoreRegistrationEvent.empty -> {
                            Log.i("cooltag", "empty")
                        }
                        is StoreRegistrationEvent.loading -> {
                            Log.i("cooltag", "loading")
                        }
                        is StoreRegistrationEvent.success -> {
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }
                Scaffold(scaffoldState = scaffoldState, topBar = {
                    StoreRegistrationTopNav(onBack = {
                        with(viewModel.currentPage) {
                            when (this) {
                                StoreRegistrationPage.Registration -> {
                                    viewModel.currentPage = StoreRegistrationPage.StoreType
                                }
                                StoreRegistrationPage.BillingInfo -> {
                                    viewModel.currentPage = StoreRegistrationPage.Registration
                                }
                                StoreRegistrationPage.StoreType -> {
                                    finish()
                                }
                                StoreRegistrationPage.LocationMap -> {
                                    viewModel.currentPage = StoreRegistrationPage.Registration
                                }
                            }
                        }
                    })
                }) { paddingValues ->
                    Column(modifier = Modifier) {
                        when (viewModel.currentPage) {
                            StoreRegistrationPage.StoreType -> StoreTypeMenu(
                                onSelect = {
                                    viewModel.storeType = it
                                    viewModel.currentPage = StoreRegistrationPage.Registration
                                }, modifier = Modifier.padding(paddingValues)
                            )
                            StoreRegistrationPage.BillingInfo -> BillingInfoPage()
                            StoreRegistrationPage.Registration -> RegistrationForm()

                            StoreRegistrationPage.LocationMap -> LocationPicker()
                        }
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }

        }
    }

    // put this in activity instead of vm to avoid mem leak
    fun getAddress(lng: Double, lat: Double, onResult: (Address?, Double?, Double?) -> Unit) {
        onResult(geoCoder.getFromLocation(lng, lat, 1)?.firstOrNull(), lat, lng)
    }

    @Composable
    fun LocationPicker() {
        Box(modifier = Modifier.fillMaxSize()) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    LatLng(
                        40.745341, -73.986370
                    ), 15f
                )
            }

            GoogleMap(
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(compassEnabled = false, mapToolbarEnabled = false)
            ) {
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            cameraPositionState.position.target.latitude,
                            cameraPositionState.position.target.longitude
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .align(BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedButton(onClick = {
                    getAddress(cameraPositionState.position.target.latitude,
                        cameraPositionState.position.target.longitude,
                        onResult = { address, lat, lng ->
                            address?.let {
                                viewModel.address = it.getAddressLine(0)
                                viewModel.lat = lat
                                viewModel.lng = lng
                            }
                        })
                    viewModel.currentPage = StoreRegistrationPage.Registration

                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)) {
                    Text("Confirm")
                }
            }

        }
    }

    @Composable
    fun RegistrationForm() {

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // business type
            Box {
                DropdownMenu(
                    modifier = Modifier.align(TopEnd),
                    expanded = viewModel.businessTypeMenu,
                    onDismissRequest = { viewModel.businessTypeMenu = false },
                ) {
                    viewModel.businessTypes.forEach {
                        DropdownMenuItem(onClick = {
                            viewModel.businessType = it
                            viewModel.businessTypeMenu = false
                        }) {
                            Text(it)
                        }
                    }
                }
                FormInput(value = viewModel.businessType,
                    onValueChange = { viewModel.businessTypeError = false },
                    label = "Business Type",
                    icon = painterResource(id = R.drawable.business),
                    isError = viewModel.businessTypeError,
                    enabled = false,
                    actionIcon = Icons.Default.ArrowDropDown,
                    onAction = { viewModel.businessTypeMenu = true })
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.businessName,
                onValueChange = {
                    viewModel.businessName = it
                    viewModel.businessNameError = false
                },
                label = "Business Name",
                icon = painterResource(id = R.drawable.business),
                isError = viewModel.businessNameError,
            )

            if (viewModel.isTHCRegistration()) {
                Spacer(modifier = Modifier.padding(top = 20.dp))
                Box {
                    DropdownMenu(
                        modifier = Modifier.align(TopEnd),
                        expanded = viewModel.useForMenu,
                        onDismissRequest = { viewModel.useForMenu = false },
                    ) {
                        viewModel.useForTypes.forEach {
                            DropdownMenuItem(onClick = {
                                viewModel.useFor = it
                                viewModel.useForMenu = false
                            }) {
                                Text(it)
                            }
                        }
                    }
                    FormInput(value = viewModel.useFor,
                        onValueChange = { viewModel.useForError = false },
                        label = "Use for",
                        icon = painterResource(id = R.drawable.use_for),
                        isError = viewModel.useForError,
                        enabled = false,
                        actionIcon = Icons.Default.ArrowDropDown,
                        onAction = { viewModel.useForMenu = true })
                }
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.phone,
                onValueChange = {
                    viewModel.phone = it
                    viewModel.phoneError = false
                },
                label = "Phone no",
                icon = painterResource(id = R.drawable.phone),
                isError = viewModel.phoneError,
                numeric = true
            )

            if (viewModel.storeType == 1) {
                Spacer(modifier = Modifier.padding(top = 20.dp))
                FormInput(value = viewModel.address,
                    onValueChange = {
                        viewModel.address = it
                        viewModel.addressError = false
                    },
                    label = "Address",
                    icon = painterResource(id = R.drawable.phone),
                    isError = viewModel.addressError,
                    enabled = false,
                    onAction = {
                        viewModel.currentPage = StoreRegistrationPage.LocationMap
                    })
            }


            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.licence,
                onValueChange = {
                    viewModel.licence = it
                    viewModel.licenceError = false
                },
                label = "Licence no",
                icon = painterResource(id = R.drawable.licence),
                isError = viewModel.licenceError,
                numeric = true
            )



            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.emailAddress,
                onValueChange = {
                    viewModel.emailAddress = it
                    viewModel.emailAddressError = false
                },
                label = "Email address",
                icon = painterResource(id = R.drawable.email),
                isError = viewModel.emailAddressError,
            )

            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.website,
                onValueChange = {
                    viewModel.website = it
                    viewModel.websiteError = false
                },
                label = "Website",
                icon = painterResource(id = R.drawable.website),
                isError = viewModel.websiteError,
            )

            val pdfPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let {
                        viewModel.licencePDF = it
                    }
                }

            if (viewModel.isTHCRegistration()) {
                Spacer(modifier = Modifier.padding(top = 20.dp))
                FormInput(value = when (viewModel.licencePDF == null) {
                    true -> ""
                    else -> "licence.pdf"
                },
                    onValueChange = { viewModel.licencePDFError = false },
                    label = "Upload PDF to your licence",
                    icon = painterResource(id = R.drawable.pdf_licence),
                    isError = viewModel.licencePDFError,
                    enabled = false,
                    actionIconPainter = painterResource(id = R.drawable.plus_pdf),
                    onAction = { pdfPicker.launch(arrayOf("application/pdf")) })
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))
            BillingInfoButton(onClick = {
                viewModel.currentPage = StoreRegistrationPage.BillingInfo
            }, isError = viewModel.billingInfoError)
            Spacer(modifier = Modifier.padding(top = 10.dp))
            TOSSection(
                onAgree = { viewModel.tosAgreed = !viewModel.tosAgreed },
                agreed = viewModel.tosAgreed
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
            RegisterButton(
                onClick = { viewModel.registerStore() },
                modifier = Modifier.align(CenterHorizontally),
                enabled = viewModel.tosAgreed
            )
        }

    }


    @Composable
    fun BillingInfoButton(onClick: () -> Unit, isError: Boolean) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp, color = when (isError) {
                    true -> Color.Red
                    false -> VeryLightGray
                }, shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 30.dp, vertical = 16.dp)) {
            Text(
                text = "Billing information",
                fontWeight = FontWeight.SemiBold,
                color = accentBlue,
                modifier = Modifier.align(
                    CenterStart
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.chevron_next),
                contentDescription = null,
                modifier = Modifier.align(
                    CenterEnd
                )
            )
        }
    }

    @Composable
    fun RegisterButton(onClick: () -> Unit, modifier: Modifier, enabled: Boolean) {
        Box(modifier = modifier
            .height(70.dp)
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(35.dp))
            .background(
                color = when (enabled) {
                    true -> accentGreen
                    false -> Color.LightGray
                }, shape = RoundedCornerShape(35.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 8.dp)) {
            Text(
                text = "Register",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    fun TOSSection(onAgree: () -> Unit, agreed: Boolean) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = agreed, onClick = { onAgree() })
            Spacer(modifier = Modifier.padding(end = 10.dp))
            Text(
                "By registering to Weed World I agree to Terms of service as well as the cotent policy.",
                color = Color.Gray,
                fontSize = 14.sp,
            )
        }
    }

    @Composable
    fun BillingInfoPage() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Billing information", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}


@Composable
fun FormInput(
    modifier: Modifier=Modifier.fillMaxWidth(), // added this so it would take all space in case no specific modifier applied like weight or size
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Painter? = null,
    iconVector: ImageVector? = null,
    isError: Boolean,
    enabled: Boolean = true,
    actionIcon: ImageVector? = null,
    actionIconPainter: Painter? = null,
    onAction: (() -> Unit)? = null,
    numeric: Boolean = false,
    textFieldColors: TextFieldColors = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        disabledTextColor = MaterialTheme.colors.onBackground,
        disabledTrailingIconColor = MaterialTheme.colors.onBackground,
        disabledLabelColor = MaterialTheme.colors.onBackground.copy(ContentAlpha.medium),
        disabledIndicatorColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor = MaterialTheme.colors.onBackground,
    ),
    background: Color = Color.Transparent,
    borderColor: Color = Color.Transparent
) {
    Box(modifier =  when (onAction) {
        null -> modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = background, shape = RoundedCornerShape(12.dp))
        else -> modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = background, shape = RoundedCornerShape(12.dp))
            .clickable { onAction.invoke() }
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
    }){

        TextField(
            enabled = enabled,
            leadingIcon = if (icon !=null || iconVector != null) {
                @Composable {
                    icon?.let {
                        Icon(
                            painter = it, contentDescription = null, tint = accentGreen
                        )
                    }
                    iconVector?.let {
                        Icon(
                            imageVector = it, contentDescription = null, tint = accentGreen
                        )
                    }
                }
            } else null,
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(text = label, overflow = TextOverflow.Ellipsis)
            },
            modifier = modifier,
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                keyboardType = when (numeric) {
                    true -> KeyboardType.Number
                    false -> KeyboardType.Text
                }
            ),
            colors = textFieldColors,
            trailingIcon = {
                actionIcon?.let { Icon(imageVector = it, contentDescription = null) }
                actionIconPainter?.let {
                    Image(
                        painter = it, contentDescription = null, modifier = Modifier.size(24.dp)
                    )
                }
            },
        )
    }
}

@Composable
fun StoreRegistrationTopNav(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        IconButton(onClick = { onBack() }, modifier = Modifier.align(CenterStart)) {
            Icon(painter = painterResource(id = R.drawable.back_chevron), contentDescription = null)
        }

        Row(modifier = Modifier.align(Center), verticalAlignment = CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.business_upgrades),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.padding(end = 14.dp))
            Text(text = "Business upgrades", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StoreTypeMenu(onSelect: (Int) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        listOf(
            Pair("Online Store", R.drawable.online_store),
            Pair("Local Store", R.drawable.local_store),
            Pair("Business Page", R.drawable.business_page)
        ).forEachIndexed { index, pair ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = VeryLightGray, shape = RoundedCornerShape(16.dp))
                .clickable { onSelect(index) }
                .padding(horizontal = 30.dp, vertical = 16.dp),
                verticalAlignment = CenterVertically) {
                Image(
                    painter = painterResource(id = pair.second),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.padding(end = 16.dp))
                Text(text = pair.first, fontSize = 16.sp)
            }


        }
    }
}