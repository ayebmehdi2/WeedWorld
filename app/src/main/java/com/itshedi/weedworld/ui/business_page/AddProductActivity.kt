package com.itshedi.weedworld.ui.business_page

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itshedi.weedworld.R
import com.itshedi.weedworld.ui.auth.DefaultSnackbar
import com.itshedi.weedworld.ui.dialogs.ProgressDialog
import com.itshedi.weedworld.ui.store_registration.FormInput
import com.itshedi.weedworld.ui.theme.WeedWorldTheme
import com.itshedi.weedworld.ui.theme.accentGreen
import com.itshedi.weedworld.utils.productCategories
import com.itshedi.weedworld.utils.productSpecies
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddProductActivity : ComponentActivity() {
    lateinit var viewModel: AddProductViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AddProductViewModel::class.java]


        val bundle = intent.getBundleExtra("bundle")

        if (bundle!!.containsKey("storeId")) {
            viewModel.storeId = bundle.getString("storeId")!!
        }

        if (bundle.containsKey("product")) {
            viewModel.editableProduct = bundle.getParcelable("product")
        }


        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setNavigationBarColor(
                color = Color.Black.copy(0.15f),
                darkIcons = true
            )




            LaunchedEffect(viewModel.editableProduct){
                viewModel.editableProduct?.let{
                    viewModel.productName = it.name?:""
                    viewModel.brandName = it.brand?:""
                    viewModel.category = it.category?:""
                    viewModel.specie = it.specie?:""
                    viewModel.description = it.description?:""
                    viewModel.thc = it.thc?.toString()?:""
                    viewModel.cbd = it.cbd?.toString()?:""
                    viewModel.price = it.price?.toString()?:""
                    viewModel.discountPrice = it.discountPrice?.toString()?:""
                    viewModel.weight = it.weight?.toString()?:""
                    viewModel.photoList.addAll(it.images.map { url -> Uri.parse(url) })
                }
            }
            WeedWorldTheme {

                val scaffoldState = rememberScaffoldState() // this contains the `SnackbarHostState`
                var snackbarMessage by remember { mutableStateOf<String?>(null) }
                var cs = rememberCoroutineScope()
                val state = viewModel.eventFlow.collectAsState(AddProductEvent.empty).value
                if (state is AddProductEvent.loading) {
                    ProgressDialog()
                }
                LaunchedEffect(state) {
                    when (state) {
                        is AddProductEvent.showSnackbar -> {
                            snackbarMessage = state.message
                        }
                        is AddProductEvent.productAdded -> {
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
                                Icon(painter = painterResource(id = R.drawable.back_chevron), contentDescription = null)
                            }
                        }, title = { Text(when(viewModel.editableProduct){
                            null -> "Add product"
                            else -> "Edit product"
                        })},
                        backgroundColor = Color.White,
                        contentColor = Color.Black,
                            elevation = 0.dp
                        )
                    }
                ){ paddingValues ->
                    Box(modifier = Modifier.fillMaxSize()){
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(300.dp)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(0.15f),
                                        )
                                    )
                                )
                        )
                        Column(modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(paddingValues)) {
                            Spacer(modifier = Modifier.padding(top = 8.dp))
                            AddPhotoSection()
                            ProductForm()
                            Spacer(modifier = Modifier.padding(top = 22.dp))
                            ConfirmCheckButton(
                                onClick = {
                                    viewModel.submit()
                                }
                            )
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

    @Composable
    fun ProductForm() {
        Column(modifier = Modifier
            .padding(horizontal = 20.dp)) {
            FormInput(
                value = viewModel.productName,
                onValueChange = {
                    viewModel.productName = it
                    viewModel.productNameError = false
                },
                label = "Product Name",
                icon = painterResource(id = R.drawable.licence),
                isError = viewModel.productNameError,
            )

            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.brandName,
                onValueChange = {
                    viewModel.brandName = it
                    viewModel.brandNameError = false
                },
                label = "Brand Name",
                icon = painterResource(id = R.drawable.website),
                isError = viewModel.brandNameError,
            )

            Spacer(modifier = Modifier.padding(top = 20.dp))
            Box {
                DropdownMenu(
                    modifier = Modifier.align(Alignment.TopEnd),
                    expanded = viewModel.categoryMenu,
                    onDismissRequest = { viewModel.categoryMenu = false },
                ) {
                    productCategories.forEach {
                        DropdownMenuItem(onClick = {
                            viewModel.category = it.name
                            viewModel.categoryMenu = false
                        }) {
                            Text(it.name)
                        }
                    }
                }
                FormInput(value = viewModel.category,
                    onValueChange = {
                        viewModel.specie = ""
                        viewModel.categoryError = false },
                    label = "Category",
                    icon = painterResource(id = R.drawable.use_for),
                    isError = viewModel.categoryError,
                    enabled = false,
                    actionIcon = Icons.Default.ArrowDropDown,
                    onAction = { viewModel.categoryMenu = true })
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))

            if(viewModel.isSpecieSelectionRequired(viewModel.category)){
                Box {
                    DropdownMenu(
                        modifier = Modifier.align(Alignment.TopEnd),
                        expanded = viewModel.specieMenu,
                        onDismissRequest = { viewModel.specieMenu = false },
                    ) {
                        productSpecies.forEach {
                            DropdownMenuItem(onClick = {
                                viewModel.specie = it.name
                                viewModel.specieMenu = false
                            }) {
                                Text(it.name)
                            }
                        }
                    }
                    FormInput(value = viewModel.specie,
                        onValueChange = { viewModel.specieError = false },
                        label = "Specie",
                        icon = painterResource(id = R.drawable.specie),
                        isError = viewModel.specieError,
                        enabled = false,
                        actionIcon = Icons.Default.ArrowDropDown,
                        onAction = { viewModel.specieMenu = true })
                }

                Spacer(modifier = Modifier.padding(top = 20.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                FormInput(
                    modifier = Modifier.weight(1f),
                    value = viewModel.thc,
                    onValueChange = {
                        viewModel.thc = it
                        viewModel.thcError = false
                    },
                    label = "THC %",
                    icon = painterResource(id = R.drawable.website),
                    isError = viewModel.thcError,
                    numeric = true
                )
                Spacer(modifier = Modifier.padding(end = 10.dp))
                FormInput(
                    modifier = Modifier.weight(1f),
                    value = viewModel.cbd,
                    onValueChange = {
                        viewModel.cbd = it
                        viewModel.cbdError = false
                    },
                    label = "CBD %",
                    icon = painterResource(id = R.drawable.website),
                    isError = viewModel.cbdError,
                    numeric = true
                )
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FormInput(
                    modifier = Modifier.weight(1f),
                    value = viewModel.price,
                    onValueChange = {
                        viewModel.price = it
                        viewModel.priceError = false
                    },
                    label = "Price",
                    icon = painterResource(id = R.drawable.website),
                    isError = viewModel.priceError,
                    numeric = true
                )
                Spacer(modifier = Modifier.padding(end = 10.dp))
                FormInput(
                    modifier = Modifier.weight(1f),
                    value = viewModel.discountPrice,
                    onValueChange = {
                        viewModel.discountPrice = it
                        viewModel.discountPriceError = false
                    },
                    label = "Discount",
                    icon = painterResource(id = R.drawable.website),
                    isError = viewModel.discountPriceError,
                    numeric = true
                )
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))
            FormInput(
                value = viewModel.weight,
                onValueChange = {
                    viewModel.weight = it
                    viewModel.weightError = false
                },
                label = "Weight",
                icon = painterResource(id = R.drawable.website),
                isError = viewModel.weightError,
                numeric = true
            )
            Spacer(modifier = Modifier.padding(top = 20.dp))
            DescriptionInput(value = viewModel.description, onValueChange = {viewModel.description = it})
        }
    }

    @Composable
    fun DescriptionInput(
        value: String,
        onValueChange: (String) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp)){
            if (value.isBlank()) {
                Text("Add description..", color = Color.Gray)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            )
        }
    }
    @Composable
    fun AddPhotoSection() {
        val addPhoto =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    viewModel.photoList.add(0,uri)
                }
            }
        LazyRow(modifier = Modifier.padding(vertical = 8.dp)){
            item { Spacer(modifier = Modifier.padding(start = 16.dp)) }
            item {
                AddPhotoButton(onClick = {
                    addPhoto.launch(arrayOf("*/*"))
                })
            }
            itemsIndexed(items = viewModel.photoList){ index, item ->  
                PhotoItem(uri = item,
                onDelete = {viewModel.photoList.removeAt(index)},
                isPrimary = viewModel.primaryPhotoIndex == index,
                onClick = {
                    viewModel.primaryPhotoIndex = index
                })
            }
            item { Spacer(modifier = Modifier.padding(start = 16.dp)) }
        }
    }

    @Composable
    fun AddPhotoButton(onClick:()->Unit) {
        Box(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(80.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .background(color = Color.LightGray, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(34.dp))
        }
    }
    @Composable
    fun PhotoItem(uri: Uri, isPrimary: Boolean, onDelete:()->Unit, onClick: () -> Unit) {
        Box(Modifier
            .padding(end = 10.dp)){
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { onClick() }
                    .background(color = Color.Gray, shape = CircleShape)
                    .border(
                        width = 4.dp,
                        color = when (isPrimary) {
                            true -> accentGreen
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = rememberImagePainter(data = uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(0.5f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable {
                        onDelete()
                    }
                    .padding(4.dp)
                    .align(
                        Alignment.TopEnd
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp),
                    tint = Color.White
                )
            }
        }

    }
    
}

//note: common

@Composable
fun ConfirmCheckButton(modifier: Modifier = Modifier,onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
    ) {
        Box(modifier = Modifier
            .align(Alignment.CenterEnd)
            .clip(RoundedCornerShape(30.dp))
            .background(color = accentGreen, shape = RoundedCornerShape(30.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 4.dp)) {
            Icon(
                imageVector = Icons.Outlined.Check,
                tint = Color.White,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}