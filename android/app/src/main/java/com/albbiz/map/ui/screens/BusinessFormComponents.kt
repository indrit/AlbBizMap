// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.google.android.gms.maps.model.LatLng

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MeTontRed
    )
}

@Composable
fun WorkingHoursEditor(
    hours: Map<String, String>,
    onHoursChanged: (Map<String, String>) -> Unit
) {
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
                            "Closed",
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
                            OutlinedTextField(
                                value = openTime,
                                onValueChange = {
                                    openTime = it
                                    onHoursChanged(hours + ("${day}_open" to it))
                                },
                                label = { Text("Open", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MeTontRed,
                                    cursorColor = MeTontRed
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text("-", color = MeTontGrey, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = closeTime,
                                onValueChange = {
                                    closeTime = it
                                    onHoursChanged(hours + ("${day}_close" to it))
                                },
                                label = { Text("Close", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MeTontRed,
                                    cursorColor = MeTontRed
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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