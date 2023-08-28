package com.itshedi.weedworld.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itshedi.weedworld.ui.theme.VeryLightGreen
import com.itshedi.weedworld.ui.theme.accentGreen


@Composable
fun ProgressDialog() {
    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun ChoiceDialog(
    showDialog: Boolean,
    list: List<String>,
    onItemClick: (String, Int) -> Unit,
    onDismissRequest: () -> Unit,
    title:String
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismissRequest() },
            DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VeryLightGreen)
                        .padding(16.dp),
                    ){
                        Text(text = title, modifier = Modifier
                            .align(Alignment.TopCenter), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    }
                    LazyColumn {
                        itemsIndexed(items = list) { index, item ->
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onItemClick(item, index)
                                    onDismissRequest()
                                }
                                .padding(16.dp)
                            ){
                                Text(text = item, modifier = Modifier)
                            }
                            if (index + 1 < list.size) {
                                Divider(thickness = 1.dp)
                            }
                        }
                    }
                }

            }
        }
    }
}



@Composable
fun ChoiceConfirmDialog(
    showDialog: Boolean,
    message: @Composable (() -> Unit),
    okMessage: String,
    cancelMessage: String,
    onOk: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismissRequest() },
            DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {

                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    message()
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { onOk() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = Color.White, contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = okMessage
                        )
                    }

                    Button(
                        onClick = { onCancel() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = accentGreen, contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = cancelMessage
                        )
                    }

                }
            }
        }
    }
}