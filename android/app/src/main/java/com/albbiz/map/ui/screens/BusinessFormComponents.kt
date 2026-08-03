// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MeTontRed
    )
}

// Splits a stored "HH:mm" 24-hour string into (hour, minute), falling back to
// 09:00 for anything unparseable (blank, malformed, or a leftover typed value
// from before this became a picker).
fun parseStoredTime(time: String): Pair<Int, Int> {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

// Friendly 12-hour display ("9:00 AM") for a stored "HH:mm" 24-hour string —
// the stored format itself doesn't change, only how it's shown.
fun formatTimeDisplay(time: String): String {
    val (hour, minute) = parseStoredTime(time)
    val period = if (hour < 12) "AM" else "PM"
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, period)
}

// Read-only, tap-to-open time field. Replaces a free-text field that used
// KeyboardType.Number — most Android number keyboards have no ":" key at
// all, so typing a valid "09:00" was practically impossible. This opens a
// real time picker instead and still writes back the same "HH:mm" format
// the rest of the app (and Firestore) already expects.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = MeTontGrey)
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true },
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCCCCCC))
        ) {
            Text(
                formatTimeDisplay(time),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }

    if (showPicker) {
        val (initialHour, initialMinute) = remember(time) { parseStoredTime(time) }
        val pickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(label, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChanged(String.format(Locale.US, "%02d:%02d", pickerState.hour, pickerState.minute))
                    showPicker = false
                }) {
                    Text(strings.ok, color = MeTontRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
    }
}

@Composable
fun WorkingHoursEditor(
    hours: Map<String, String>,
    onHoursChanged: (Map<String, String>) -> Unit
) {
    val strings = LocalAppStrings.current
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { day ->
            var openTime by remember { mutableStateOf(hours["${day}_open"] ?: "09:00") }
            var closeTime by remember { mutableStateOf(hours["${day}_close"] ?: "18:00") }
            var isClosed by remember { mutableStateOf(hours["${day}_closed"] == "true") }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        day,
                        modifier = Modifier.width(36.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 13.sp
                    )

                    if (isClosed) {
                        Text(
                            strings.closedLabel,
                            color = MeTontRed,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                null,
                                tint = MeTontRed,
                                modifier = Modifier.size(14.dp)
                            )
                            TimePickerField(
                                label = strings.hoursOpenLabel,
                                time = openTime,
                                onTimeChanged = {
                                    openTime = it
                                    onHoursChanged(hours + ("${day}_open" to it))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Text("-", color = MeTontGrey, fontWeight = FontWeight.Bold)
                            TimePickerField(
                                label = strings.hoursCloseLabel,
                                time = closeTime,
                                onTimeChanged = {
                                    closeTime = it
                                    onHoursChanged(hours + ("${day}_close" to it))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            isClosed = !isClosed
                            onHoursChanged(hours + ("${day}_closed" to isClosed.toString()))
                        }
                    ) {
                        Text(
                            if (isClosed) "Open" else "Close",
                            color = MeTontRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationPickerDialog(
    initialLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Select Location",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Enter coordinates or pick from map",
                    style = MaterialTheme.typography.bodySmall,
                    color = MeTontGrey
                )
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Lat: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed
                            )
                            Text(
                                "${selectedLocation.latitude}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Lng: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed
                            )
                            Text(
                                "${selectedLocation.longitude}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLocationSelected(selectedLocation) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                )
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MeTontGrey)
            }
        }
    )
}