package com.itshedi.weedworld.ui.posts.user_post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itshedi.weedworld.ui.theme.accentGreen


@Composable
fun BottomSheetChatGroup(groups: List<String>, onSelect:(Int)->Unit, name:String, onNameChange:(String)->Unit, onCreateGroup:()->Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Spacer(modifier = Modifier.padding(8.dp))
        BottomSheetHandle(modifier = Modifier.align(CenterHorizontally))
        Spacer(modifier = Modifier.padding(9.dp))
        Text("Create list", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.padding(9.dp))
        TextField(value = name, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        placeholder = {Text( "Add your list name here")})
        Spacer(modifier = Modifier.padding(9.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()){
            itemsIndexed(groups){ index, item ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(index) }
                    .padding(8.dp)){
                    Text(item)
                }
            }
        }
        Spacer(modifier = Modifier.padding(9.dp))
        AddGroupButton {
            onCreateGroup()
        }
    }
}

@Composable
fun AddGroupButton(onOk:()->Unit) {
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
                onOk()
            }
            .padding(horizontal = 15.dp, vertical = 6.dp)) {
            Icon(
                imageVector = Icons.Outlined.Send,
                tint = Color.White,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
@Composable
fun BottomSheetActions(onDelete : (()->Unit)?=null,onEdit : (()->Unit)?=null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.padding(8.dp))
        BottomSheetHandle(modifier = Modifier.align(CenterHorizontally))
        Spacer(modifier = Modifier.padding(9.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            onDelete?.let {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { it.invoke() }
                    .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(30.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Text(text = "Delete", fontSize = 16.sp, color = Color.Gray)
                }
            }
            onEdit?.let {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { it.invoke() }
                    .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(30.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Text(text = "Edit", fontSize = 16.sp, color = Color.Gray)
                }
            }
        }
    }
    
}

@Composable
fun BottomSheetHandle(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colors.onBackground.copy(0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .width(100.dp)
            .height(4.dp)
    )
}
