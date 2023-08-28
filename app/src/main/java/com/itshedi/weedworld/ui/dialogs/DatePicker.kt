package com.itshedi.weedworld.ui.dialogs

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DatePicker(onDateSelected: (LocalDate) -> Unit, onDismissRequest: () -> Unit) {
    val selDate = remember { mutableStateOf(LocalDate.now().minusYears(18)) }

    Dialog(onDismissRequest = { onDismissRequest() }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(size = 16.dp)
                )
        ) {
            Column(
                Modifier
                    .defaultMinSize(minHeight = 72.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select date".toUpperCase(Locale.ENGLISH),
                    style = MaterialTheme.typography.caption,
                )

                Spacer(modifier = Modifier.size(24.dp))

                Text(
                    text = selDate.value.format(DateTimeFormatter.ofPattern("MMM d, YYYY")),
                    style = MaterialTheme.typography.h4,
                )

                Spacer(modifier = Modifier.size(16.dp))
            }

            CustomCalendarView(onDateSelected = {
                selDate.value = it
            })

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(
                        text = "Cancel")
                }

                TextButton(
                    onClick = {
                        onDateSelected(selDate.value)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "OK")
                }

            }
        }
    }
}

@Composable
fun CustomCalendarView(onDateSelected: (LocalDate) -> Unit) {
    // Adds view to Compose
    AndroidView(
        modifier = Modifier.wrapContentSize().background(Color.White),
        factory = { context ->
            CalendarView(context)

        },
        update = { view ->
            // Get the maximum date 18 years ago
            val maxDate = LocalDate.now().minusYears(18)
            // Set the maximum date on the CalendarView
            view.maxDate = maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                onDateSelected(
                    LocalDate
                        .now()
                        .withMonth(month)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }
        }
    )
}